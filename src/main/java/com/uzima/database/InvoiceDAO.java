package com.uzima.database;

import com.uzima.models.Invoice;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Invoice operations
 */
public class InvoiceDAO {

    /**
     * Create a new invoice in the database
     * @param invoice The invoice object to insert
     * @return true if successful, false otherwise
     */
    public static boolean createInvoice(Invoice invoice) {
        String sql = "INSERT INTO invoices (request_id, client_id, survey_fee, local_authority_fee, drilling_cost, pump_cost, plumbing_cost, tank_cost, depth_height_charge, subtotal, tax_amount, total_amount, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, invoice.getRequestId());
            pstmt.setInt(2, invoice.getClientId());
            pstmt.setDouble(3, invoice.getSurveyFee());
            pstmt.setDouble(4, invoice.getLocalAuthorityFee());
            pstmt.setDouble(5, invoice.getDrillingCost());
            pstmt.setDouble(6, invoice.getPumpCost());
            pstmt.setDouble(7, invoice.getPlumbingCost());
            pstmt.setDouble(8, invoice.getTankCost());
            pstmt.setDouble(9, invoice.getDepthHeightCharge());
            pstmt.setDouble(10, invoice.getSubtotal());
            pstmt.setDouble(11, invoice.getTaxAmount());
            pstmt.setDouble(12, invoice.getTotalAmount());
            pstmt.setString(13, invoice.getPaymentStatus());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating invoice: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieve an invoice by ID
     * @param invoiceId The invoice ID
     * @return Invoice object or null if not found
     */
    public static Invoice getInvoiceById(int invoiceId) {
        String sql = "SELECT * FROM invoices WHERE invoice_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToInvoice(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving invoice: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieve all invoices for a client
     * @param clientId The client ID
     * @return List of invoices
     */
    public static List<Invoice> getInvoicesByClientId(int clientId) {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE client_id = ? ORDER BY invoice_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, clientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                invoices.add(mapResultSetToInvoice(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving invoices by client: " + e.getMessage());
        }
        return invoices;
    }

    /**
     * Retrieve all invoices
     * @return List of all invoices
     */
    public static List<Invoice> getAllInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM invoices ORDER BY invoice_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                invoices.add(mapResultSetToInvoice(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all invoices: " + e.getMessage());
        }
        return invoices;
    }

    /**
     * Update invoice payment status
     * @param invoiceId The invoice ID
     * @param paymentStatus The new payment status
     * @return true if successful, false otherwise
     */
    public static boolean updatePaymentStatus(int invoiceId, String paymentStatus) {
        String sql = "UPDATE invoices SET payment_status = ? WHERE invoice_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, paymentStatus);
            pstmt.setInt(2, invoiceId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get total revenue from all invoices
     * @return Total revenue amount
     */
    public static double getTotalRevenue() {
        String sql = "SELECT SUM(total_amount) as total FROM invoices WHERE payment_status = 'Paid'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total revenue: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get total tax collected
     * @return Total tax amount
     */
    public static double getTotalTaxCollected() {
        String sql = "SELECT SUM(tax_amount) as total FROM invoices WHERE payment_status = 'Paid'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total tax: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get revenue by service type
     * @return Total revenue from drilling services
     */
    public static double getRevenueFromDrillingServices() {
        String sql = "SELECT SUM(drilling_cost) as total FROM invoices WHERE payment_status = 'Paid'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating drilling revenue: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Map a ResultSet row to an Invoice object
     */
    private static Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        return new Invoice(
            rs.getInt("invoice_id"),
            rs.getInt("request_id"),
            rs.getInt("client_id"),
            rs.getDouble("survey_fee"),
            rs.getDouble("local_authority_fee"),
            rs.getDouble("drilling_cost"),
            rs.getDouble("pump_cost"),
            rs.getDouble("plumbing_cost"),
            rs.getDouble("tank_cost"),
            rs.getDouble("depth_height_charge"),
            rs.getDouble("subtotal"),
            rs.getDouble("tax_amount"),
            rs.getDouble("total_amount"),
            rs.getTimestamp("invoice_date").toLocalDateTime(),
            rs.getString("payment_status")
        );
    }
}
