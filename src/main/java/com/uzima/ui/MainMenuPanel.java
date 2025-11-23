package com.uzima.ui;

import com.uzima.services.ClientService;
import com.uzima.models.Client;
import javax.swing.*;
import java.awt.*;

/**
 * Main Menu Panel
 * Displays after user login/registration with options to access various features
 */
public class MainMenuPanel extends JPanel {
    private int clientId;
    private Client currentClient;

    public MainMenuPanel(int clientId) {
        this.clientId = clientId;
        this.currentClient = ClientService.getClientById(clientId);
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 246, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        
        // Header
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        
        JLabel welcomeLabel = new JLabel("Welcome, " + (currentClient != null ? currentClient.getFullName() : "User"));
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(12, 45, 97));
        add(welcomeLabel, gbc);
        
        // Subtitle
        gbc.gridy = 1;
        JLabel subtitleLabel = new JLabel("UZIMA Borehole Drilling System - Main Menu");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(108, 117, 125));
        add(subtitleLabel, gbc);
        
        // Divider
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JSeparator separator = new JSeparator();
        add(separator, gbc);
        
        // Menu options
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(40, 20, 40, 20);
        
        // Request Service button
        gbc.gridx = 0;
        gbc.gridy = 3;
        JPanel requestServicePanel = createMenuButton(
            "📋 Request Service",
            "Create a new borehole drilling service request",
            new Color(76, 175, 80),
            e -> onRequestService()
        );
        add(requestServicePanel, gbc);
        
        // View Invoices button
        gbc.gridx = 1;
        JPanel viewInvoicesPanel = createMenuButton(
            "📄 View Invoices",
            "View your service invoices and payment status",
            new Color(33, 150, 243),
            e -> onViewInvoices()
        );
        add(viewInvoicesPanel, gbc);
        
        // View Services button
        gbc.gridx = 0;
        gbc.gridy = 4;
        JPanel viewServicesPanel = createMenuButton(
            "🔧 View Services",
            "Browse available drilling and pump services",
            new Color(255, 152, 0),
            e -> onViewServices()
        );
        add(viewServicesPanel, gbc);
        
        // View Reports button
        gbc.gridx = 1;
        JPanel viewReportsPanel = createMenuButton(
            "📊 View Reports",
            "View your account and service reports",
            new Color(156, 39, 176),
            e -> onViewReports()
        );
        add(viewReportsPanel, gbc);
        
        // Settings button
        gbc.gridx = 0;
        gbc.gridy = 5;
        JPanel settingsPanel = createMenuButton(
            "⚙️ Settings",
            "Update your profile and preferences",
            new Color(96, 125, 139),
            e -> onSettings()
        );
        add(settingsPanel, gbc);
        
        // Logout button
        gbc.gridx = 1;
        JPanel logoutPanel = createMenuButton(
            "🚪 Logout",
            "Sign out from your account",
            new Color(244, 67, 54),
            e -> onLogout()
        );
        add(logoutPanel, gbc);
        
        // Footer
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(50, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel footerLabel = new JLabel("© 2025 UZIMA Borehole Drilling Services | Contact: +254 700 000 001");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footerLabel.setForeground(new Color(150, 150, 150));
        footerLabel.setHorizontalAlignment(JLabel.CENTER);
        add(footerLabel, gbc);
    }

    private JPanel createMenuButton(String title, String description, Color bgColor, java.awt.event.ActionListener action) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(bgColor);
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.setPreferredSize(new Dimension(250, 100));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Description
        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(new Color(240, 240, 240));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(titleLabel);
        buttonPanel.add(Box.createVerticalStrut(8));
        buttonPanel.add(descLabel);
        buttonPanel.add(Box.createVerticalStrut(5));
        
        // Add hover effect
        buttonPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                action.actionPerformed(null);
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createRaisedBevelBorder(),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
                buttonPanel.setOpaque(true);
            }
        });
        
        return buttonPanel;
    }

    private void onRequestService() {
        JFrame wizardFrame = new JFrame("Service Request Wizard");
        wizardFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        wizardFrame.setSize(800, 700);
        wizardFrame.setLocationRelativeTo(null);
        wizardFrame.add(new ServiceRequestWizardManager(clientId));
        wizardFrame.setVisible(true);
    }

    private void onViewInvoices() {
        JFrame invoiceFrame = new JFrame("My Invoices");
        invoiceFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        invoiceFrame.setSize(900, 600);
        invoiceFrame.setLocationRelativeTo(null);
        invoiceFrame.add(new InvoiceListPanel(clientId));
        invoiceFrame.setVisible(true);
    }

    private void onViewServices() {
        JOptionPane.showMessageDialog(this,
            "View Services feature coming soon!\n" +
            "Browse all available drilling and pump services.",
            "Services",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void onViewReports() {
        JOptionPane.showMessageDialog(this,
            "View Reports feature coming soon!\n" +
            "Access your account reports and analytics.",
            "Reports",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void onSettings() {
        JOptionPane.showMessageDialog(this,
            "Settings feature coming soon!\n" +
            "Update your profile and preferences.",
            "Settings",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void onLogout() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            SwingUtilities.getWindowAncestor(this).dispose();
            // Show login screen
        }
    }
}
