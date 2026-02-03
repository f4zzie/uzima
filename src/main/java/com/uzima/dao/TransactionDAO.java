package com.uzima.dao;

import com.uzima.model.Transaction;
import com.uzima.util.DatabaseConnection;
import com.uzima.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Transaction operations
 */
public class TransactionDAO {
    private static final Logger logger = LoggerFactory.getLogger(TransactionDAO.class);
    
    /**
     * Generate unique transaction number
     */
    public String generateTransactionNo() {
        String prefix = "TXN-";
        String datePart = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        
        String sql = "SELECT MAX(CAST(SUBSTRING(transaction_no, 13) AS UNSIGNED)) as max_num " +
                    "FROM transactions WHERE transaction_no LIKE ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, prefix + datePart + "%");
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int maxNum = rs.getInt("max_num");
                return prefix + datePart + "-" + String.format("%04d", maxNum + 1);
            }
            
            return prefix + datePart + "-0001";
            
        } catch (SQLException e) {
            logger.error("Error generating transaction number", e);
            return prefix + datePart + "-0001";
        }
    }
    
    /**
     * Save transaction
     */
    public boolean saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (transaction_no, client_id, transaction_date, " +
                    "drilling_type_id, borehole_depth, drilling_cost, pump_type_id, tank_height, " +
                    "pump_cost, tank_type_id, tank_cost, has_plumbing, plumbing_cost, " +
                    "survey_fee, authority_fee, subtotal, tax_amount, grand_total, " +
                    "payment_status, amount_paid, balance, job_status, notes, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, transaction.getTransactionNo());
            stmt.setString(2, transaction.getClientId());
            stmt.setDate(3, new java.sql.Date(transaction.getTransactionDate().getTime()));
            
            // Drilling
            if (transaction.getDrillingTypeId() > 0) {
                stmt.setInt(4, transaction.getDrillingTypeId());
                stmt.setDouble(5, transaction.getBoreholeDepth());
                stmt.setDouble(6, transaction.getDrillingCost());
            } else {
                stmt.setNull(4, Types.INTEGER);
                stmt.setNull(5, Types.DOUBLE);
                stmt.setDouble(6, 0);
            }
            
            // Pump
            if (transaction.getPumpTypeId() > 0) {
                stmt.setInt(7, transaction.getPumpTypeId());
                stmt.setDouble(8, transaction.getTankHeight());
                stmt.setDouble(9, transaction.getPumpCost());
            } else {
                stmt.setNull(7, Types.INTEGER);
                stmt.setNull(8, Types.DOUBLE);
                stmt.setDouble(9, 0);
            }
            
            // Tank
            if (transaction.getTankTypeId() > 0) {
                stmt.setInt(10, transaction.getTankTypeId());
                stmt.setDouble(11, transaction.getTankCost());
            } else {
                stmt.setNull(10, Types.INTEGER);
                stmt.setDouble(11, 0);
            }
            
            // Plumbing
            stmt.setBoolean(12, transaction.isHasPlumbing());
            stmt.setDouble(13, transaction.getPlumbingCost());
            
            // Fees and totals
            stmt.setDouble(14, transaction.getSurveyFee());
            stmt.setDouble(15, transaction.getAuthorityFee());
            stmt.setDouble(16, transaction.getSubtotal());
            stmt.setDouble(17, transaction.getTaxAmount());
            stmt.setDouble(18, transaction.getGrandTotal());
            stmt.setString(19, transaction.getPaymentStatus());
            stmt.setDouble(20, transaction.getAmountPaid());
            stmt.setDouble(21, transaction.getBalance());
            stmt.setString(22, transaction.getJobStatus());
            stmt.setString(23, transaction.getNotes());
            
            // Handle created_by - use current user ID if available (staff), otherwise null (customer)
            if (SessionManager.getInstance().getCurrentUser() != null) {
                stmt.setInt(24, SessionManager.getInstance().getCurrentUser().getUserId());
            } else {
                stmt.setNull(24, Types.INTEGER);
            }
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    transaction.setTransactionId(generatedKeys.getInt(1));
                }
                logger.info("Transaction saved: {}", transaction.getTransactionNo());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error saving transaction", e);
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get drilling types
     */
    public List<String[]> getDrillingTypes() {
        List<String[]> types = new ArrayList<>();
        String sql = "SELECT drilling_type_id, drilling_name, down_payment FROM drilling_types WHERE is_active = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                types.add(new String[]{
                    String.valueOf(rs.getInt("drilling_type_id")),
                    rs.getString("drilling_name"),
                    String.valueOf(rs.getDouble("down_payment"))
                });
            }
            
        } catch (SQLException e) {
            logger.error("Error fetching drilling types", e);
        }
        
        return types;
    }
    
    /**
     * Get pump types
     */
    public List<String[]> getPumpTypes() {
        List<String[]> types = new ArrayList<>();
        String sql = "SELECT pump_type_id, pump_name, base_cost FROM pump_types WHERE is_active = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                types.add(new String[]{
                    String.valueOf(rs.getInt("pump_type_id")),
                    rs.getString("pump_name"),
                    String.valueOf(rs.getDouble("base_cost"))
                });
            }
            
        } catch (SQLException e) {
            logger.error("Error fetching pump types", e);
        }
        
        return types;
    }
    
    /**
     * Get tank types
     */
    public List<String[]> getTankTypes() {
        List<String[]> types = new ArrayList<>();
        String sql = "SELECT tank_type_id, capacity_liters, base_cost FROM tank_types WHERE is_active = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                types.add(new String[]{
                    String.valueOf(rs.getInt("tank_type_id")),
                    String.valueOf(rs.getInt("capacity_liters")),
                    String.valueOf(rs.getDouble("base_cost"))
                });
            }
            
        } catch (SQLException e) {
            logger.error("Error fetching tank types", e);
        }
        
        return types;
    }
    
    /**
     * Get survey and authority fees for client category
     */
    public double[] getSurveyAuthorityFees(String clientCategory) {
        String sql = "SELECT survey_fee, authority_fee FROM survey_authority_fees WHERE client_category = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, clientCategory);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new double[]{
                    rs.getDouble("survey_fee"),
                    rs.getDouble("authority_fee")
                };
            }
            
        } catch (SQLException e) {
            logger.error("Error fetching fees", e);
        }
        
        return new double[]{0, 0};
    }
    
    /**
     * Calculate depth/height cost based on tiered pricing
     */
    public static double calculateDepthCost(double depth) throws SQLException {
        double total = 0;
        
        if (depth <= 0) return 0;
        
        if (depth <= 100) {
            total = depth * 1000;
        } else if (depth <= 200) {
            total = (100 * 1000) + ((depth - 100) * 1500);
        } else if (depth <= 300) {
            total = (100 * 1000) + (100 * 1500) + ((depth - 200) * 2000);
        } else {
            total = (100 * 1000) + (100 * 1500) + (100 * 2000) + ((depth - 300) * 2500);
        }
        
        return total;
    }
    
    /**
     * Get all transactions
     */
    public static List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        
        return transactions;
    }
    
    /**
     * Get transactions by client ID
     */
    public static List<Transaction> getTransactionsByClientId(String clientId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE client_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, clientId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        
        return transactions;
    }
    
    /**
     * Get transaction by ID
     */
    public static Transaction getTransactionById(int transactionId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToTransaction(rs);
            }
        }
        
        return null;
    }
    
    /**
     * Get total transaction count
     */
    public static int getTotalTransactionCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM transactions";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        
        return 0;
    }
    
    /**
     * Get transaction count by payment status
     */
    public static int getTransactionCountByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM transactions WHERE payment_status = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        
        return 0;
    }
    
    /**
     * Update payment for a transaction
     */
    public static boolean updatePayment(int transactionId, double amountPaid) throws SQLException {
        String sql = "UPDATE transactions SET amount_paid = amount_paid + ?, " +
                    "balance = grand_total - (amount_paid + ?), " +
                    "payment_status = CASE " +
                    "  WHEN (amount_paid + ?) >= grand_total THEN 'PAID' " +
                    "  WHEN (amount_paid + ?) > 0 THEN 'PARTIALLY_PAID' " +
                    "  ELSE 'NOT_PAID' END, " +
                    "updated_at = CURRENT_TIMESTAMP " +
                    "WHERE transaction_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, amountPaid);
            stmt.setDouble(2, amountPaid);
            stmt.setDouble(3, amountPaid);
            stmt.setDouble(4, amountPaid);
            stmt.setInt(5, transactionId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Update transaction job status
     */
    public boolean updateTransactionStatus(String transactionNo, String newStatus) {
        String sql = "UPDATE transactions SET job_status = ?, updated_at = NOW() WHERE transaction_no = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newStatus);
            stmt.setString(2, transactionNo);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                logger.info("Transaction {} status updated to {}", transactionNo, newStatus);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error updating transaction status", e);
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Map ResultSet to Transaction object
     */
    private static Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setTransactionNo(rs.getString("transaction_no"));
        t.setClientId(rs.getString("client_id"));
        t.setTransactionDate(rs.getDate("transaction_date"));
        t.setDrillingTypeId(rs.getInt("drilling_type_id"));
        t.setBoreholeDepth(rs.getDouble("borehole_depth"));
        t.setDrillingCost(rs.getDouble("drilling_cost"));
        t.setPumpTypeId(rs.getInt("pump_type_id"));
        t.setTankHeight(rs.getDouble("tank_height"));
        t.setPumpCost(rs.getDouble("pump_cost"));
        t.setTankTypeId(rs.getInt("tank_type_id"));
        t.setTankCost(rs.getDouble("tank_cost"));
        t.setHasPlumbing(rs.getBoolean("has_plumbing"));
        t.setPlumbingCost(rs.getDouble("plumbing_cost"));
        t.setSurveyFee(rs.getDouble("survey_fee"));
        t.setAuthorityFee(rs.getDouble("authority_fee"));
        t.setSubtotal(rs.getDouble("subtotal"));
        t.setTaxAmount(rs.getDouble("tax_amount"));
        t.setGrandTotal(rs.getDouble("grand_total"));
        t.setPaymentStatus(rs.getString("payment_status"));
        t.setAmountPaid(rs.getDouble("amount_paid"));
        t.setBalance(rs.getDouble("balance"));
        t.setJobStatus(rs.getString("job_status"));
        t.setNotes(rs.getString("notes"));
        // created_by is varchar in DB, parse to int (or default to 0)
        String createdByStr = rs.getString("created_by");
        t.setCreatedBy(createdByStr != null && !createdByStr.isEmpty() ? Integer.parseInt(createdByStr) : 0);
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setUpdatedAt(rs.getTimestamp("updated_at"));
        return t;
    }
}
