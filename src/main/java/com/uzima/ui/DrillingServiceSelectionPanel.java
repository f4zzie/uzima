package com.uzima.ui;

import com.uzima.services.ServiceManagementService;
import com.uzima.models.DrillingService;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Step 2: Drilling Service Selection Panel
 * User selects drilling service type (Symmetric, Core, or Geo-Technical)
 */
public class DrillingServiceSelectionPanel extends JPanel {
    private ServiceRequestWizardManager wizard;
    private ButtonGroup serviceGroup;
    private JLabel descriptionLabel;

    public DrillingServiceSelectionPanel(ServiceRequestWizardManager wizard) {
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
        JLabel titleLabel = new JLabel("Step 2: Select Drilling Service");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(12, 45, 97));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);
        
        // Description
        JLabel descLabel = new JLabel("Choose the type of drilling service for your borehole:");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(108, 117, 125));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 20, 20);
        add(descLabel, gbc);
        
        // Get drilling services from database
        List<DrillingService> services = ServiceManagementService.getAllDrillingServices();
        serviceGroup = new ButtonGroup();
        
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 20, 10, 20);
        int row = 2;
        
        for (DrillingService service : services) {
            // Radio button
            JRadioButton serviceButton = new JRadioButton(service.getServiceType(), false);
            serviceButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            serviceButton.addActionListener(e -> onServiceSelected(service));
            serviceGroup.add(serviceButton);
            gbc.gridy = row;
            add(serviceButton, gbc);
            
            // Cost label
            JLabel costLabel = new JLabel("Down Payment: " + String.format("%.0f", service.getDownPaymentFee()) + " KES");
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
        
        descriptionLabel = new JLabel("Select a drilling service to see details");
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
        
        JLabel title = new JLabel("Drilling Types:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel symmetric = new JLabel("• Symmetric: Standard drilling method for general purposes");
        symmetric.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel core = new JLabel("• Core: Drilling with core extraction for geological analysis");
        core.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel geo = new JLabel("• Geo-Technical: Advanced drilling for technical assessment");
        geo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(symmetric);
        panel.add(core);
        panel.add(geo);
        
        return panel;
    }

    private void onServiceSelected(DrillingService service) {
        wizard.getRequestData().drillingType = service.getServiceType();
        wizard.getRequestData().drillingCost = service.getDownPaymentFee();
        
        descriptionLabel.setText("<html><b>" + service.getServiceType() + " Drilling:</b> " + 
                                 service.getDescription() + 
                                 "<br>Down Payment: KES " + String.format("%.0f", service.getDownPaymentFee()) + "</html>");
        
        System.out.println("Selected drilling service: " + service.getServiceType());
    }
}
