package com.uzima.database;

import com.uzima.models.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Client operations
 */
public class ClientDAO {

    /**
     * Create a new client in the database
     * @param client The client object to insert
     * @return true if successful, false otherwise
     */
    public static boolean createClient(Client client) {
        String sql = "INSERT INTO clients (full_name, email, phone, address, category, borehole_location, password) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, client.getFullName());
            pstmt.setString(2, client.getEmail());
            pstmt.setString(3, client.getPhone());
            pstmt.setString(4, client.getAddress());
            pstmt.setString(5, client.getCategory());
            pstmt.setString(6, client.getBoreholeLocation());
            pstmt.setString(7, client.getPassword());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating client: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieve a client by ID
     * @param clientId The client ID
     * @return Client object or null if not found
     */
    public static Client getClientById(int clientId) {
        String sql = "SELECT * FROM clients WHERE client_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, clientId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToClient(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving client: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieve a client by email
     * @param email The client email
     * @return Client object or null if not found
     */
    public static Client getClientByEmail(String email) {
        String sql = "SELECT * FROM clients WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToClient(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving client by email: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieve all clients
     * @return List of all clients
     */
    public static List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all clients: " + e.getMessage());
        }
        return clients;
    }

    /**
     * Update a client's information
     * @param client The client object with updated data
     * @return true if successful, false otherwise
     */
    public static boolean updateClient(Client client) {
        String sql = "UPDATE clients SET full_name = ?, phone = ?, address = ?, category = ?, borehole_location = ? WHERE client_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, client.getFullName());
            pstmt.setString(2, client.getPhone());
            pstmt.setString(3, client.getAddress());
            pstmt.setString(4, client.getCategory());
            pstmt.setString(5, client.getBoreholeLocation());
            pstmt.setInt(6, client.getClientId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating client: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a client
     * @param clientId The client ID to delete
     * @return true if successful, false otherwise
     */
    public static boolean deleteClient(int clientId) {
        String sql = "DELETE FROM clients WHERE client_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, clientId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting client: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if client email exists
     * @param email The email to check
     * @return true if exists, false otherwise
     */
    public static boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM clients WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get clients by category
     * @param category The client category (Industrial, Commercial, Domestic)
     * @return List of clients in that category
     */
    public static List<Client> getClientsByCategory(String category) {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients WHERE category = ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving clients by category: " + e.getMessage());
        }
        return clients;
    }

    /**
     * Map a ResultSet row to a Client object
     */
    private static Client mapResultSetToClient(ResultSet rs) throws SQLException {
        return new Client(
            rs.getInt("client_id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("address"),
            rs.getString("category"),
            rs.getString("borehole_location"),
            rs.getString("password"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
