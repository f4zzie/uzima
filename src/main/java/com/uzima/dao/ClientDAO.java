package com.uzima.dao;

import com.uzima.model.Client;
import com.uzima.util.DatabaseConnection;
import com.uzima.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Client operations
 */
public class ClientDAO {
    private static final Logger logger = LoggerFactory.getLogger(ClientDAO.class);
    
    /**
     * Generate unique client ID in simple incremental format (uzm1, uzm2, uzm3...)
     */
    public String generateClientId(String category) {
        String sql = "SELECT MAX(CAST(SUBSTRING(client_id, 4) AS UNSIGNED)) as max_num " +
                    "FROM clients WHERE client_id LIKE 'uzm%'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int maxNum = rs.getInt("max_num");
                return "uzm" + (maxNum + 1);
            }
            
            return "uzm1";
            
        } catch (SQLException e) {
            logger.error("Error generating client ID", e);
            // Fallback: try to count total clients and add 1
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) as total FROM clients")) {
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next()) {
                    int total = countRs.getInt("total");
                    return "uzm" + (total + 1);
                }
            } catch (SQLException ex) {
                logger.error("Error in fallback client ID generation", ex);
            }
            return "uzm1";
        }
    }
    
    /**
     * Add new client
     */
    public boolean addClient(Client client) {
        String sql = "INSERT INTO clients (client_id, full_name, physical_address, phone_number, " +
                    "phone_number_2, email, client_category, borehole_location, county, " +
                    "gps_coordinates, id_number, registration_date, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, client.getClientId());
            stmt.setString(2, client.getFullName());
            stmt.setString(3, client.getPhysicalAddress());
            stmt.setString(4, client.getPhoneNumber());
            stmt.setString(5, client.getPhoneNumber2());
            stmt.setString(6, client.getEmail());
            stmt.setString(7, client.getClientCategory());
            stmt.setString(8, client.getBoreholeLocation());
            stmt.setString(9, client.getCounty());
            stmt.setString(10, client.getGpsCoordinates());
            stmt.setString(11, client.getIdNumber());
            stmt.setDate(12, new java.sql.Date(client.getRegistrationDate().getTime()));
            stmt.setInt(13, SessionManager.getInstance().getCurrentUser().getUserId());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                logger.info("Client added: {}", client.getClientId());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error adding client", e);
        }
        
        return false;
    }
    
    /**
     * Update client
     */
    public boolean updateClient(Client client) {
        String sql = "UPDATE clients SET full_name = ?, physical_address = ?, phone_number = ?, " +
                    "phone_number_2 = ?, email = ?, client_category = ?, borehole_location = ?, " +
                    "county = ?, gps_coordinates = ?, id_number = ? WHERE client_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, client.getFullName());
            stmt.setString(2, client.getPhysicalAddress());
            stmt.setString(3, client.getPhoneNumber());
            stmt.setString(4, client.getPhoneNumber2());
            stmt.setString(5, client.getEmail());
            stmt.setString(6, client.getClientCategory());
            stmt.setString(7, client.getBoreholeLocation());
            stmt.setString(8, client.getCounty());
            stmt.setString(9, client.getGpsCoordinates());
            stmt.setString(10, client.getIdNumber());
            stmt.setString(11, client.getClientId());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                logger.info("Client updated: {}", client.getClientId());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error updating client", e);
        }
        
        return false;
    }
    
    /**
     * Delete client
     */
    public boolean deleteClient(String clientId) {
        String sql = "DELETE FROM clients WHERE client_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, clientId);
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                logger.info("Client deleted: {}", clientId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error deleting client", e);
        }
        
        return false;
    }
    
    /**
     * Get all clients
     */
    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients WHERE is_active = TRUE ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error fetching clients", e);
        }
        
        return clients;
    }
    
    /**
     * Search clients
     */
    public List<Client> searchClients(String searchTerm) {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients WHERE is_active = TRUE AND " +
                    "(client_id LIKE ? OR full_name LIKE ? OR phone_number LIKE ?) " +
                    "ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error searching clients", e);
        }
        
        return clients;
    }
    
    /**
     * Get client by ID
     */
    public Client getClientById(String clientId) {
        String sql = "SELECT * FROM clients WHERE client_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, clientId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToClient(rs);
            }
            
        } catch (SQLException e) {
            logger.error("Error fetching client", e);
        }
        
        return null;
    }
    
    /**
     * Get clients by category
     */
    public List<Client> getClientsByCategory(String category) {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients WHERE client_category = ? AND is_active = TRUE " +
                    "ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error fetching clients by category", e);
        }
        
        return clients;
    }
    
    /**
     * Get total client count
     */
    public int getTotalClientCount() {
        String sql = "SELECT COUNT(*) as count FROM clients WHERE is_active = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            
        } catch (SQLException e) {
            logger.error("Error getting client count", e);
        }
        
        return 0;
    }
    
    /**
     * Authenticate customer by client_id and password
     */
    public Client authenticateClient(String clientId, String password) {
        String sql = "SELECT * FROM clients WHERE client_id = ? AND password = ? AND is_active = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, clientId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                logger.info("Customer authenticated: {}", clientId);
                return mapResultSetToClient(rs);
            }
            
        } catch (SQLException e) {
            logger.error("Error authenticating customer", e);
        }
        
        return null;
    }
    
    /**
     * Register a new customer with password (self-registration)
     */
    public boolean registerCustomer(Client client) {
        String sql = "INSERT INTO clients (client_id, full_name, physical_address, phone_number, " +
                    "phone_number_2, email, client_category, borehole_location, county, " +
                    "gps_coordinates, id_number, registration_date, password) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, client.getClientId());
            stmt.setString(2, client.getFullName());
            stmt.setString(3, client.getPhysicalAddress());
            stmt.setString(4, client.getPhoneNumber());
            stmt.setString(5, client.getPhoneNumber2());
            stmt.setString(6, client.getEmail());
            stmt.setString(7, client.getClientCategory());
            stmt.setString(8, client.getBoreholeLocation());
            stmt.setString(9, client.getCounty());
            stmt.setString(10, client.getGpsCoordinates());
            stmt.setString(11, client.getIdNumber());
            stmt.setDate(12, new java.sql.Date(client.getRegistrationDate().getTime()));
            stmt.setString(13, client.getPassword());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                logger.info("Customer registered: {}", client.getClientId());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error registering customer: " + e.getMessage(), e);
            System.err.println("SQL Error: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
        }
        
        return false;
    }
    
    /**
     * Map ResultSet to Client object
     */
    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setClientId(rs.getString("client_id"));
        client.setFullName(rs.getString("full_name"));
        client.setPhysicalAddress(rs.getString("physical_address"));
        client.setPhoneNumber(rs.getString("phone_number"));
        client.setPhoneNumber2(rs.getString("phone_number_2"));
        client.setEmail(rs.getString("email"));
        client.setClientCategory(rs.getString("client_category"));
        client.setBoreholeLocation(rs.getString("borehole_location"));
        client.setCounty(rs.getString("county"));
        client.setGpsCoordinates(rs.getString("gps_coordinates"));
        client.setIdNumber(rs.getString("id_number"));
        client.setRegistrationDate(rs.getDate("registration_date"));
        client.setActive(rs.getBoolean("is_active"));
        client.setCreatedBy(rs.getInt("created_by"));
        client.setCreatedAt(rs.getTimestamp("created_at"));
        client.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        // Try to get password (may not exist in older schema)
        try {
            client.setPassword(rs.getString("password"));
        } catch (SQLException ignored) {}
        
        return client;
    }
}
