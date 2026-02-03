package com.uzima.dao;

import com.uzima.model.ServiceType;
import com.uzima.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Service Types (Drilling, Pumps, Tanks)
 */
public class ServiceDAO {
    
    // ==================== DRILLING TYPES ====================
    public List<ServiceType> getDrillingTypes() {
        List<ServiceType> types = new ArrayList<>();
        String sql = "SELECT * FROM drilling_types ORDER BY drilling_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ServiceType type = new ServiceType();
                type.setId(rs.getInt("drilling_type_id"));
                type.setName(rs.getString("drilling_name"));
                type.setDescription(rs.getString("description"));
                type.setBasePrice(rs.getBigDecimal("down_payment"));
                type.setActive(rs.getBoolean("is_active"));
                types.add(type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return types;
    }
    
    public boolean addDrillingType(ServiceType type) {
        String sql = "INSERT INTO drilling_types (drilling_name, description, down_payment, is_active) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, type.getName());
            pstmt.setString(2, type.getDescription());
            pstmt.setBigDecimal(3, type.getBasePrice());
            pstmt.setBoolean(4, type.isActive());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateDrillingType(ServiceType type) {
        String sql = "UPDATE drilling_types SET drilling_name=?, description=?, down_payment=?, is_active=? WHERE drilling_type_id=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, type.getName());
            pstmt.setString(2, type.getDescription());
            pstmt.setBigDecimal(3, type.getBasePrice());
            pstmt.setBoolean(4, type.isActive());
            pstmt.setInt(5, type.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteDrillingType(int id) {
        return deleteRecord("drilling_types", "drilling_type_id", id);
    }
    
    // ==================== PUMP TYPES ====================
    public List<ServiceType> getPumpTypes() {
        List<ServiceType> types = new ArrayList<>();
        String sql = "SELECT * FROM pump_types ORDER BY pump_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ServiceType type = new ServiceType();
                type.setId(rs.getInt("pump_type_id"));
                type.setName(rs.getString("pump_name"));
                type.setDescription(rs.getString("description"));
                type.setPrice(rs.getBigDecimal("base_cost"));
                type.setActive(rs.getBoolean("is_active"));
                types.add(type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return types;
    }
    
    public boolean addPumpType(ServiceType type) {
        String sql = "INSERT INTO pump_types (pump_name, description, base_cost, is_active) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, type.getName());
            pstmt.setString(2, type.getDescription());
            pstmt.setBigDecimal(3, type.getPrice());
            pstmt.setBoolean(4, type.isActive());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deletePumpType(int id) {
        return deleteRecord("pump_types", "pump_type_id", id);
    }
    
    // ==================== TANK TYPES ====================
    public List<ServiceType> getTankTypes() {
        List<ServiceType> types = new ArrayList<>();
        String sql = "SELECT * FROM tank_types ORDER BY capacity_liters";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ServiceType type = new ServiceType();
                type.setId(rs.getInt("tank_type_id"));
                type.setCapacity(rs.getInt("capacity_liters"));
                type.setDescription(rs.getString("description"));
                type.setPrice(rs.getBigDecimal("base_cost"));
                type.setActive(rs.getBoolean("is_active"));
                types.add(type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return types;
    }
    
    public boolean addTankType(ServiceType type) {
        String sql = "INSERT INTO tank_types (capacity_liters, description, base_cost, is_active) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, type.getCapacity());
            pstmt.setString(2, type.getDescription());
            pstmt.setBigDecimal(3, type.getPrice());
            pstmt.setBoolean(4, type.isActive());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteTankType(int id) {
        return deleteRecord("tank_types", "tank_type_id", id);
    }
    
    // ==================== HELPER METHODS ====================
    private boolean deleteRecord(String table, String idColumn, int id) {
        String sql = "DELETE FROM " + table + " WHERE " + idColumn + " = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
