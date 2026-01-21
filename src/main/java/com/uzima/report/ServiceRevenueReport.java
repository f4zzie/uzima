package com.uzima.report;

import com.uzima.model.Transaction;
import com.uzima.service.InvoiceService;

import java.util.List;

/**
 * Generates revenue reports by service type
 */
public class ServiceRevenueReport {
    
    /**
     * Generate service revenue breakdown report
     */
    public String generateServiceRevenueReport() {
        List<Transaction> transactions = InvoiceService.getAllTransactions();
        
        double totalDrillingRevenue = 0;
        double totalPumpRevenue = 0;
        double totalTankRevenue = 0;
        double totalPlumbingRevenue = 0;
        
        int drillingCount = 0;
        int pumpCount = 0;
        int tankCount = 0;
        int plumbingCount = 0;
        
        for (Transaction t : transactions) {
            if ("PAID".equals(t.getPaymentStatus())) {
                if (t.getDrillingCost() > 0) {
                    totalDrillingRevenue += t.getDrillingCost();
                    drillingCount++;
                }
                if (t.getPumpCost() > 0) {
                    totalPumpRevenue += t.getPumpCost();
                    pumpCount++;
                }
                if (t.getTankCost() > 0) {
                    totalTankRevenue += t.getTankCost();
                    tankCount++;
                }
                if (t.getPlumbingCost() > 0) {
                    totalPlumbingRevenue += t.getPlumbingCost();
                    plumbingCount++;
                }
            }
        }
        
        double totalRevenue = totalDrillingRevenue + totalPumpRevenue + totalTankRevenue + totalPlumbingRevenue;
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════════════\n");
        report.append("                    SERVICE REVENUE REPORT\n");
        report.append("═══════════════════════════════════════════════════════════════════\n\n");
        
        report.append("REVENUE BY SERVICE:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("Drilling Services:    %3d transactions    KSh %,.2f\n", drillingCount, totalDrillingRevenue));
        report.append(String.format("Pump Installation:    %3d transactions    KSh %,.2f\n", pumpCount, totalPumpRevenue));
        report.append(String.format("Tank Installation:    %3d transactions    KSh %,.2f\n", tankCount, totalTankRevenue));
        report.append(String.format("Plumbing Services:    %3d transactions    KSh %,.2f\n", plumbingCount, totalPlumbingRevenue));
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("TOTAL REVENUE:                            KSh %,.2f\n", totalRevenue));
        report.append("\n");
        
        // Revenue percentages
        report.append("REVENUE DISTRIBUTION:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        if (totalRevenue > 0) {
            report.append(String.format("Drilling:    %.1f%%\n", (totalDrillingRevenue / totalRevenue) * 100));
            report.append(String.format("Pump:        %.1f%%\n", (totalPumpRevenue / totalRevenue) * 100));
            report.append(String.format("Tank:        %.1f%%\n", (totalTankRevenue / totalRevenue) * 100));
            report.append(String.format("Plumbing:    %.1f%%\n", (totalPlumbingRevenue / totalRevenue) * 100));
        } else {
            report.append("No paid transactions found.\n");
        }
        
        report.append("\n═══════════════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
    
    /**
     * Generate average transaction report
     */
    public String generateAverageTransactionReport() {
        List<Transaction> transactions = InvoiceService.getAllTransactions();
        
        if (transactions.isEmpty()) {
            return "No transactions found for analysis.";
        }
        
        double totalValue = 0;
        double totalPaid = 0;
        int paidCount = 0;
        int pendingCount = 0;
        
        for (Transaction t : transactions) {
            totalValue += t.getGrandTotal();
            totalPaid += t.getAmountPaid();
            
            if ("PAID".equals(t.getPaymentStatus())) {
                paidCount++;
            } else {
                pendingCount++;
            }
        }
        
        double avgTransactionValue = totalValue / transactions.size();
        double avgPaidValue = paidCount > 0 ? totalPaid / paidCount : 0;
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════════════\n");
        report.append("                  AVERAGE TRANSACTION REPORT\n");
        report.append("═══════════════════════════════════════════════════════════════════\n\n");
        
        report.append("TRANSACTION SUMMARY:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("Total Transactions:           %d\n", transactions.size()));
        report.append(String.format("Paid Transactions:            %d\n", paidCount));
        report.append(String.format("Pending Transactions:         %d\n", pendingCount));
        report.append("─────────────────────────────────────────────────────────────────────\n\n");
        
        report.append("AVERAGES:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("Average Transaction Value:    KSh %,.2f\n", avgTransactionValue));
        report.append(String.format("Average Paid Amount:          KSh %,.2f\n", avgPaidValue));
        report.append(String.format("Total Revenue:                KSh %,.2f\n", totalValue));
        report.append(String.format("Total Collected:              KSh %,.2f\n", totalPaid));
        report.append(String.format("Outstanding Balance:          KSh %,.2f\n", totalValue - totalPaid));
        
        report.append("\n═══════════════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
}
