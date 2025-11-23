package com.uzima.database;

import com.uzima.models.DrillingService;
import com.uzima.models.PumpService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Service operations
 */
public class ServiceDAO {

    /**
     * Get all drilling services
     * @return List of all drilling services
     */
    public static List<DrillingService> getAllDrillingServices() {
        List<DrillingService> services = new ArrayList<>();
        String sql = "SELECT * FROM drilling_services";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                services.add(new DrillingService(
                    rs.getInt("drilling_id"),
                    rs.getString("service_type"),
                    rs.getDouble("down_payment_fee"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving drilling services: " + e.getMessage());
        }
        return services;
    }

    /**
     * Get drilling service by type
     * @param serviceType The drilling service type
     * @return DrillingService object or null
     */
    public static DrillingService getDrillingServiceByType(String serviceType) {
        String sql = "SELECT * FROM drilling_services WHERE service_type = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, serviceType);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new DrillingService(
                    rs.getInt("drilling_id"),
                    rs.getString("service_type"),
                    rs.getDouble("down_payment_fee"),
                    rs.getString("description")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving drilling service: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all pump services
     * @return List of all pump services
     */
    public static List<PumpService> getAllPumpServices() {
        List<PumpService> services = new ArrayList<>();
        String sql = "SELECT * FROM pump_services";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                services.add(new PumpService(
                    rs.getInt("pump_id"),
                    rs.getString("pump_type"),
                    rs.getDouble("cost"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving pump services: " + e.getMessage());
        }
        return services;
    }

    /**
     * Get pump service by type
     * @param pumpType The pump type
     * @return PumpService object or null
     */
    public static PumpService getPumpServiceByType(String pumpType) {
        String sql = "SELECT * FROM pump_services WHERE pump_type = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, pumpType);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new PumpService(
                    rs.getInt("pump_id"),
                    rs.getString("pump_type"),
                    rs.getDouble("cost"),
                    rs.getString("description")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving pump service: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get depth/height charge for a specific depth range
     * @param depth The depth in meters
     * @return The cost per meter for that depth, or 0 if not found
     */
    public static double getDepthHeightCharge(int depth) {
        String sql = "SELECT cost_per_meter FROM depth_height_charges WHERE depth_min <= ? AND depth_max >= ? LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, depth);
            pstmt.setInt(2, depth);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("cost_per_meter");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving depth/height charge: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get survey and local authority fees for a client category
     * @param category The client category
     * @return Array with [surveyFee, localAuthorityFee] or null if not found
     */
    public static double[] getFeesByCategory(String category) {
        String sql = "SELECT survey_fee, local_authority_fee FROM fees WHERE client_category = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new double[]{
                    rs.getDouble("survey_fee"),
                    rs.getDouble("local_authority_fee")
                };
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving fees by category: " + e.getMessage());
        }
        return new double[]{0, 0};
    }

    /**
     * Get all depth/height charges
     * @return List of all charges
     */
    public static List<Double> getAllDepthHeightCharges() {
        List<Double> charges = new ArrayList<>();
        String sql = "SELECT cost_per_meter FROM depth_height_charges ORDER BY depth_min";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                charges.add(rs.getDouble("cost_per_meter"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving depth/height charges: " + e.getMessage());
        }
        return charges;
    }
}
