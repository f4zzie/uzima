package com.uzima.report;

import com.uzima.model.Client;
import com.uzima.model.Transaction;
import com.uzima.service.InvoiceService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates personalized reports for individual customers
 */
public class CustomerReportGenerator {
    private Client client;
    
    public CustomerReportGenerator(Client client) {
        this.client = client;
    }
    
    /**
     * Generate comprehensive personal account summary
     */
    public String generatePersonalAccountSummary() {
        List<Transaction> transactions = InvoiceService.getAllTransactions().stream()
            .filter(t -> t.getClientId().equals(client.getClientId()))
            .collect(Collectors.toList());
        
        double totalSpent = 0;
        double totalPaid = 0;
        double pendingBalance = 0;
        int paidCount = 0;
        int partialCount = 0;
        int pendingCount = 0;
        
        for (Transaction t : transactions) {
            totalSpent += t.getGrandTotal();
            String status = t.getPaymentStatus();
            if ("PAID".equals(status)) {
                paidCount++;
                totalPaid += t.getGrandTotal();
            } else if ("PARTIALLY_PAID".equals(status)) {
                partialCount++;
                totalPaid += t.getAmountPaid();
                pendingBalance += (t.getGrandTotal() - t.getAmountPaid());
            } else {
                pendingCount++;
                pendingBalance += t.getGrandTotal();
            }
        }
        
        StringBuilder report = new StringBuilder();
        report.append("╔═══════════════════════════════════════════════════════════════════╗\n");
        report.append("║               MY ACCOUNT SUMMARY - UZIMA BOREHOLE                ║\n");
        report.append("╚═══════════════════════════════════════════════════════════════════╝\n\n");
        
        report.append("PERSONAL INFORMATION:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("  Client ID:                  %s\n", client.getClientId()));
        report.append(String.format("  Name:                       %s\n", client.getFullName()));
        report.append(String.format("  Email:                      %s\n", client.getEmail()));
        report.append(String.format("  Phone:                      %s\n", client.getPhoneNumber()));
        report.append(String.format("  Category:                   %s\n", client.getClientCategory()));
        report.append(String.format("  Location:                   %s\n", client.getBoreholeLocation()));
        report.append("\n");
        
        report.append("ACCOUNT STATISTICS:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("  Total Services:             %d\n", transactions.size()));
        report.append(String.format("  Completed Payments:         %d\n", paidCount));
        report.append(String.format("  Partial Payments:           %d\n", partialCount));
        report.append(String.format("  Pending Payments:           %d\n", pendingCount));
        report.append("\n");
        
        report.append("FINANCIAL SUMMARY:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("  Total Service Value:        KSh %,.2f\n", totalSpent));
        report.append(String.format("  Total Paid:                 KSh %,.2f\n", totalPaid));
        report.append(String.format("  Outstanding Balance:        KSh %,.2f\n", pendingBalance));
        report.append("\n");
        
        report.append("═══════════════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
    
    /**
     * Generate detailed transaction history
     */
    public String generateTransactionHistory() {
        List<Transaction> transactions = InvoiceService.getAllTransactions().stream()
            .filter(t -> t.getClientId().equals(client.getClientId()))
            .collect(Collectors.toList());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        
        StringBuilder report = new StringBuilder();
        report.append("╔═══════════════════════════════════════════════════════════════════╗\n");
        report.append("║           MY TRANSACTION HISTORY - UZIMA BOREHOLE                ║\n");
        report.append("╚═══════════════════════════════════════════════════════════════════╝\n\n");
        
        report.append(String.format("Client: %s (%s)\n", client.getFullName(), client.getClientId()));
        report.append(String.format("Total Transactions: %d\n\n", transactions.size()));
        
        if (transactions.isEmpty()) {
            report.append("No transactions found.\n");
        } else {
            report.append("TRANSACTION DETAILS:\n");
            report.append("─────────────────────────────────────────────────────────────────────\n");
            
            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                report.append(String.format("\n%d. Transaction #%s\n", (i + 1), t.getTransactionNo()));
                report.append(String.format("   Date:              %s\n", sdf.format(t.getTransactionDate())));
                report.append(String.format("   Service Type:      %s\n", t.getJobStatus()));
                report.append(String.format("   Total Amount:      KSh %,.2f\n", t.getGrandTotal()));
                report.append(String.format("   Amount Paid:       KSh %,.2f\n", t.getAmountPaid()));
                report.append(String.format("   Balance:           KSh %,.2f\n", t.getGrandTotal() - t.getAmountPaid()));
                report.append(String.format("   Payment Status:    %s\n", t.getPaymentStatus()));
                report.append(String.format("   Job Status:        %s\n", t.getJobStatus()));
                report.append("   ─────────────────────────────────────────────────\n");
            }
        }
        
        report.append("\n═══════════════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
    
    /**
     * Generate payment history report
     */
    public String generatePaymentHistory() {
        List<Transaction> transactions = InvoiceService.getAllTransactions().stream()
            .filter(t -> t.getClientId().equals(client.getClientId()))
            .collect(Collectors.toList());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        
        StringBuilder report = new StringBuilder();
        report.append("╔═══════════════════════════════════════════════════════════════════╗\n");
        report.append("║            MY PAYMENT HISTORY - UZIMA BOREHOLE                   ║\n");
        report.append("╚═══════════════════════════════════════════════════════════════════╝\n\n");
        
        report.append(String.format("Client: %s (%s)\n\n", client.getFullName(), client.getClientId()));
        
        double totalPaid = 0;
        int paymentCount = 0;
        
        report.append("PAYMENT RECORDS:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        
        for (Transaction t : transactions) {
            if (t.getAmountPaid() > 0) {
                paymentCount++;
                totalPaid += t.getAmountPaid();
                
                report.append(String.format("\nTransaction: %s\n", t.getTransactionNo()));
                report.append(String.format("   Date:           %s\n", sdf.format(t.getTransactionDate())));
                report.append(String.format("   Service:        %s\n", t.getJobStatus()));
                report.append(String.format("   Total Cost:     KSh %,.2f\n", t.getGrandTotal()));
                report.append(String.format("   Amount Paid:    KSh %,.2f\n", t.getAmountPaid()));
                report.append(String.format("   Status:         %s\n", t.getPaymentStatus()));
                report.append("   ─────────────────────────────────────────────────\n");
            }
        }
        
        if (paymentCount == 0) {
            report.append("\nNo payments recorded yet.\n");
        }
        
        report.append("\n");
        report.append("PAYMENT SUMMARY:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("  Total Payments Made:        %d\n", paymentCount));
        report.append(String.format("  Total Amount Paid:          KSh %,.2f\n", totalPaid));
        report.append("\n═══════════════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
    
    /**
     * Generate service breakdown report
     */
    public String generateServiceBreakdown() {
        List<Transaction> transactions = InvoiceService.getAllTransactions().stream()
            .filter(t -> t.getClientId().equals(client.getClientId()))
            .collect(Collectors.toList());
        
        StringBuilder report = new StringBuilder();
        report.append("╔═══════════════════════════════════════════════════════════════════╗\n");
        report.append("║          MY SERVICE BREAKDOWN - UZIMA BOREHOLE                   ║\n");
        report.append("╚═══════════════════════════════════════════════════════════════════╝\n\n");
        
        report.append(String.format("Client: %s (%s)\n\n", client.getFullName(), client.getClientId()));
        
        double totalDrilling = 0;
        double totalPump = 0;
        double totalTank = 0;
        double totalPlumbing = 0;
        double totalFees = 0;
        double totalTax = 0;
        
        for (Transaction t : transactions) {
            totalDrilling += t.getDrillingCost();
            totalPump += t.getPumpCost();
            totalTank += t.getTankCost();
            totalPlumbing += t.getPlumbingCost();
            totalFees += (t.getSurveyFee() + t.getAuthorityFee());
            totalTax += t.getTaxAmount();
        }
        
        report.append("SERVICE COST BREAKDOWN:\n");
        report.append("─────────────────────────────────────────────────────────────────────\n");
        report.append(String.format("  Drilling Services:          KSh %,.2f\n", totalDrilling));
        report.append(String.format("  Pump Installation:          KSh %,.2f\n", totalPump));
        report.append(String.format("  Tank Installation:          KSh %,.2f\n", totalTank));
        report.append(String.format("  Plumbing Services:          KSh %,.2f\n", totalPlumbing));
        report.append(String.format("  Survey & Authority Fees:    KSh %,.2f\n", totalFees));
        report.append(String.format("  Tax (16% VAT):              KSh %,.2f\n", totalTax));
        report.append("  ─────────────────────────────────────────────────\n");
        report.append(String.format("  Grand Total:                KSh %,.2f\n", 
            totalDrilling + totalPump + totalTank + totalPlumbing + totalFees + totalTax));
        report.append("\n");
        
        report.append("═══════════════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
}
