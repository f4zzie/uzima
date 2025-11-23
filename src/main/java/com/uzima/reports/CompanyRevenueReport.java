package com.uzima.reports;

import com.uzima.models.Invoice;
import com.uzima.services.ClientService;
import com.uzima.services.InvoiceService;
import java.util.List;

/**
 * Generates company-wide financial reports
 */
public class CompanyRevenueReport {

    /**
     * Generate comprehensive company financial report
     * @return Formatted report string
     */
    public static String generateComprehensiveFinancialReport() {
        List<Invoice> invoices = InvoiceService.getAllInvoices();
        double totalRevenue = InvoiceService.getTotalRevenue();
        double totalTax = InvoiceService.getTotalTaxCollected();
        int totalClients = ClientService.getTotalClientCount();
        int paidInvoices = 0;
        int pendingInvoices = 0;
        
        for (Invoice invoice : invoices) {
            if (invoice.getPaymentStatus().equals("Paid")) {
                paidInvoices++;
            } else if (invoice.getPaymentStatus().equals("Pending")) {
                pendingInvoices++;
            }
        }
        
        StringBuilder report = new StringBuilder();
        report.append("╔═══════════════════════════════════════════════════════════╗\n");
        report.append("║         UZIMA BOREHOLE - FINANCIAL REPORT                ║\n");
        report.append("╚═══════════════════════════════════════════════════════════╝\n\n");
        
        report.append("COMPANY STATISTICS:\n");
        report.append("─────────────────────────────────────────────────────────────\n");
        report.append(String.format("  Total Clients:          %d\n", totalClients));
        report.append(String.format("  Total Invoices:         %d\n", invoices.size()));
        report.append(String.format("  Paid Invoices:          %d\n", paidInvoices));
        report.append(String.format("  Pending Invoices:       %d\n", pendingInvoices));
        report.append("\n");
        
        report.append("FINANCIAL SUMMARY:\n");
        report.append("─────────────────────────────────────────────────────────────\n");
        report.append(String.format("  Total Revenue (Paid):   KES %,.2f\n", totalRevenue));
        report.append(String.format("  Total Tax Collected:    KES %,.2f\n", totalTax));
        report.append(String.format("  Net Revenue (after tax):KES %,.2f\n", totalRevenue - totalTax));
        report.append("\n");
        
        report.append("CLIENT BREAKDOWN:\n");
        report.append("─────────────────────────────────────────────────────────────\n");
        report.append(String.format("  Industrial Clients:     %d\n", ClientService.getClientCountByCategory("Industrial")));
        report.append(String.format("  Commercial Clients:     %d\n", ClientService.getClientCountByCategory("Commercial")));
        report.append(String.format("  Domestic Clients:       %d\n", ClientService.getClientCountByCategory("Domestic")));
        report.append("\n");
        
        report.append("═══════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    /**
     * Generate fee collection report
     * @return Formatted report string
     */
    public static String generateFeeCollectionReport() {
        List<Invoice> invoices = InvoiceService.getAllInvoices();
        double totalSurveyFees = 0;
        double totalLocalAuthorityFees = 0;
        int collectionsCount = 0;
        
        for (Invoice invoice : invoices) {
            if (invoice.getPaymentStatus().equals("Paid")) {
                totalSurveyFees += invoice.getSurveyFee();
                totalLocalAuthorityFees += invoice.getLocalAuthorityFee();
                collectionsCount++;
            }
        }
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("           SURVEY & LOCAL AUTHORITY FEES REPORT\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");
        report.append(String.format("Collections from %d paid invoices:\n\n", collectionsCount));
        report.append(String.format("  Survey Fees:            KES %,.2f\n", totalSurveyFees));
        report.append(String.format("  Local Authority Fees:   KES %,.2f\n", totalLocalAuthorityFees));
        report.append("─────────────────────────────────────────────────────────────\n");
        report.append(String.format("  TOTAL FEES COLLECTED:   KES %,.2f\n", totalSurveyFees + totalLocalAuthorityFees));
        report.append("═══════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    /**
     * Generate payment status summary
     * @return Formatted report string
     */
    public static String generatePaymentStatusReport() {
        List<Invoice> invoices = InvoiceService.getAllInvoices();
        double paidAmount = 0;
        double pendingAmount = 0;
        double overdueAmount = 0;
        int paidCount = 0;
        int pendingCount = 0;
        int overdueCount = 0;
        
        for (Invoice invoice : invoices) {
            switch (invoice.getPaymentStatus()) {
                case "Paid":
                    paidAmount += invoice.getTotalAmount();
                    paidCount++;
                    break;
                case "Pending":
                    pendingAmount += invoice.getTotalAmount();
                    pendingCount++;
                    break;
                case "Overdue":
                    overdueAmount += invoice.getTotalAmount();
                    overdueCount++;
                    break;
            }
        }
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("              PAYMENT STATUS SUMMARY\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");
        report.append(String.format("PAID INVOICES (%d):\n", paidCount));
        report.append(String.format("  Amount:                 KES %,.2f\n\n", paidAmount));
        report.append(String.format("PENDING INVOICES (%d):\n", pendingCount));
        report.append(String.format("  Amount:                 KES %,.2f\n\n", pendingAmount));
        report.append(String.format("OVERDUE INVOICES (%d):\n", overdueCount));
        report.append(String.format("  Amount:                 KES %,.2f\n\n", overdueAmount));
        report.append("─────────────────────────────────────────────────────────────\n");
        report.append(String.format("  Total Outstanding:     KES %,.2f\n", pendingAmount + overdueAmount));
        report.append("═══════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
}
