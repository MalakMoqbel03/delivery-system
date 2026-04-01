package com.ubs.delivery.dao;

import com.ubs.delivery.model.*;
import com.ubs.delivery.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
//this class handles all DB operations for locations and only this class talks to the database

public class LocationDAO {

     // all SQL is written here, and if we change DB later (for example to Postgress) we will update only this file
    private final Connection conn;
    public LocationDAO() {
        this.conn = DBConnection.getConnection();
    }
    public List<Location> getAllLocations() throws SQLException {
        List<Location> list = new ArrayList<>();
        String sql = "SELECT * FROM locations ORDER BY id";
        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }
    public List<Location> getAllSortedByDistance() throws SQLException {
        List<Location> list = new ArrayList<>();
        String sql = "SELECT * FROM locations ORDER BY distance_km ASC";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);

        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }
    public List<Location> getHighPriorityDeliveryPoints() throws SQLException {
        List<Location> list = new ArrayList<>();
        String sql = "SELECT * FROM locations WHERE type = 'DP' AND priority = 'HIGH'";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);

        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }
    public List<Location> getWarehousesWithSpace() throws SQLException {
        List<Location> list = new ArrayList<>();
        String sql = "SELECT * FROM locations WHERE type = 'WH' AND current_load < max_capacity";

        Statement stmt = conn.createStatement();
        ResultSet rs   = stmt.executeQuery(sql);

        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }
    public void addLocation(Location loc) throws SQLException {
        String sql = "INSERT INTO locations (code, name, type, distance_km, priority, current_load, max_capacity) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, loc.getCode());
        ps.setString(2, loc.getName());
        ps.setString(3, loc.getType());
        ps.setDouble(4, loc.getDistanceKm());
        if (loc instanceof DeliveryPoint) {
            DeliveryPoint dp = (DeliveryPoint) loc;
            ps.setString(5, dp.getPriority());
            ps.setNull(6, Types.INTEGER);
            ps.setNull(7, Types.INTEGER);
        } else if (loc instanceof Warehouse) {
            Warehouse wh = (Warehouse) loc;
            ps.setNull(5, Types.VARCHAR);
            ps.setInt(6, wh.getCurrentLoad());
            ps.setInt(7, wh.getMaxCapacity());
        } else {
            ps.setNull(5, Types.VARCHAR);
            ps.setNull(6, Types.INTEGER);
            ps.setNull(7, Types.INTEGER);
        }

        ps.executeUpdate();
        System.out.println("[DB] Location added: " + loc.getName());
    }

     // Converts a DB row into the correct Java object, so uses the "type" column to decide which class to create
    private Location mapRow(ResultSet rs) throws SQLException {
        int    id         = rs.getInt("id");
        String code       = rs.getString("code");
        String name       = rs.getString("name");
        String type       = rs.getString("type");
        double distanceKm = rs.getDouble("distance_km");
        switch (type) {
            case "DP": {
                String priority = rs.getString("priority");
                return new DeliveryPoint(id, code, name, distanceKm, priority);
            }
            case "WH": {
                int currentLoad = rs.getInt("current_load");
                int maxCapacity = rs.getInt("max_capacity");
                return new Warehouse(id, code, name, distanceKm, currentLoad, maxCapacity);
            }
            default: {
                return new GeneralLocation(id, code, name, distanceKm);
            }
        }
    }
}