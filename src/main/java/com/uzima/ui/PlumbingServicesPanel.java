package com.uzima.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Step 5: Plumbing Services Panel (Optional)
 * User can add plumbing services
 */
public class PlumbingServicesPanel extends JPanel {
    private ServiceRequestWizardManager wizard;
    private JCheckBox includePlumbingCheckbox;
    private JPanel inputPanel;
    private JSpinner lengthSpinner;
    private JSpinner diameterSpinner;
    private JSpinner outletsSpinner;
    private JLabel costLabel;

    public PlumbingServicesPanel(ServiceRequestWizardManager wizard) {
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
        JLabel titleLabel = new JLabel("Step 5: Plumbing Services (Optional)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(12, 45, 97));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);
        
        // Description
        JLabel descLabel = new JLabel("Add plumbing services if needed for your installation:");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(108, 117, 125));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 20, 20);
        add(descLabel, gbc);
        
        // Include plumbing checkbox
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 20, 10, 20);
        includePlumbingCheckbox = new JCheckBox("Include Plumbing Services");
        includePlumbingCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        includePlumbingCheckbox.addActionListener(e -> onIncludePlumbingChanged());
        add(includePlumbingCheckbox, gbc);
        
        // Input panel (initially disabled)
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        inputPanel = createInputPanel();
        inputPanel.setEnabled(false);
        add(inputPanel, gbc);
        
        // Cost display
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 10, 20);
        
        JPanel costPanel = new JPanel(new BorderLayout());
        costPanel.setBackground(new Color(240, 248, 255));
        costPanel.setBorder(BorderFactory.createLineBorder(new Color(52, 168, 224), 2));
        
        costLabel = new JLabel("Plumbing Cost: KES 0.00");
        costLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        costLabel.setForeground(new Color(12, 45, 97));
        costLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        costPanel.add(costLabel, BorderLayout.CENTER);
        add(costPanel, gbc);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 246, 250));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Plumbing Details",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.PLAIN, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Pipe length
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lengthLabel = new JLabel("Pipe Length (meters):");
        lengthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(lengthLabel, gbc);
        
        lengthSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 500, 5));
        lengthSpinner.addChangeListener(e -> onPlumbingDetailsChanged());
        gbc.gridx = 1;
        panel.add(lengthSpinner, gbc);
        
        // Pipe diameter
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel diameterLabel = new JLabel("Pipe Diameter (inches):");
        diameterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(diameterLabel, gbc);
        
        diameterSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        diameterSpinner.addChangeListener(e -> onPlumbingDetailsChanged());
        gbc.gridx = 1;
        panel.add(diameterSpinner, gbc);
        
        // Number of outlets
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel outletsLabel = new JLabel("Number of Outlets:");
        outletsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(outletsLabel, gbc);
        
        outletsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        outletsSpinner.addChangeListener(e -> onPlumbingDetailsChanged());
        gbc.gridx = 1;
        panel.add(outletsSpinner, gbc);
        
        // Cost per meter note
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JLabel noteLabel = new JLabel("Cost Calculation: (Pipe Length) × (Cost per Meter based on Diameter)");
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        noteLabel.setForeground(new Color(100, 100, 100));
        panel.add(noteLabel, gbc);
        
        return panel;
    }

    private void onIncludePlumbingChanged() {
        boolean include = includePlumbingCheckbox.isSelected();
        setComponentsEnabled(inputPanel, include);
        
        if (!include) {
            wizard.getRequestData().plumbingCost = 0;
            costLabel.setText("Plumbing Cost: KES 0.00");
        } else {
            onPlumbingDetailsChanged();
        }
    }

    private void onPlumbingDetailsChanged() {
        int length = (Integer) lengthSpinner.getValue();
        int diameter = (Integer) diameterSpinner.getValue();
        int outlets = (Integer) outletsSpinner.getValue();
        
        wizard.getRequestData().pipeLengthMeters = length;
        wizard.getRequestData().pipeDiameterInches = diameter;
        wizard.getRequestData().numberOfOutlets = outlets;
        
        // Simplified cost calculation: 500 KES per meter
        double costPerMeter = 500;
        double totalCost = length * costPerMeter;
        
        wizard.getRequestData().plumbingCost = totalCost;
        costLabel.setText("Plumbing Cost: KES " + String.format("%.2f", totalCost));
        
        System.out.println("Plumbing: " + length + "m @ " + costPerMeter + "/m = " + totalCost);
    }

    private void setComponentsEnabled(JComponent component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof JPanel) {
            for (java.awt.Component child : component.getComponents()) {
                if (child instanceof JComponent) {
                    setComponentsEnabled((JComponent) child, enabled);
                }
            }
        }
    }
}
