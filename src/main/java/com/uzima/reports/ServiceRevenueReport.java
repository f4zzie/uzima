package com.uzima.reports;

import com.uzima.models.Invoice;
import com.uzima.services.InvoiceService;
import java.util.List;

/**
 * Generates revenue reports for services and company
 */
public class ServiceRevenueReport {

    /**
     * Generate revenue report for all services
     * @return Formatted report string
     */
    public static String generateServiceRevenueReport() {
        List<Invoice> invoices = InvoiceService.getAllInvoices();
        
        double drillingRevenue = 0;
        double pumpRevenue = 0;
        double plumbingRevenue = 0;
        double tankRevenue = 0;
        double surveyAndLocalFeeRevenue = 0;
        
        for (Invoice invoice : invoices) {
            if (invoice.getPaymentStatus().equals("Paid")) {
                drillingRevenue += invoice.getDrillingCost();
                pumpRevenue += invoice.getPumpCost();
                plumbingRevenue += invoice.getPlumbingCost();
                tankRevenue += invoice.getTankCost();
                surveyAndLocalFeeRevenue += invoice.getSurveyFee() + invoice.getLocalAuthorityFee();
            }
        }
        
        double totalServiceRevenue = drillingRevenue + pumpRevenue + plumbingRevenue + tankRevenue;
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("           SERVICE REVENUE BREAKDOWN REPORT\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");
        report.append("Revenue by Service Type:\n");
        report.append("─────────────────────────────────────────────────────────────\n");
        report.append(String.format("  Drilling Services:      KES %,.2f\n", drillingRevenue));
        report.append(String.format("  Pump Installation:      KES %,.2f\n", pumpRevenue));
        report.append(String.format("  Plumbing Services:      KES %,.2f\n", plumbingRevenue));
        report.append(String.format("  Tank Installation:      KES %,.2f\n", tankRevenue));
        report.append(String.format("  Survey & Local Fees:    KES %,.2f\n", surveyAndLocalFeeRevenue));
        report.append("─────────────────────────────────────────────────────────────\n");
        report.append(String.format("  TOTAL SERVICE REVENUE:  KES %,.2f\n", totalServiceRevenue + surveyAndLocalFeeRevenue));
        report.append("═══════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    /**
     * Generate drilling service revenue report
     * @return Formatted report string
     */
    public static String generateDrillingRevenueReport() {
        double drillingRevenue = InvoiceService.getDrillingRevenue();
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("         DRILLING SERVICES REVENUE REPORT\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");
        report.append(String.format("Total Drilling Revenue:  KES %,.2f\n", drillingRevenue));
        report.append("═══════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    /**
     * Generate detailed invoice revenue report
     * @return Formatted report string
     */
    public static String generateDetailedRevenueReport() {
        List<Invoice> invoices = InvoiceService.getAllInvoices();
        double totalRevenue = 0;
        int paidInvoices = 0;
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("           DETAILED INVOICE REVENUE REPORT\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");
        report.append(String.format("%-10s %-10s %-15s %-15s %-12s\n", 
            "Invoice#", "Client ID", "Subtotal", "Total Amount", "Status"));
        report.append("─────────────────────────────────────────────────────────────\n");
        
        for (Invoice invoice : invoices) {
            report.append(String.format("%-10d %-10d KES %-13.2f KES %-13.2f %-12s\n",
                invoice.getInvoiceId(),
                invoice.getClientId(),
                invoice.getSubtotal(),
                invoice.getTotalAmount(),
                invoice.getPaymentStatus()));
            
            if (invoice.getPaymentStatus().equals("Paid")) {
                totalRevenue += invoice.getTotalAmount();
                paidInvoices++;
            }
        }
        
        report.append("─────────────────────────────────────────────────────────────\n");
        report.append(String.format("Paid Invoices:          %d\n", paidInvoices));
        report.append(String.format("Total Revenue (Paid):   KES %,.2f\n", totalRevenue));
        report.append("═══════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
}
