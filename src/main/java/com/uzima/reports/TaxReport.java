package com.uzima.reports;

import com.uzima.models.Invoice;
import com.uzima.services.InvoiceService;
import java.util.List;

/**
 * Generates tax-related reports
 */
public class TaxReport {

    /**
     * Generate a report of total tax collected
     * @return Formatted report string
     */
    public static String generateTaxSummaryReport() {
        double totalTax = InvoiceService.getTotalTaxCollected();
        double totalRevenue = InvoiceService.getTotalRevenue();
        double subTotal = totalRevenue > 0 ? totalRevenue / 1.16 : 0;
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("                   TAX SUMMARY REPORT\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");
        report.append(String.format("Subtotal (before tax):   KES %.2f\n", subTotal));
        report.append(String.format("Tax Rate:                16%%\n"));
        report.append(String.format("Total Tax Collected:     KES %.2f\n", totalTax));
        report.append(String.format("Total Revenue:           KES %.2f\n", totalRevenue));
        report.append("═══════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    /**
     * Generate tax breakdown by invoice
     * @return Formatted report string
     */
    public static String generateTaxBreakdownReport() {
        List<Invoice> invoices = InvoiceService.getAllInvoices();
        double totalTax = 0;
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("               TAX BREAKDOWN BY INVOICE\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");
        report.append(String.format("%-10s %-10s %-15s %-15s %-15s\n", 
            "Invoice#", "Client ID", "Subtotal", "Tax (16%)", "Total"));
        report.append("─────────────────────────────────────────────────────────────\n");
        
        for (Invoice invoice : invoices) {
            report.append(String.format("%-10d %-10d KES %-13.2f KES %-13.2f KES %-13.2f\n",
                invoice.getInvoiceId(),
                invoice.getClientId(),
                invoice.getSubtotal(),
                invoice.getTaxAmount(),
                invoice.getTotalAmount()));
            totalTax += invoice.getTaxAmount();
        }
        
        report.append("─────────────────────────────────────────────────────────────\n");
        report.append(String.format("%-10s %-10s %-15s %-15s %-15s\n", 
            "", "", "", "TOTAL:", ""));
        report.append(String.format("%-38s KES %.2f\n", "", totalTax));
        report.append("═══════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    /**
     * Generate tax report for a specific client
     * @param clientId The client ID
     * @return Formatted report string
     */
    public static String generateClientTaxReport(int clientId) {
        List<Invoice> invoices = InvoiceService.getClientInvoices(clientId);
        double totalTax = 0;
        double totalAmount = 0;
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("              CLIENT TAX REPORT\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");
        report.append(String.format("Client ID: %d\n\n", clientId));
        report.append(String.format("%-10s %-12s %-12s %-12s\n", 
            "Invoice#", "Subtotal", "Tax (16%)", "Total"));
        report.append("─────────────────────────────────────────────────────────────\n");
        
        for (Invoice invoice : invoices) {
            report.append(String.format("%-10d KES %-11.2f KES %-11.2f KES %-11.2f\n",
                invoice.getInvoiceId(),
                invoice.getSubtotal(),
                invoice.getTaxAmount(),
                invoice.getTotalAmount()));
            totalTax += invoice.getTaxAmount();
            totalAmount += invoice.getTotalAmount();
        }
        
        report.append("─────────────────────────────────────────────────────────────\n");
        report.append(String.format("Total Tax Paid:          KES %.2f\n", totalTax));
        report.append(String.format("Total Amount Paid:       KES %.2f\n", totalAmount));
        report.append("═══════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
}
