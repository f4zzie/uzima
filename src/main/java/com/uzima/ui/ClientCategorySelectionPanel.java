package com.uzima.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Step 1: Client Category Selection Panel
 * User selects their client category which determines survey and local authority fees
 */
public class ClientCategorySelectionPanel extends JPanel {
    private ServiceRequestWizardManager wizard;
    private JRadioButton industrialButton;
    private JRadioButton commercialButton;
    private JRadioButton domesticButton;

    public ClientCategorySelectionPanel(ServiceRequestWizardManager wizard) {
        this.wizard = wizard;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 246, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel titleLabel = new JLabel("Step 1: Select Client Category");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(12, 45, 97));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);
        
        // Description
        JLabel descLabel = new JLabel("Choose your client category to determine applicable fees:");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(108, 117, 125));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 20, 20);
        add(descLabel, gbc);
        
        // Radio buttons
        ButtonGroup group = new ButtonGroup();
        
        // Industrial
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 20, 10, 20);
        industrialButton = new JRadioButton("Industrial", false);
        industrialButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        industrialButton.addActionListener(e -> onCategorySelected("Industrial"));
        group.add(industrialButton);
        add(industrialButton, gbc);
        
        JLabel industrialFeeLabel = new JLabel("Survey: 20,000 KES | Local Authority: 50,000 KES");
        industrialFeeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        industrialFeeLabel.setForeground(new Color(100, 100, 100));
        gbc.gridx = 1;
        add(industrialFeeLabel, gbc);
        
        // Commercial
        gbc.gridx = 0;
        gbc.gridy = 3;
        commercialButton = new JRadioButton("Commercial", false);
        commercialButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        commercialButton.addActionListener(e -> onCategorySelected("Commercial"));
        group.add(commercialButton);
        add(commercialButton, gbc);
        
        JLabel commercialFeeLabel = new JLabel("Survey: 15,000 KES | Local Authority: 30,000 KES");
        commercialFeeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        commercialFeeLabel.setForeground(new Color(100, 100, 100));
        gbc.gridx = 1;
        add(commercialFeeLabel, gbc);
        
        // Domestic
        gbc.gridx = 0;
        gbc.gridy = 4;
        domesticButton = new JRadioButton("Domestic", false);
        domesticButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        domesticButton.addActionListener(e -> onCategorySelected("Domestic"));
        group.add(domesticButton);
        add(domesticButton, gbc);
        
        JLabel domesticFeeLabel = new JLabel("Survey: 7,000 KES | Local Authority: 10,000 KES");
        domesticFeeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        domesticFeeLabel.setForeground(new Color(100, 100, 100));
        gbc.gridx = 1;
        add(domesticFeeLabel, gbc);
        
        // Info panel
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 20, 10, 20);
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, gbc);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(230, 240, 255));
        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 122, 204), 2));
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        JLabel infoIcon = new JLabel("ℹ");
        infoIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        infoIcon.setForeground(new Color(0, 122, 204));
        
        JLabel infoText = new JLabel("<html>Your category determines the survey and local authority fees. " +
                "All services include a 16% tax on the total amount.</html>");
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        panel.add(infoIcon);
        panel.add(infoText);
        
        return panel;
    }

    private void onCategorySelected(String category) {
        wizard.getRequestData().clientCategory = category;
        System.out.println("Selected category: " + category);
    }
}
