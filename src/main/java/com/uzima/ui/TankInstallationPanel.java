package com.uzima.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Step 6: Tank Installation Panel (Optional)
 * User can add water tank installation
 */
public class TankInstallationPanel extends JPanel {
    private ServiceRequestWizardManager wizard;
    private JCheckBox includeTankCheckbox;
    private JPanel inputPanel;
    private JSpinner capacitySpinner;
    private JLabel costLabel;

    public TankInstallationPanel(ServiceRequestWizardManager wizard) {
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
        JLabel titleLabel = new JLabel("Step 6: Water Tank Installation (Optional)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(12, 45, 97));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);
        
        // Description
        JLabel descLabel = new JLabel("Add a water tank installation to your service request:");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(108, 117, 125));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 20, 20);
        add(descLabel, gbc);
        
        // Include tank checkbox
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 20, 10, 20);
        includeTankCheckbox = new JCheckBox("Include Water Tank Installation");
        includeTankCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        includeTankCheckbox.addActionListener(e -> onIncludeTankChanged());
        add(includeTankCheckbox, gbc);
        
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
        
        costLabel = new JLabel("Tank Cost: KES 0.00");
        costLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        costLabel.setForeground(new Color(12, 45, 97));
        costLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        costPanel.add(costLabel, BorderLayout.CENTER);
        add(costPanel, gbc);
        
        // Info panel
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 20, 10, 20);
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, gbc);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 246, 250));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Tank Specifications",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.PLAIN, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Tank capacity
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel capacityLabel = new JLabel("Tank Capacity (litres):");
        capacityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(capacityLabel, gbc);
        
        capacitySpinner = new JSpinner(new SpinnerNumberModel(5000, 1000, 50000, 1000));
        capacitySpinner.addChangeListener(e -> onTankCapacityChanged());
        gbc.gridx = 1;
        panel.add(capacitySpinner, gbc);
        
        // Cost per litre note
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JLabel noteLabel = new JLabel("Cost: 50 KES per 1,000 litres of capacity");
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        noteLabel.setForeground(new Color(100, 100, 100));
        panel.add(noteLabel, gbc);
        
        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(230, 240, 255));
        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 122, 204), 2));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel title = new JLabel("💧 Tank Information:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel info1 = new JLabel("• Standard capacity ranges from 1,000 to 50,000 litres");
        info1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel info2 = new JLabel("• Tank cost is calculated based on total capacity");
        info2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel info3 = new JLabel("• Underground tanks are more durable and space-efficient");
        info3.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(info1);
        panel.add(info2);
        panel.add(info3);
        
        return panel;
    }

    private void onIncludeTankChanged() {
        boolean include = includeTankCheckbox.isSelected();
        setComponentsEnabled(inputPanel, include);
        
        if (!include) {
            wizard.getRequestData().tankCost = 0;
            costLabel.setText("Tank Cost: KES 0.00");
        } else {
            onTankCapacityChanged();
        }
    }

    private void onTankCapacityChanged() {
        int capacity = (Integer) capacitySpinner.getValue();
        
        wizard.getRequestData().tankCapacityLitres = capacity;
        
        // Cost calculation: 50 KES per 1,000 litres
        double costPerThousandLitres = 50;
        double totalCost = (capacity / 1000.0) * costPerThousandLitres;
        
        wizard.getRequestData().tankCost = totalCost;
        costLabel.setText("Tank Cost: KES " + String.format("%.2f", totalCost) + 
                         " (" + capacity + " litres @ 50 KES/1000L)");
        
        System.out.println("Tank: " + capacity + "L = " + totalCost + " KES");
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
