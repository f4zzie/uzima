package com.uzima.ui;

import com.uzima.database.InvoiceDAO;
import com.uzima.models.Invoice;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Invoice List Panel
 * Displays all invoices for the logged-in client
 */
public class InvoiceListPanel extends JPanel {
    private int clientId;
    private boolean showAllInvoices;
    private JTable invoiceTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;

    public InvoiceListPanel(int clientId) {
    this.clientId = clientId;
    this.showAllInvoices = clientId <= 0;
    initUI();
    loadInvoices();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(12, 45, 97));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel(showAllInvoices ? "All Invoices" : "My Invoices");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel(showAllInvoices
            ? "Manage every invoice in the system"
            : "View all your service invoices and payment status");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 200, 200));
        
        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setBackground(new Color(12, 45, 97));
        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(5));
        titleContainer.add(subtitleLabel);
        
        headerPanel.add(titleContainer, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Table Panel
        String[] columnNames = {"Invoice ID", "Date", "Subtotal (KES)", "Tax (KES)", "Total (KES)", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        invoiceTable = new JTable(tableModel);
        invoiceTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        invoiceTable.setRowHeight(30);
        invoiceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        invoiceTable.getTableHeader().setBackground(new Color(33, 150, 243));
        invoiceTable.getTableHeader().setForeground(Color.WHITE);
        invoiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoiceTable.setSelectionBackground(new Color(33, 150, 243, 50));
        
        // Add double-click listener to view invoice details
        invoiceTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = invoiceTable.getSelectedRow();
                    if (row != -1) {
                        int invoiceId = (int) tableModel.getValueAt(row, 0);
                        viewInvoiceDetails(invoiceId);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(invoiceTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom Panel with summary
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(245, 246, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        totalLabel = new JLabel("Total Outstanding: KES 0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(new Color(12, 45, 97));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(new Color(245, 246, 250));
        
        JButton viewDetailsButton = new JButton("View Details");
        viewDetailsButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewDetailsButton.setBackground(new Color(33, 150, 243));
        viewDetailsButton.setForeground(Color.WHITE);
        viewDetailsButton.setFocusPainted(false);
        viewDetailsButton.addActionListener(e -> {
            int row = invoiceTable.getSelectedRow();
            if (row != -1) {
                int invoiceId = (int) tableModel.getValueAt(row, 0);
                viewInvoiceDetails(invoiceId);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select an invoice to view details.",
                    "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setBackground(new Color(76, 175, 80));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> loadInvoices());
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeButton.setBackground(new Color(244, 67, 54));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());
        
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        bottomPanel.add(totalLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadInvoices() {
        tableModel.setRowCount(0); // Clear existing data
        
        List<Invoice> invoices = showAllInvoices
            ? InvoiceDAO.getAllInvoices()
            : InvoiceDAO.getInvoicesByClientId(clientId);
        
        double totalOutstanding = 0.0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Invoice invoice : invoices) {
            Object[] row = {
                invoice.getInvoiceId(),
                invoice.getInvoiceDate().format(formatter),
                String.format("%.2f", invoice.getSubtotal()),
                String.format("%.2f", invoice.getTaxAmount()),
                String.format("%.2f", invoice.getTotalAmount()),
                invoice.getPaymentStatus()
            };
            tableModel.addRow(row);
            
            // Calculate total outstanding (Pending + Partial + Overdue)
            if (!invoice.getPaymentStatus().equalsIgnoreCase("Paid")) {
                totalOutstanding += invoice.getTotalAmount();
            }
        }
        
        totalLabel.setText(String.format("Total Outstanding: KES %.2f", totalOutstanding));
        
        if (invoices.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No invoices found.\nCreate a service request to generate an invoice.",
                "No Invoices",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void viewInvoiceDetails(int invoiceId) {
        Invoice invoice = InvoiceDAO.getInvoiceById(invoiceId);
        
        if (invoice == null) {
            JOptionPane.showMessageDialog(this,
                "Invoice not found.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create detailed invoice view
        JFrame detailFrame = new JFrame("Invoice Details - #" + invoiceId);
        detailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        detailFrame.setSize(700, 800);
        detailFrame.setLocationRelativeTo(this);
        
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(Color.WHITE);
        
        // Create invoice text
        JTextArea invoiceText = new JTextArea();
        invoiceText.setFont(new Font("Consolas", Font.PLAIN, 11));
        invoiceText.setEditable(false);
        invoiceText.setMargin(new Insets(15, 15, 15, 15));
        
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║              UZIMA BOREHOLE DRILLING SYSTEM                    ║\n");
        sb.append("║                      SERVICE INVOICE                           ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════╝\n\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        sb.append("INVOICE ID: ").append(invoice.getInvoiceId()).append("\n");
        sb.append("DATE: ").append(invoice.getInvoiceDate().format(formatter)).append("\n");
        sb.append("CLIENT ID: ").append(invoice.getClientId()).append("\n");
        sb.append("REQUEST ID: ").append(invoice.getRequestId()).append("\n");
        sb.append("PAYMENT STATUS: ").append(invoice.getPaymentStatus()).append("\n\n");
        
        sb.append("─────────────────────────────────────────────────────────────────\n");
        sb.append("SERVICE BREAKDOWN:\n");
        sb.append("─────────────────────────────────────────────────────────────────\n\n");
        
        if (invoice.getSurveyFee() > 0) {
            sb.append(String.format("Survey Fees                          %15.2f KES\n", invoice.getSurveyFee()));
        }
        if (invoice.getLocalAuthorityFee() > 0) {
            sb.append(String.format("Local Authority Fees                 %15.2f KES\n", invoice.getLocalAuthorityFee()));
        }
        if (invoice.getDrillingCost() > 0) {
            sb.append(String.format("Drilling Service                     %15.2f KES\n", invoice.getDrillingCost()));
        }
        if (invoice.getDepthHeightCharge() > 0) {
            sb.append(String.format("Depth/Height Charge                  %15.2f KES\n", invoice.getDepthHeightCharge()));
        }
        if (invoice.getPumpCost() > 0) {
            sb.append(String.format("Pump Installation                    %15.2f KES\n", invoice.getPumpCost()));
        }
        if (invoice.getPlumbingCost() > 0) {
            sb.append(String.format("Plumbing Services                    %15.2f KES\n", invoice.getPlumbingCost()));
        }
        if (invoice.getTankCost() > 0) {
            sb.append(String.format("Tank Installation                    %15.2f KES\n", invoice.getTankCost()));
        }
        
        sb.append("\n─────────────────────────────────────────────────────────────────\n");
        sb.append(String.format("SUBTOTAL:                              %15.2f KES\n", invoice.getSubtotal()));
        sb.append(String.format("TAX (16%%):                             %15.2f KES\n", invoice.getTaxAmount()));
        sb.append("─────────────────────────────────────────────────────────────────\n");
        sb.append(String.format("TOTAL AMOUNT DUE:                      %15.2f KES\n", invoice.getTotalAmount()));
        sb.append("═════════════════════════════════════════════════════════════════\n\n");
        
        sb.append("Payment Terms:\n");
        sb.append("• Payment is required upon completion of services\n");
        sb.append("• Invoices are valid for 30 days from date of issue\n");
        sb.append("• Late payments may incur additional charges\n\n");
        
        sb.append("Thank you for choosing UZIMA Borehole Drilling Services!\n");
        
        invoiceText.setText(sb.toString());
        
        JScrollPane scrollPane = new JScrollPane(invoiceText);
        detailPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        
        JButton printButton = new JButton("🖨 Print");
        printButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        printButton.setBackground(new Color(33, 150, 243));
        printButton.setForeground(Color.WHITE);
        printButton.setFocusPainted(false);
        printButton.addActionListener(e -> {
            try {
                invoiceText.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(detailFrame,
                    "Error printing invoice: " + ex.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.setBackground(new Color(96, 125, 139));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> detailFrame.dispose());
        
        btnPanel.add(printButton);
        btnPanel.add(closeBtn);
        detailPanel.add(btnPanel, BorderLayout.SOUTH);
        
        detailFrame.add(detailPanel);
        detailFrame.setVisible(true);
    }
}
