package com.uzima.service;

import com.uzima.dao.TransactionDAO;
import com.uzima.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for Invoice and Transaction operations
 * Provides service methods for invoice generation, calculations, and reporting
 */
public class InvoiceService {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);
    private static final double TAX_RATE = 0.16; // 16% tax

    /**
     * Calculate the depth/height charge based on tiered pricing
     * Tier 1 (0-100m): KSh 1,000/meter
     * Tier 2 (101-200m): KSh 1,500/meter
     * Tier 3 (201-300m): KSh 2,000/meter
     * Tier 4 (301+m): KSh 2,500/meter
     * 
     * @param depth The borehole depth in meters
     * @return Total depth charge
     */
    public static double calculateDepthCost(double depth) {
        try {
            return TransactionDAO.calculateDepthCost(depth);
        } catch (SQLException e) {
            logger.error("Error calculating depth cost", e);
            // Fallback calculation
            if (depth <= 100) {
                return depth * 1000;
            } else if (depth <= 200) {
                return (100 * 1000) + ((depth - 100) * 1500);
            } else if (depth <= 300) {
                return (100 * 1000) + (100 * 1500) + ((depth - 200) * 2000);
            } else {
                return (100 * 1000) + (100 * 1500) + (100 * 2000) + ((depth - 300) * 2500);
            }
        }
    }

    /**
     * Calculate tax amount
     * @param subtotal The subtotal before tax
     * @return Tax amount (16% of subtotal)
     */
    public static double calculateTax(double subtotal) {
        return subtotal * TAX_RATE;
    }

    /**
     * Calculate grand total including tax
     * @param subtotal The subtotal before tax
     * @return Grand total (subtotal + tax)
     */
    public static double calculateGrandTotal(double subtotal) {
        return subtotal + calculateTax(subtotal);
    }

    /**
     * Get all transactions
     * @return List of all transactions
     */
    public static List<Transaction> getAllTransactions() {
        try {
            return TransactionDAO.getAllTransactions();
        } catch (SQLException e) {
            logger.error("Error fetching all transactions", e);
            return List.of();
        }
    }

    /**
     * Get transactions by client ID
     * @param clientId The client ID
     * @return List of transactions for the client
     */
    public static List<Transaction> getTransactionsByClient(String clientId) {
        try {
            return TransactionDAO.getTransactionsByClientId(clientId);
        } catch (SQLException e) {
            logger.error("Error fetching transactions for client: " + clientId, e);
            return List.of();
        }
    }

    /**
     * Get transaction by ID
     * @param transactionId The transaction ID
     * @return Transaction object or null
     */
    public static Transaction getTransactionById(int transactionId) {
        try {
            return TransactionDAO.getTransactionById(transactionId);
        } catch (SQLException e) {
            logger.error("Error fetching transaction: " + transactionId, e);
            return null;
        }
    }

    /**
     * Calculate total revenue from all paid transactions
     * @return Total revenue
     */
    public static double getTotalRevenue() {
        try {
            double total = 0.0;
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            for (Transaction t : transactions) {
                if ("PAID".equals(t.getPaymentStatus())) {
                    total += t.getGrandTotal();
                }
            }
            return total;
        } catch (SQLException e) {
            logger.error("Error calculating total revenue", e);
            return 0.0;
        }
    }

    /**
     * Calculate total tax collected from paid transactions
     * @return Total tax amount
     */
    public static double getTotalTaxCollected() {
        try {
            double total = 0.0;
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            for (Transaction t : transactions) {
                if ("PAID".equals(t.getPaymentStatus())) {
                    total += t.getTaxAmount();
                }
            }
            return total;
        } catch (SQLException e) {
            logger.error("Error calculating total tax", e);
            return 0.0;
        }
    }

    /**
     * Get total number of transactions
     * @return Transaction count
     */
    public static int getTotalTransactionCount() {
        try {
            return TransactionDAO.getTotalTransactionCount();
        } catch (SQLException e) {
            logger.error("Error getting transaction count", e);
            return 0;
        }
    }

    /**
     * Get count of transactions by payment status
     * @param status Payment status (NOT_PAID, PARTIALLY_PAID, PAID)
     * @return Count of transactions with given status
     */
    public static int getTransactionCountByStatus(String status) {
        try {
            return TransactionDAO.getTransactionCountByStatus(status);
        } catch (SQLException e) {
            logger.error("Error getting transaction count by status: " + status, e);
            return 0;
        }
    }

    /**
     * Update transaction payment status
     * @param transactionId The transaction ID
     * @param amountPaid The amount paid
     * @return true if update successful
     */
    public static boolean updatePayment(int transactionId, double amountPaid) {
        try {
            return TransactionDAO.updatePayment(transactionId, amountPaid);
        } catch (SQLException e) {
            logger.error("Error updating payment for transaction: " + transactionId, e);
            return false;
        }
    }

    /**
     * Get outstanding balance across all transactions
     * @return Total outstanding amount
     */
    public static double getTotalOutstandingBalance() {
        try {
            double total = 0.0;
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            for (Transaction t : transactions) {
                if (!"PAID".equals(t.getPaymentStatus())) {
                    total += t.getBalance();
                }
            }
            return total;
        } catch (SQLException e) {
            logger.error("Error calculating outstanding balance", e);
            return 0.0;
        }
    }
}
