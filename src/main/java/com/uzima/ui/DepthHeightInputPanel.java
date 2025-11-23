package com.uzima.ui;

import com.uzima.services.InvoiceService;
import javax.swing.*;
import java.awt.*;

/**
 * Step 4: Depth & Height Input Panel
 * User enters borehole depth and tank height in meters
 */
public class DepthHeightInputPanel extends JPanel {
    private ServiceRequestWizardManager wizard;
    private JSpinner depthSpinner;
    private JSpinner heightSpinner;
    private JLabel depthChargeLabel;
    private JLabel totalChargeLabel;

    public DepthHeightInputPanel(ServiceRequestWizardManager wizard) {
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
        JLabel titleLabel = new JLabel("Step 4: Enter Borehole Depth & Tank Height");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(12, 45, 97));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);
        
        // Description
        JLabel descLabel = new JLabel("Specify the borehole depth and tank height. Charges are calculated based on depth ranges:");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(108, 117, 125));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 20, 20);
        add(descLabel, gbc);
        
        // Pricing info
        JLabel pricingLabel = new JLabel("1-100m: 1,000 KES/m  •  101-200m: 1,500 KES/m  •  201-300m: 2,000 KES/m  •  300m+: 2,500 KES/m");
        pricingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pricingLabel.setForeground(new Color(0, 122, 204));
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 20, 20, 20);
        add(pricingLabel, gbc);
        
        // Depth input
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 20, 10, 20);
        JLabel depthLabel = new JLabel("Borehole Depth (meters):");
        depthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(depthLabel, gbc);
        
        depthSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 10));
        depthSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        depthSpinner.addChangeListener(e -> onDepthOrHeightChanged());
        gbc.gridx = 1;
        add(depthSpinner, gbc);
        
        // Height input
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel heightLabel = new JLabel("Tank Height (meters):");
        heightLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(heightLabel, gbc);
        
        heightSpinner = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
        heightSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        heightSpinner.addChangeListener(e -> onDepthOrHeightChanged());
        gbc.gridx = 1;
        add(heightSpinner, gbc);
        
        // Charge calculation area
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 10, 20);
        
        JPanel chargePanel = new JPanel(new GridLayout(2, 1, 10, 10));
        chargePanel.setBackground(new Color(240, 248, 255));
        chargePanel.setBorder(BorderFactory.createLineBorder(new Color(52, 168, 224), 2));
        chargePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 168, 224), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        depthChargeLabel = new JLabel("Depth/Height Charge: KES 0.00");
        depthChargeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        depthChargeLabel.setForeground(new Color(0, 122, 204));
        
        totalChargeLabel = new JLabel("Total with Drilling: KES 0.00");
        totalChargeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalChargeLabel.setForeground(new Color(12, 45, 97));
        
        chargePanel.add(depthChargeLabel);
        chargePanel.add(totalChargeLabel);
        add(chargePanel, gbc);
        
        // Info panel
        gbc.gridy = 6;
        gbc.insets = new Insets(20, 20, 10, 20);
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, gbc);
        
        // Initialize calculations
        onDepthOrHeightChanged();
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(230, 240, 255));
        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 122, 204), 2));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel title = new JLabel("📏 Measurement Tips:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel depth = new JLabel("• Depth: The distance from ground level to bottom of borehole");
        depth.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel height = new JLabel("• Height: The height of water tank above ground level");
        height.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel charge = new JLabel("• Charge: Total meters (depth + height) × cost per meter for your range");
        charge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(depth);
        panel.add(height);
        panel.add(charge);
        
        return panel;
    }

    private void onDepthOrHeightChanged() {
        int depth = (Integer) depthSpinner.getValue();
        int height = (Integer) heightSpinner.getValue();
        
        wizard.getRequestData().depthMeters = depth;
        wizard.getRequestData().heightMeters = height;
        
        // Calculate depth/height charge
        double depthCharge = InvoiceService.calculateDepthHeightCharge(depth, height);
        wizard.getRequestData().depthHeightCharge = depthCharge;
        
        // Update labels
        depthChargeLabel.setText("Depth/Height Charge: KES " + String.format("%.2f", depthCharge));
        
        double drillingCost = wizard.getRequestData().drillingCost;
        double totalWithDrilling = drillingCost + depthCharge;
        totalChargeLabel.setText("Total with Drilling: KES " + String.format("%.2f", totalWithDrilling));
        
        System.out.println("Depth: " + depth + "m, Height: " + height + "m, Charge: " + depthCharge);
    }
}
