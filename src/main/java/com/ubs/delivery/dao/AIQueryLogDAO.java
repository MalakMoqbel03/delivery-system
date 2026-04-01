package com.ubs.delivery.dao;

import com.ubs.delivery.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
// handles AI query logs in DB,used for caching +saving +viewing history

public class AIQueryLogDAO {

    private final Connection conn;

    public AIQueryLogDAO() {
        this.conn = DBConnection.getConnection();
    }

     //check if a recent AI response exists from the last 24h return it if found, else return null

    public String getCachedResponse(String locationCode, String queryType)
            throws SQLException {
        String sql = "SELECT ai_response FROM ai_query_log "
                + "WHERE location_code = ? "
                + "AND query_type = ? "
                + "AND queried_at >= NOW() - INTERVAL 24 HOUR "
                + "ORDER BY queried_at DESC "
                + "LIMIT 1";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, locationCode);
        ps.setString(2, queryType.toUpperCase());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("ai_response");
        }
        return null;
    }
    public void saveResponse(String locationCode, String queryType,
                             String promptSummary, String aiResponse)
            throws SQLException {
        String sql = "INSERT INTO ai_query_log "
                + "(location_code, query_type, prompt_summary, ai_response) "
                + "VALUES (?, ?, ?, ?)"; // ? for user inputs (safe from sql injection)
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, locationCode);
        ps.setString(2, queryType.toUpperCase());
        ps.setString(3, promptSummary);
        ps.setString(4, aiResponse);

        ps.executeUpdate();
        System.out.println("[DB] AI response logged for "
                + locationCode + " [" + queryType.toUpperCase() + "]");
    }
    public void printAllHistory() throws SQLException {
        String sql = "SELECT location_code, query_type, prompt_summary, "
                + "LEFT(ai_response, 80) AS preview, queried_at "
                + "FROM ai_query_log ORDER BY queried_at DESC";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);

        System.out.println("\n========== AI Query History ==========");
        System.out.printf("%-12s %-12s %-30s %-22s%n",
                "Location", "Type", "Summary", "Queried At");
        System.out.println("-".repeat(80));

        boolean empty = true;
        while (rs.next()) {
            empty = false;
            System.out.printf("%-12s %-12s %-30s %-22s%n",
                    rs.getString("location_code"),
                    rs.getString("query_type"),
                    truncate(rs.getString("prompt_summary"), 28),
                    rs.getTimestamp("queried_at").toString());
            System.out.println("  Preview: " + rs.getString("preview") + "...");
            System.out.println();
        }
        if (empty) {
            System.out.println("  No AI queries logged yet.");
        }
        System.out.println("======================================");
    }
    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 2) + "..";
    }
}