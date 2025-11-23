package com.uzima.ui;

import com.uzima.models.Invoice;
import com.uzima.services.InvoiceService;
import com.uzima.services.ServiceManagementService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Step 7: Invoice Preview & Confirmation Panel
 * User reviews the invoice before submitting the service request
 */
public class InvoicePreviewPanel extends JPanel {
    private ServiceRequestWizardManager wizard;
    private JTextArea invoiceTextArea;

    public InvoicePreviewPanel(ServiceRequestWizardManager wizard) {
        this.wizard = wizard;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(12, 45, 97));
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));
        
        JLabel titleLabel = new JLabel("Step 7: Invoice Preview & Confirmation");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Invoice display area
        invoiceTextArea = new JTextArea();
        invoiceTextArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        invoiceTextArea.setEditable(false);
        invoiceTextArea.setMargin(new Insets(15, 15, 15, 15));
        invoiceTextArea.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(invoiceTextArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
        
        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(new Color(245, 246, 250));
        
        JButton confirmButton = new JButton("✓ Confirm & Submit");
        confirmButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        confirmButton.setBackground(new Color(76, 175, 80));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        confirmButton.setBorderPainted(false);
        confirmButton.addActionListener(e -> onConfirmSubmit());
        
        JButton printButton = new JButton("🖨 Print Invoice");
        printButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        printButton.setBackground(new Color(33, 150, 243));
        printButton.setForeground(Color.WHITE);
        printButton.setFocusPainted(false);
        printButton.setBorderPainted(false);
        printButton.addActionListener(e -> onPrintInvoice());
        
        actionPanel.add(printButton);
        actionPanel.add(confirmButton);
        add(actionPanel, BorderLayout.SOUTH);
        
        // Generate invoice preview
        generateInvoicePreview();
    }

    private void generateInvoicePreview() {
        ServiceRequestWizardManager.ServiceRequestData data = wizard.getRequestData();
        
        // Get category fees
        double[] fees = ServiceManagementService.getFeesByCategory(data.clientCategory);
        data.surveyFee = fees[0];
        data.localAuthorityFee = fees[1];
        
        // Calculate subtotal
        data.subtotal = data.surveyFee + data.localAuthorityFee + data.drillingCost + 
                       data.pumpCost + data.plumbingCost + data.tankCost + data.depthHeightCharge;
        
        // Calculate tax and total
        data.taxAmount = InvoiceService.calculateTax(data.subtotal);
        data.totalAmount = InvoiceService.calculateTotalWithTax(data.subtotal);
        
        // Generate invoice text
        StringBuilder invoice = new StringBuilder();
        invoice.append("╔════════════════════════════════════════════════════════════════╗\n");
        invoice.append("║              UZIMA BOREHOLE DRILLING SYSTEM                    ║\n");
        invoice.append("║                      SERVICE REQUEST INVOICE                   ║\n");
        invoice.append("╚════════════════════════════════════════════════════════════════╝\n\n");
        
        // Invoice details
        invoice.append("DATE: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        invoice.append("CLIENT ID: ").append(wizard.getClientId()).append("\n");
        invoice.append("CLIENT CATEGORY: ").append(data.clientCategory).append("\n\n");
        
        // Services breakdown
        invoice.append("─────────────────────────────────────────────────────────────────\n");
        invoice.append("SERVICE BREAKDOWN:\n");
        invoice.append("─────────────────────────────────────────────────────────────────\n\n");
        
        invoice.append(String.format("Survey Fees                          %15.2f KES\n", data.surveyFee));
        invoice.append(String.format("Local Authority Fees                 %15.2f KES\n", data.localAuthorityFee));
        
        if (data.drillingCost > 0) {
            invoice.append(String.format("Drilling Service (%s)        %15.2f KES\n", 
                data.drillingType != null ? data.drillingType : "N/A", data.drillingCost));
        }
        
        if (data.depthHeightCharge > 0) {
            invoice.append(String.format("Depth/Height Charge (%dm + %dm)       %15.2f KES\n", 
                data.depthMeters, data.heightMeters, data.depthHeightCharge));
        }
        
        if (data.pumpCost > 0) {
            invoice.append(String.format("Pump Installation (%s)         %15.2f KES\n", 
                data.pumpType != null ? data.pumpType : "N/A", data.pumpCost));
        }
        
        if (data.plumbingCost > 0) {
            invoice.append(String.format("Plumbing Services (%dm pipes)        %15.2f KES\n", 
                data.pipeLengthMeters, data.plumbingCost));
        }
        
        if (data.tankCost > 0) {
            invoice.append(String.format("Tank Installation (%dL)           %15.2f KES\n", 
                data.tankCapacityLitres, data.tankCost));
        }
        
        invoice.append("\n─────────────────────────────────────────────────────────────────\n");
        invoice.append(String.format("SUBTOTAL:                              %15.2f KES\n", data.subtotal));
        invoice.append(String.format("TAX (16%%):                             %15.2f KES\n", data.taxAmount));
        invoice.append("─────────────────────────────────────────────────────────────────\n");
        invoice.append(String.format("TOTAL AMOUNT DUE:                      %15.2f KES\n", data.totalAmount));
        invoice.append("═════════════════════════════════════════════════════════════════\n\n");
        
        invoice.append("Payment Terms:\n");
        invoice.append("• Payment is required upon completion of services\n");
        invoice.append("• Invoices are valid for 30 days from date of issue\n");
        invoice.append("• Late payments may incur additional charges\n\n");
        
        invoice.append("Thank you for choosing UZIMA Borehole Drilling Services!\n");
        
        invoiceTextArea.setText(invoice.toString());
    }

    private void onConfirmSubmit() {
        ServiceRequestWizardManager.ServiceRequestData data = wizard.getRequestData();
        
        // Create invoice using backend service
        Invoice invoiceObj = new Invoice(
            1,  // requestId (would be generated by system)
            wizard.getClientId(),
            data.surveyFee,
            data.localAuthorityFee,
            data.drillingCost,
            data.pumpCost,
            data.plumbingCost,
            data.tankCost,
            data.depthHeightCharge
        );
        
        // Save to database
        boolean saved = InvoiceService.createInvoice(invoiceObj);
        
        if (saved) {
            data.invoiceId = invoiceObj.getInvoiceId();
            
            JOptionPane.showMessageDialog(this,
                "✓ Service request submitted successfully!\n\n" +
                "Invoice ID: " + invoiceObj.getInvoiceId() + "\n" +
                "Total Amount: KES " + String.format("%.2f", invoiceObj.getTotalAmount()) + "\n\n" +
                "Your service request has been recorded. Our team will contact you shortly.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Close wizard/navigate
            SwingUtilities.getWindowAncestor(this).dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "✗ Error submitting service request. Please try again.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onPrintInvoice() {
        try {
            invoiceTextArea.print();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error printing invoice: " + e.getMessage(),
                "Print Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
