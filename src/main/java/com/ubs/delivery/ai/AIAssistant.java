package com.ubs.delivery.ai;

import com.ubs.delivery.dao.AIQueryLogDAO;
import com.ubs.delivery.model.DeliveryPoint;
import com.ubs.delivery.model.Location;
import com.ubs.delivery.model.Warehouse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;

//this class connects Java with Python AI , Handles caching +calling AI +saving results

//Uses ProcessBuilder to run Python script and read its output

public class AIAssistant {

    private static final String PYTHON_SCRIPT = "python/ai_query.py";
    private final AIQueryLogDAO logDAO;
    public AIAssistant() {
        this.logDAO = new AIQueryLogDAO();
    }
    public String getDeliveryInsights(Location loc) {
        String queryType = "INSIGHTS";
        String locType   = loc.getClass().getSimpleName();
        String details;

        if (loc instanceof DeliveryPoint) {
            details = ((DeliveryPoint) loc).getPriority();
        } else if (loc instanceof Warehouse) {
            Warehouse wh = (Warehouse) loc;
            details = wh.getCurrentLoad() + "/" + wh.getMaxCapacity();
        } else {
            details = "N/A";
        }
        return callAI(queryType, loc.getCode(), loc.getName(), locType, details,
                "Delivery insights for " + loc.getName());
    }

    public String getRestockingAdvice(Warehouse wh) {
        String details = wh.getCurrentLoad() + "/" + wh.getMaxCapacity();
        return callAI("RESTOCKING", wh.getCode(), wh.getName(), "Warehouse",
                details, "Restocking advice for " + wh.getName());
    }

    public String comparePriority(DeliveryPoint dp1, DeliveryPoint dp2) {
        String details = dp1.getName() + "|" + dp1.getPriority() + "|" + dp1.getDistanceKm()
                + "|" + dp2.getName() + "|" + dp2.getPriority() + "|" + dp2.getDistanceKm();

        return callAI("COMPARISON", dp1.getCode(), dp1.getName(), "DeliveryPoint",
                details, "Comparison: " + dp1.getName() + " vs " + dp2.getName());
    }


    public String planRoute(List<Location> locations) {
        StringBuilder sb = new StringBuilder();
        for (Location loc : locations) {
            sb.append(loc.getName())
                    .append("(").append(loc.getCode()).append(")")
                    .append(":").append(loc.getDistanceKm()).append("km|");
        }
        String result = callPython("route", "All Locations", "Mixed", sb.toString());

        try {
            logDAO.saveResponse("LOC_1", "ROUTE",
                    "Full route for " + locations.size() + " stops", result);
        } catch (SQLException e) {
            System.err.println("[DB] Could not log route query: " + e.getMessage());
        }

        return result;
    }

    // main flow: check cache then call AI then save result
    private String callAI(String queryType, String locationCode,
                          String locationName, String locationType,
                          String details, String summary) {
        try {
            String cached = logDAO.getCachedResponse(locationCode, queryType);
            if (cached != null) {
                System.out.println("[CACHE] Using cached AI response from DB (< 24h old).");
                return cached;
            }
            String response = callPython(queryType.toLowerCase(),
                    locationName, locationType, details);
            logDAO.saveResponse(locationCode, queryType, summary, response);
            return response;
        } catch (SQLException e) {
            System.err.println("[DB] Cache error: " + e.getMessage());
            return callPython(queryType.toLowerCase(),
                    locationName, locationType, details);
        }
    }
    private String callPython(String queryType, String locationName,
                              String locationType, String details) {
        StringBuilder output = new StringBuilder();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "python3",
                    PYTHON_SCRIPT,
                    queryType,
                    locationName,
                    locationType,
                    details
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("[AI] Python exited with code " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("[AI] Failed to run Python script: " + e.getMessage());
            System.err.println("     Make sure python3 is installed and in your PATH.");
            return "AI service unavailable. Check python3 installation.";
        }
        return output.toString().trim();
    }
}