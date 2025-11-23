package com.uzima.services;

import com.uzima.models.TaxCalculation;
import com.uzima.models.Invoice;
import com.uzima.database.InvoiceDAO;
import com.uzima.database.ServiceDAO;

/**
 * Business logic for Invoice and billing operations
 */
public class InvoiceService {

    /**
     * Calculate the depth/height charge for a service
     * @param depth The borehole depth in meters
     * @param height The tank height in meters
     * @return Total depth/height charge
     */
    public static double calculateDepthHeightCharge(int depth, int height) {
        double costPerMeter = ServiceDAO.getDepthHeightCharge(depth);
        return (depth + height) * costPerMeter;
    }

    /**
     * Calculate total drilling cost including down payment and depth charges
     * @param drillingType The drilling service type
     * @param depth The borehole depth
     * @param height The tank height
     * @return Total drilling cost
     */
    public static double calculateDrillingCost(String drillingType, int depth, int height) {
        double downPayment = 0;
        
        if (drillingType.equals("Symmetric")) {
            downPayment = 130000;
        } else if (drillingType.equals("Core")) {
            downPayment = 225000;
        } else if (drillingType.equals("Geo-Technical")) {
            downPayment = 335000;
        }
        
        double depthCharge = calculateDepthHeightCharge(depth, height);
        return downPayment + depthCharge;
    }

    /**
     * Calculate total plumbing cost
     * @param pipeLengthMeters Length of pipes in meters
     * @param costPerMeter Cost per meter
     * @return Total plumbing cost
     */
    public static double calculatePlumbingCost(int pipeLengthMeters, double costPerMeter) {
        return pipeLengthMeters * costPerMeter;
    }

    /**
     * Get fees for a client category
     * @param category The client category
     * @return Array with [surveyFee, localAuthorityFee]
     */
    public static double[] getCategoryFees(String category) {
        return ServiceDAO.getFeesByCategory(category);
    }

    /**
     * Create a new invoice
     * @param invoice The invoice object
     * @return true if creation successful, false otherwise
     */
    public static boolean createInvoice(Invoice invoice) {
        if (invoice.getSubtotal() < 0) {
            System.err.println("Invalid invoice subtotal");
            return false;
        }
        return InvoiceDAO.createInvoice(invoice);
    }

    /**
     * Get invoice by ID
     * @param invoiceId The invoice ID
     * @return Invoice object or null
     */
    public static Invoice getInvoiceById(int invoiceId) {
        return InvoiceDAO.getInvoiceById(invoiceId);
    }

    /**
     * Get all invoices for a client
     * @param clientId The client ID
     * @return List of invoices
     */
    public static java.util.List<Invoice> getClientInvoices(int clientId) {
        return InvoiceDAO.getInvoicesByClientId(clientId);
    }

    /**
     * Get all invoices
     * @return List of all invoices
     */
    public static java.util.List<Invoice> getAllInvoices() {
        return InvoiceDAO.getAllInvoices();
    }

    /**
     * Update invoice payment status
     * @param invoiceId The invoice ID
     * @param paymentStatus The new payment status
     * @return true if update successful, false otherwise
     */
    public static boolean updatePaymentStatus(int invoiceId, String paymentStatus) {
        if (!isValidPaymentStatus(paymentStatus)) {
            System.err.println("Invalid payment status");
            return false;
        }
        return InvoiceDAO.updatePaymentStatus(invoiceId, paymentStatus);
    }

    /**
     * Calculate total amount to be paid including tax
     * @param subtotal The subtotal before tax
     * @return Total amount with 16% tax
     */
    public static double calculateTotalWithTax(double subtotal) {
        return TaxCalculation.calculateTotal(subtotal);
    }

    /**
     * Calculate tax amount
     * @param subtotal The subtotal before tax
     * @return Tax amount (16% of subtotal)
     */
    public static double calculateTax(double subtotal) {
        return TaxCalculation.calculateTax(subtotal);
    }

    /**
     * Get total revenue
     * @return Total revenue amount
     */
    public static double getTotalRevenue() {
        return InvoiceDAO.getTotalRevenue();
    }

    /**
     * Get total tax collected
     * @return Total tax amount
     */
    public static double getTotalTaxCollected() {
        return InvoiceDAO.getTotalTaxCollected();
    }

    /**
     * Get revenue from drilling services
     * @return Total drilling revenue
     */
    public static double getDrillingRevenue() {
        return InvoiceDAO.getRevenueFromDrillingServices();
    }

    /**
     * Validate payment status
     */
    private static boolean isValidPaymentStatus(String status) {
        return status.equals("Pending") || status.equals("Partial") || 
               status.equals("Paid") || status.equals("Overdue");
    }
}
