package com.uzima.ui;

import com.uzima.services.ServiceManagementService;
import com.uzima.models.PumpService;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Step 3: Pump Service Selection Panel
 * User selects pump type (Submersible Electric, Solar, or Hand)
 */
public class PumpServiceSelectionPanel extends JPanel {
    private ServiceRequestWizardManager wizard;
    private ButtonGroup pumpGroup;
    private JLabel descriptionLabel;

    public PumpServiceSelectionPanel(ServiceRequestWizardManager wizard) {
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
        JLabel titleLabel = new JLabel("Step 3: Select Pump Type");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(12, 45, 97));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);
        
        // Description
        JLabel descLabel = new JLabel("Choose the type of water pump for your borehole:");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(108, 117, 125));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 20, 20);
        add(descLabel, gbc);
        
        // Get pump services from database
        List<PumpService> services = ServiceManagementService.getAllPumpServices();
        pumpGroup = new ButtonGroup();
        
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 20, 10, 20);
        int row = 2;
        
        for (PumpService service : services) {
            // Radio button
            JRadioButton pumpButton = new JRadioButton(service.getPumpType(), false);
            pumpButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            pumpButton.addActionListener(e -> onPumpSelected(service));
            pumpGroup.add(pumpButton);
            gbc.gridy = row;
            add(pumpButton, gbc);
            
            // Cost label
            JLabel costLabel = new JLabel("Cost: " + String.format("%.0f", service.getCost()) + " KES");
            costLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            costLabel.setForeground(new Color(0, 122, 204));
            gbc.gridx = 1;
            add(costLabel, gbc);
            
            row++;
            gbc.gridx = 0;
        }
        
        // Description and cost area
        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 10, 20);
        
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(new Color(240, 248, 255));
        detailsPanel.setBorder(BorderFactory.createLineBorder(new Color(52, 168, 224), 2));
        
        descriptionLabel = new JLabel("Select a pump type to see details");
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descriptionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        detailsPanel.add(descriptionLabel, BorderLayout.CENTER);
        add(detailsPanel, gbc);
        
        // Info panel
        gbc.gridy = row + 2;
        gbc.insets = new Insets(20, 20, 10, 20);
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, gbc);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(230, 240, 255));
        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 122, 204), 2));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel title = new JLabel("Pump Types:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel submersible = new JLabel("• Submersible Electric: Powerful electric pump for deep wells");
        submersible.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel solar = new JLabel("• Solar: Eco-friendly solar-powered pump solution");
        solar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel hand = new JLabel("• Hand: Manual hand pump for backup/remote areas");
        hand.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(submersible);
        panel.add(solar);
        panel.add(hand);
        
        return panel;
    }

    private void onPumpSelected(PumpService service) {
        wizard.getRequestData().pumpType = service.getPumpType();
        wizard.getRequestData().pumpCost = service.getCost();
        
        descriptionLabel.setText("<html><b>" + service.getPumpType() + " Pump:</b> " + 
                                 service.getDescription() + 
                                 "<br>Installation Cost: KES " + String.format("%.0f", service.getCost()) + "</html>");
        
        System.out.println("Selected pump: " + service.getPumpType());
    }
}
