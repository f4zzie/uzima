package com.uzima.report;

import com.uzima.model.Transaction;
import com.uzima.service.ClientServiceLayer;
import com.uzima.service.InvoiceService;

import java.util.List;

/**
 * Generates company-wide financial reports
 */
public class CompanyRevenueReport {
    private ClientServiceLayer clientService;
    private InvoiceService invoiceService;
    
    public CompanyRevenueReport() {
        this.clientService = new ClientServiceLayer();
        this.invoiceService = new InvoiceService();
    }

    /**
     * Generate comprehensive company financial report
     * @return Formatted report string
     */
    public String generateComprehensiveFinancialReport() {
        List<Transaction> transactions = InvoiceService.getAllTransactions();
        double totalRevenue = InvoiceService.getTotalRevenue();
        double totalTax = InvoiceService.getTotalTaxCollected();
        int totalClients = clientService.getTotalClientCount();
        int paidCount = 0;
        int partialCount = 0;
        int pendingCount = 0;
        
        for (Transaction t : transactions) {
            String status = t.getPaymentStatus();
            if ("PAID".equals(status)) {
                paidCount++;
            } else if ("PARTIALLY_PAID".equals(status)) {
                partialCount++;
            } else {
                pendingCount++;
            }
        }
        
        StringBuilder report = new StringBuilder();
        report.append("╔═══════════════════════════════════════════════════════════════════╗\n");
        report.append("║           UZIMA BOREHOLE - FINANCIAL REPORT                      ║\n");
        report.append("╚═══════════════════════════════════════════════════════════════════╝\n\n");
        
        report.append("COMPANY STATISTICS:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("  Total Clients:              %d\n", totalClients));
        report.append(String.format("  Total Transactions:         %d\n", transactions.size()));
        report.append(String.format("  Paid Transactions:          %d\n", paidCount));
        report.append(String.format("  Partially Paid:             %d\n", partialCount));
        report.append(String.format("  Pending Transactions:       %d\n", pendingCount));
        report.append("\n");
        
        report.append("FINANCIAL SUMMARY:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("  Total Revenue (Paid):       KSh %,.2f\n", totalRevenue));
        report.append(String.format("  Total Tax Collected:        KSh %,.2f\n", totalTax));
        report.append(String.format("  Net Revenue (after tax):    KSh %,.2f\n", totalRevenue - totalTax));
        report.append(String.format("  Outstanding Balance:        KSh %,.2f\n", InvoiceService.getTotalOutstandingBalance()));
        report.append("\n");
        
        report.append("CLIENT BREAKDOWN:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("  Industrial Clients:         %d\n", clientService.getClientCountByCategory("INDUSTRIAL")));
        report.append(String.format("  Commercial Clients:         %d\n", clientService.getClientCountByCategory("COMMERCIAL")));
        report.append(String.format("  Domestic Clients:           %d\n", clientService.getClientCountByCategory("DOMESTIC")));
        report.append("\n");
        
        report.append("═══════════════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    /**
     * Generate fee collection report
     * @return Formatted report string
     */
    public String generateFeeCollectionReport() {
        List<Transaction> transactions = InvoiceService.getAllTransactions();
        double totalSurveyFees = 0;
        double totalAuthorityFees = 0;
        int collectionsCount = 0;
        
        for (Transaction t : transactions) {
            if ("PAID".equals(t.getPaymentStatus())) {
                totalSurveyFees += t.getSurveyFee();
                totalAuthorityFees += t.getAuthorityFee();
                collectionsCount++;
            }
        }
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════════════\n");
        report.append("             SURVEY & LOCAL AUTHORITY FEES REPORT\n");
        report.append("═══════════════════════════════════════════════════════════════════\n\n");
        report.append(String.format("Collections from %d paid transactions:\n\n", collectionsCount));
        report.append(String.format("  Survey Fees:                KSh %,.2f\n", totalSurveyFees));
        report.append(String.format("  Local Authority Fees:       KSh %,.2f\n", totalAuthorityFees));
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("  TOTAL FEES COLLECTED:       KSh %,.2f\n", totalSurveyFees + totalAuthorityFees));
        report.append("═══════════════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }

    /**
     * Generate payment status summary
     * @return Formatted report string
     */
    public String generatePaymentStatusReport() {
        int paidCount = InvoiceService.getTransactionCountByStatus("PAID");
        int partialCount = InvoiceService.getTransactionCountByStatus("PARTIALLY_PAID");
        int pendingCount = InvoiceService.getTransactionCountByStatus("NOT_PAID");
        int total = paidCount + partialCount + pendingCount;
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════════════\n");
        report.append("                  PAYMENT STATUS SUMMARY\n");
        report.append("═══════════════════════════════════════════════════════════════════\n\n");
        report.append(String.format("Total Transactions:         %d\n\n", total));
        report.append(String.format("  Fully Paid:               %d (%.1f%%)\n", paidCount, (paidCount * 100.0 / Math.max(total, 1))));
        report.append(String.format("  Partially Paid:           %d (%.1f%%)\n", partialCount, (partialCount * 100.0 / Math.max(total, 1))));
        report.append(String.format("  Not Paid:                 %d (%.1f%%)\n", pendingCount, (pendingCount * 100.0 / Math.max(total, 1))));
        report.append("\n");
        report.append(String.format("Outstanding Balance:        KSh %,.2f\n", InvoiceService.getTotalOutstandingBalance()));
        report.append("═══════════════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
}
