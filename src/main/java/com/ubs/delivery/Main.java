package com.ubs.delivery;

import com.ubs.delivery.ai.AIAssistant;
import com.ubs.delivery.dao.AIQueryLogDAO;
import com.ubs.delivery.dao.LocationDAO;
import com.ubs.delivery.model.*;
import com.ubs.delivery.util.DBConnection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner input    = new Scanner(System.in);
        LocationDAO dao  = new LocationDAO();
        AIAssistant ai   = new AIAssistant();
        AIQueryLogDAO logDAO = new AIQueryLogDAO();
        System.out.println("loading locations from MySQL");
        int choice;
        do {
            printMenu();
            System.out.print("Choose an option:");
            choice = Integer.parseInt(input.nextLine().trim());

            switch (choice) {
                case 1 -> {
                    try {
                        List<Location> all = dao.getAllLocations();
                        System.out.println("\nAll Locations (" + all.size() + " total):");
                        for (int i = 0; i < all.size(); i++) {
                            System.out.println((i + 1) + ". " + all.get(i).describe());
                        }
                    } catch (SQLException e) {
                        System.err.println("DB error:" + e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        addLocation(dao, input);
                    } catch (SQLException e) {
                        System.err.println("DB error:" + e.getMessage());
                    }
                }
                case 3 -> {
                    try {
                        List<Location> sorted = dao.getAllSortedByDistance();
                        System.out.println("\nlocations sorted by distance from HQ:");
                        for (int i = 0; i < sorted.size(); i++) {
                            System.out.println((i + 1) + ". " + sorted.get(i));
                        }
                    } catch (SQLException e) {
                        System.err.println("DB error:" + e.getMessage());
                    }
                }
                case 4 -> {
                    try {
                        List<Location> high = dao.getHighPriorityDeliveryPoints();
                        System.out.println("\nhigh priority DP:");
                        if (high.isEmpty()) {
                            System.out.println("None found");
                        }
                        for (Location loc : high) {
                            System.out.println("  → " + loc.describe());
                        }
                    } catch (SQLException e) {
                        System.err.println("DB error: " + e.getMessage());
                    }
                }
                case 5 -> {
                    try {
                        List<Location> space = dao.getWarehousesWithSpace();
                        System.out.println("\nWarehouses with Available Space:");
                        if (space.isEmpty()) {
                            System.out.println("All warehouses are full");
                        }
                        for (Location loc : space) {
                            System.out.println("  → " + loc.describe());
                        }
                    } catch (SQLException e) {
                        System.err.println("DB error:" + e.getMessage());
                    }
                }
                case 6 -> {
                    try {
                        Location loc = pickLocation(dao, input);
                        if (loc == null) break;

                        System.out.println("\nFetching AI delivery insights for \"" + loc.getName() + "\"...");
                        String response = ai.getDeliveryInsights(loc);
                        printAIBox("AI Delivery Insights", response);

                    } catch (SQLException e) {
                        System.err.println("DB error:" + e.getMessage());
                    }
                }
                case 7 -> {
                    try {
                        List<Location> warehouses = dao.getWarehousesWithSpace();
                        if (warehouses.isEmpty()) {
                            System.out.println("no warehouses in DB");
                            break;
                        }
                        System.out.println("\nSelect a warehouse:");
                        for (int i = 0; i < warehouses.size(); i++) {
                            System.out.println((i + 1) + ". " + warehouses.get(i));
                        }
                        System.out.print("Enter number: ");
                        int idx = Integer.parseInt(input.nextLine().trim()) - 1;

                        if (idx < 0 || idx >= warehouses.size()) {
                            System.out.println("Invalid choice.");
                            break;
                        }
                        Warehouse wh = (Warehouse) warehouses.get(idx);
                        System.out.println("\nFetching restocking advice for \"" + wh.getName() + "\"...");
                        String response = ai.getRestockingAdvice(wh);
                        printAIBox("AI Restocking Recommendation", response);

                    } catch (SQLException e) {
                        System.err.println("DB error: " + e.getMessage());
                    }
                }
                case 8 -> {
                    try {
                        List<Location> dps = dao.getHighPriorityDeliveryPoints();
                        List<Location> all = dao.getAllLocations();
                        List<DeliveryPoint> dpList = new ArrayList<>();
                        for (Location loc : all) {
                            if (loc instanceof DeliveryPoint) {
                                dpList.add((DeliveryPoint) loc);
                            }
                        }
                        if (dpList.size() < 2) {
                            System.out.println("Need at least 2 delivery points in DB.");
                            break;
                        }
                        System.out.println("\nPick first delivery point:");
                        for (int i = 0; i < dpList.size(); i++) {
                            System.out.println((i + 1) + ". " + dpList.get(i));
                        }
                        System.out.print("Enter number: ");
                        int idx1 = Integer.parseInt(input.nextLine().trim()) - 1;

                        System.out.println("\nPick second delivery point:");
                        for (int i = 0; i < dpList.size(); i++) {
                            System.out.println((i + 1) + ". " + dpList.get(i));
                        }
                        System.out.print("Enter number: ");
                        int idx2 = Integer.parseInt(input.nextLine().trim()) - 1;

                        if (idx1 == idx2 || idx1 < 0 || idx2 < 0
                                || idx1 >= dpList.size() || idx2 >= dpList.size()) {
                            System.out.println("Invalid selection.");
                            break;
                        }
                        DeliveryPoint dp1 = dpList.get(idx1);
                        DeliveryPoint dp2 = dpList.get(idx2);
                        System.out.println("\nComparing: " + dp1.getName() + " vs " + dp2.getName() + "...");
                        String response = ai.comparePriority(dp1, dp2);
                        printAIBox("AI Priority Comparison", response);
                    } catch (SQLException e) {
                        System.err.println("DB error: " + e.getMessage());
                    }
                }
                case 9 -> {
                    try {
                        List<Location> sorted = dao.getAllSortedByDistance();
                        System.out.println("\nPlanning route for " + sorted.size() + " locations");
                        String response = ai.planRoute(sorted);
                        printAIBox("AI Route Plan", response);
                    } catch (SQLException e) {
                        System.err.println("DB error: " + e.getMessage());
                    }
                }
                case 10 -> {
                    try {
                        logDAO.printAllHistory();
                    } catch (SQLException e) {
                        System.err.println("DB error: " + e.getMessage());
                    }
                }
                case 11 -> System.out.println("Goodbye");
                default -> System.out.println("Invalid option. Choose 1-11.");
            }

        } while (choice != 11);
        DBConnection.close();
        input.close();
    }

    private static void printMenu() {
        System.out.println("\n========== Delivery System Menu ==========");
        System.out.println("1.  Display all locations");
        System.out.println("2.  Add a new location");
        System.out.println("3.  Sort locations by distance");
        System.out.println("4.  Filter: High priority delivery points");
        System.out.println("5.  Filter: Warehouses with available space");
        System.out.println("--- AI Features ---");
        System.out.println("6.  Get AI delivery insights");
        System.out.println("7.  AI: Warehouse restocking recommendation");
        System.out.println("8.  AI: Delivery priority comparison");
        System.out.println("9.  AI: Delivery route planning");
        System.out.println("10. View AI query history (DB log)");
        System.out.println("11. Exit");
        System.out.println("==========================================");
    }

    private static Location pickLocation(LocationDAO dao, Scanner input) throws SQLException {
        List<Location> all = dao.getAllLocations();
        System.out.println("\nAvailable locations:");
        for (int i = 0; i < all.size(); i++) {
            System.out.println((i + 1) + ". " + all.get(i));
        }
        System.out.print("Select a location: ");
        int idx = Integer.parseInt(input.nextLine().trim()) - 1;
        if (idx < 0 || idx >= all.size()) {
            System.out.println("Invalid choice.");
            return null;
        }
        return all.get(idx);
    }
    private static void addLocation(LocationDAO dao, Scanner input) throws SQLException {
        System.out.println("\nWhat type of location?");
        System.out.println("1. General Location");
        System.out.println("2. Delivery Point");
        System.out.println("3. Warehouse");
        System.out.print("Choose: ");
        int type = Integer.parseInt(input.nextLine().trim());
        System.out.print("Code (e.g. DP_10): ");
        String code = input.nextLine().trim();
        System.out.print("Name: ");
        String name = input.nextLine().trim();
        System.out.print("Distance from HQ (km): ");
        double dist = Double.parseDouble(input.nextLine().trim());
        Location loc;
        switch (type) {
            case 2 -> {
                System.out.print("Priority (LOW/MEDIUM/HIGH): ");
                String priority = input.nextLine().trim().toUpperCase();
                loc = new DeliveryPoint(0, code, name, dist, priority);
            }
            case 3 -> {
                System.out.print("Max capacity: ");
                int max = Integer.parseInt(input.nextLine().trim());
                System.out.print("Current load: ");
                int current = Integer.parseInt(input.nextLine().trim());
                loc = new Warehouse(0, code, name, dist, current, max);
            }
            default -> loc = new GeneralLocation(0, code, name, dist);
        }
        dao.addLocation(loc);
        System.out.println("Location added successfully!");
    }

    private static void printAIBox(String title, String response) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  " + title);
        System.out.println("=".repeat(50));
        System.out.println(response);
        System.out.println("=".repeat(50));
    }
}