package com.uzima.report;

import com.uzima.service.InvoiceService;

/**
 * Generates tax-related reports
 */
public class TaxReport {

    /**
     * Generate comprehensive tax report
     */
    public String generateTaxReport() {
        double totalRevenue = InvoiceService.getTotalRevenue();
        double totalTaxCollected = InvoiceService.getTotalTaxCollected();
        double netRevenue = totalRevenue - totalTaxCollected;
        int transactionCount = InvoiceService.getTotalTransactionCount();
        int paidCount = InvoiceService.getTransactionCountByStatus("PAID");
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════════════\n");
        report.append("                      TAX REPORT (16% VAT)\n");
        report.append("═══════════════════════════════════════════════════════════════════\n\n");
        
        report.append("TAX COLLECTION SUMMARY:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("Tax Rate:                   16%%\n"));
        report.append(String.format("Total Transactions:         %d\n", transactionCount));
        report.append(String.format("Paid Transactions:          %d\n", paidCount));
        report.append("\n");
        
        report.append("FINANCIAL BREAKDOWN:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("Gross Revenue (incl. tax):  KSh %,.2f\n", totalRevenue));
        report.append(String.format("Tax Collected:              KSh %,.2f\n", totalTaxCollected));
        report.append(String.format("Net Revenue (excl. tax):    KSh %,.2f\n", netRevenue));
        report.append("\n");
        
        report.append("AVERAGE FIGURES:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        double avgTaxPerTransaction = paidCount > 0 ? totalTaxCollected / paidCount : 0;
        double avgRevenuePerTransaction = paidCount > 0 ? totalRevenue / paidCount : 0;
        
        report.append(String.format("Avg Tax per Transaction:    KSh %,.2f\n", avgTaxPerTransaction));
        report.append(String.format("Avg Revenue per Txn:        KSh %,.2f\n", avgRevenuePerTransaction));
        report.append("\n");
        
        report.append("═══════════════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
}
