package com.uzima.ui;

import com.uzima.dao.ClientDAO;
import com.uzima.dao.TransactionDAO;
import com.uzima.model.Client;
import com.uzima.model.Transaction;
import com.uzima.service.InvoiceService;
import com.uzima.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Form for viewing, searching, and managing transactions
 */
public class ViewTransactionsForm extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;
    private JLabel lblTotalTransactions, lblTotalRevenue, lblOutstanding;
    private ClientDAO clientDAO;
    
    public ViewTransactionsForm() {
        setTitle("View Transactions - UZIMA Borehole System");
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        clientDAO = new ClientDAO();
        initComponents();
        loadTransactions();
        updateSummary();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Center Panel - Table
        JPanel centerPanel = createTablePanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom Panel - Summary and Actions
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(UIHelper.PRIMARY_COLOR);
        
        JLabel lblTitle = new JLabel("TRANSACTION MANAGEMENT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblTitle, BorderLayout.NORTH);
        
        // Search and Filter Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(UIHelper.PRIMARY_COLOR);
        
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setForeground(Color.WHITE);
        searchPanel.add(lblSearch);
        
        txtSearch = new JTextField(20);
        searchPanel.add(txtSearch);
        
        JButton btnSearch = new JButton("Search");
        UIHelper.stylePrimaryButton(btnSearch);
        btnSearch.addActionListener(e -> searchTransactions());
        searchPanel.add(btnSearch);
        
        searchPanel.add(Box.createHorizontalStrut(20));
        
        JLabel lblFilter = new JLabel("Status:");
        lblFilter.setForeground(Color.WHITE);
        searchPanel.add(lblFilter);
        
        cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "NOT_PAID", "PARTIALLY_PAID", "PAID"});
        searchPanel.add(cmbStatusFilter);
        cmbStatusFilter.addActionListener(e -> filterByStatus());
        
        panel.add(searchPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columns = {"Transaction No", "Date", "Client ID", "Client Name", 
                           "Total", "Paid", "Balance", "Payment Status", "Job Status"};
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(245, 245, 245));
        
        // Summary Panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 10));
        summaryPanel.setBackground(new Color(245, 245, 245));
        
        lblTotalTransactions = new JLabel("Total Transactions: 0");
        lblTotalTransactions.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryPanel.add(lblTotalTransactions);
        
        lblTotalRevenue = new JLabel("Total Revenue: KSh 0.00");
        lblTotalRevenue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalRevenue.setForeground(UIHelper.SUCCESS_COLOR);
        summaryPanel.add(lblTotalRevenue);
        
        lblOutstanding = new JLabel("Outstanding: KSh 0.00");
        lblOutstanding.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblOutstanding.setForeground(UIHelper.DANGER_COLOR);
        summaryPanel.add(lblOutstanding);
        
        panel.add(summaryPanel, BorderLayout.CENTER);
        
        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonsPanel.setBackground(new Color(245, 245, 245));
        
        JButton btnRefresh = new JButton("Refresh");
        UIHelper.stylePrimaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> {
            loadTransactions();
            updateSummary();
        });
        buttonsPanel.add(btnRefresh);
        
        JButton btnViewDetails = new JButton("View Details");
        UIHelper.styleSuccessButton(btnViewDetails);
        btnViewDetails.addActionListener(e -> viewTransactionDetails());
        buttonsPanel.add(btnViewDetails);
        
        JButton btnUpdatePayment = new JButton("Update Payment");
        UIHelper.stylePrimaryButton(btnUpdatePayment);
        btnUpdatePayment.addActionListener(e -> updatePayment());
        buttonsPanel.add(btnUpdatePayment);
        
        JButton btnClose = new JButton("Close");
        UIHelper.styleDangerButton(btnClose);
        btnClose.addActionListener(e -> dispose());
        buttonsPanel.add(btnClose);
        
        panel.add(buttonsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void loadTransactions() {
        try {
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            tableModel.setRowCount(0);
            
            for (Transaction t : transactions) {
                String clientName = "";
                try {
                    Client client = clientDAO.getClientById(t.getClientId());
                    if (client != null) {
                        clientName = client.getFullName();
                    }
                } catch (Exception e) {
                    clientName = "Unknown";
                }
                
                tableModel.addRow(new Object[]{
                    t.getTransactionNo(),
                    UIHelper.formatDate(t.getTransactionDate()),
                    t.getClientId(),
                    clientName,
                    UIHelper.formatCurrency(t.getGrandTotal()),
                    UIHelper.formatCurrency(t.getAmountPaid()),
                    UIHelper.formatCurrency(t.getBalance()),
                    t.getPaymentStatus(),
                    t.getJobStatus()
                });
            }
        } catch (SQLException e) {
            UIHelper.showError(this, "Error loading transactions: " + e.getMessage());
        }
    }
    
    private void searchTransactions() {
        String searchTerm = txtSearch.getText().trim().toLowerCase();
        
        if (searchTerm.isEmpty()) {
            loadTransactions();
            return;
        }
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        
        RowFilter<DefaultTableModel, Object> filter = RowFilter.regexFilter("(?i)" + searchTerm);
        sorter.setRowFilter(filter);
    }
    
    private void filterByStatus() {
        String status = (String) cmbStatusFilter.getSelectedItem();
        
        if ("ALL".equals(status)) {
            table.setRowSorter(null);
            loadTransactions();
            return;
        }
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        
        RowFilter<DefaultTableModel, Object> filter = RowFilter.regexFilter(status, 7);
        sorter.setRowFilter(filter);
    }
    
    private void viewTransactionDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showWarning(this, "Please select a transaction to view");
            return;
        }
        
        String transactionNo = (String) table.getValueAt(selectedRow, 0);
        
        try {
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            Transaction transaction = null;
            
            for (Transaction t : transactions) {
                if (t.getTransactionNo().equals(transactionNo)) {
                    transaction = t;
                    break;
                }
            }
            
            if (transaction == null) {
                UIHelper.showError(this, "Transaction not found");
                return;
            }
            
            showTransactionDetailsDialog(transaction);
        } catch (SQLException e) {
            UIHelper.showError(this, "Error fetching transaction details: " + e.getMessage());
        }
    }
    
    private void showTransactionDetailsDialog(Transaction t) {
        try {
            Client client = clientDAO.getClientById(t.getClientId());
            
            StringBuilder details = new StringBuilder();
            details.append("═══════════════ TRANSACTION DETAILS ═══════════════\n\n");
            details.append("Transaction No:    ").append(t.getTransactionNo()).append("\n");
            details.append("Date:              ").append(UIHelper.formatDate(t.getTransactionDate())).append("\n");
            details.append("Client ID:         ").append(t.getClientId()).append("\n");
            details.append("Client Name:       ").append(client != null ? client.getFullName() : "Unknown").append("\n");
            details.append("Category:          ").append(client != null ? client.getClientCategory() : "Unknown").append("\n\n");
            
            details.append("────────────────── SERVICES ──────────────────\n");
            if (t.getDrillingCost() > 0) {
                details.append("Drilling:          ").append(UIHelper.formatCurrency(t.getDrillingCost())).append("\n");
                details.append("  Depth:           ").append(t.getBoreholeDepth()).append(" meters\n");
            }
            if (t.getPumpCost() > 0) {
                details.append("Pump:              ").append(UIHelper.formatCurrency(t.getPumpCost())).append("\n");
            }
            if (t.getTankCost() > 0) {
                details.append("Tank:              ").append(UIHelper.formatCurrency(t.getTankCost())).append("\n");
            }
            if (t.getPlumbingCost() > 0) {
                details.append("Plumbing:          ").append(UIHelper.formatCurrency(t.getPlumbingCost())).append("\n");
            }
            
            details.append("\n────────────────── TOTALS ──────────────────\n");
            details.append("Subtotal:          ").append(UIHelper.formatCurrency(t.getSubtotal())).append("\n");
            details.append("Tax (16%):         ").append(UIHelper.formatCurrency(t.getTaxAmount())).append("\n");
            details.append("Grand Total:       ").append(UIHelper.formatCurrency(t.getGrandTotal())).append("\n\n");
            
            details.append("────────────────── PAYMENT ──────────────────\n");
            details.append("Amount Paid:       ").append(UIHelper.formatCurrency(t.getAmountPaid())).append("\n");
            details.append("Balance:           ").append(UIHelper.formatCurrency(t.getBalance())).append("\n");
            details.append("Payment Status:    ").append(t.getPaymentStatus()).append("\n\n");
            
            details.append("────────────────── STATUS ──────────────────\n");
            details.append("Job Status:        ").append(t.getJobStatus()).append("\n");
            
            JTextArea textArea = new JTextArea(details.toString());
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setEditable(false);
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 500));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Transaction Details", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            UIHelper.showError(this, "Error loading client details: " + e.getMessage());
        }
    }
    
    private void updatePayment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            UIHelper.showWarning(this, "Please select a transaction");
            return;
        }
        
        String transactionNo = (String) table.getValueAt(selectedRow, 0);
        String balanceStr = (String) table.getValueAt(selectedRow, 6);
        
        double balance = 0;
        try {
            balance = Double.parseDouble(balanceStr.replace("KSh ", "").replace(",", ""));
        } catch (NumberFormatException e) {
            UIHelper.showError(this, "Error parsing balance");
            return;
        }
        
        if (balance <= 0) {
            UIHelper.showInfo(this, "This transaction is fully paid");
            return;
        }
        
        String input = JOptionPane.showInputDialog(this, 
            "Outstanding Balance: " + UIHelper.formatCurrency(balance) + "\nEnter payment amount:",
            "Update Payment",
            JOptionPane.QUESTION_MESSAGE);
        
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        
        try {
            double payment = Double.parseDouble(input.trim());
            
            if (payment <= 0) {
                UIHelper.showWarning(this, "Payment amount must be greater than zero");
                return;
            }
            
            if (payment > balance) {
                UIHelper.showWarning(this, "Payment amount cannot exceed balance");
                return;
            }
            
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            int transactionId = -1;
            
            for (Transaction t : transactions) {
                if (t.getTransactionNo().equals(transactionNo)) {
                    transactionId = t.getTransactionId();
                    break;
                }
            }
            
            if (transactionId == -1) {
                UIHelper.showError(this, "Transaction not found");
                return;
            }
            
            boolean success = InvoiceService.updatePayment(transactionId, payment);
            
            if (success) {
                UIHelper.showSuccess(this, "Payment updated successfully!\nAmount: " + UIHelper.formatCurrency(payment));
                loadTransactions();
                updateSummary();
            } else {
                UIHelper.showError(this, "Failed to update payment");
            }
        } catch (NumberFormatException e) {
            UIHelper.showError(this, "Invalid payment amount");
        } catch (SQLException e) {
            UIHelper.showError(this, "Error updating payment: " + e.getMessage());
        }
    }
    
    private void updateSummary() {
        int totalCount = InvoiceService.getTotalTransactionCount();
        double totalRevenue = InvoiceService.getTotalRevenue();
        double outstanding = InvoiceService.getTotalOutstandingBalance();
        
        lblTotalTransactions.setText("Total Transactions: " + totalCount);
        lblTotalRevenue.setText("Total Revenue: " + UIHelper.formatCurrency(totalRevenue));
        lblOutstanding.setText("Outstanding: " + UIHelper.formatCurrency(outstanding));
    }
}

