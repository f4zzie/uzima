package com.uzima.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel manager for multi-step service request wizard
 * Handles navigation between different service request steps
 */
public class ServiceRequestWizardManager extends JPanel {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private List<JPanel> steps;
    private int currentStep;
    private int clientId;
    
    // Store service request data
    private ServiceRequestData requestData;

    public ServiceRequestWizardManager(int clientId) {
        this.clientId = clientId;
        this.requestData = new ServiceRequestData();
        this.currentStep = 0;
        
        setLayout(new BorderLayout());
        
        // Create card layout for steps
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        // Initialize all steps
        steps = new ArrayList<>();
        initializeSteps();
        
        // Add card panel and controls
        add(cardPanel, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
        
        // Show first step
        showStep(0);
    }

    private void initializeSteps() {
        // Step 0: Client Category Selection
        steps.add(new ClientCategorySelectionPanel(this));
        cardPanel.add(steps.get(0), "step0");
        
        // Step 1: Drilling Service Selection
        steps.add(new DrillingServiceSelectionPanel(this));
        cardPanel.add(steps.get(1), "step1");
        
        // Step 2: Pump Service Selection
        steps.add(new PumpServiceSelectionPanel(this));
        cardPanel.add(steps.get(2), "step2");
        
        // Step 3: Depth & Height Input
        steps.add(new DepthHeightInputPanel(this));
        cardPanel.add(steps.get(3), "step3");
        
        // Step 4: Plumbing Services (Optional)
        steps.add(new PlumbingServicesPanel(this));
        cardPanel.add(steps.get(4), "step4");
        
        // Step 5: Tank Installation (Optional)
        steps.add(new TankInstallationPanel(this));
        cardPanel.add(steps.get(5), "step5");
        
        // Step 6: Invoice Preview & Confirmation
        steps.add(new InvoicePreviewPanel(this));
        cardPanel.add(steps.get(6), "step6");
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottomPanel.setBackground(new Color(245, 246, 250));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));
        
        JButton prevButton = new JButton("← Previous");
        JButton nextButton = new JButton("Next →");
        JButton cancelButton = new JButton("✖ Cancel");
        
        // Style buttons
        styleButton(prevButton, new Color(100, 150, 200)); // Light blue
        styleButton(nextButton, new Color(76, 175, 80));   // Green
        styleButton(cancelButton, new Color(244, 67, 54)); // Red
        
        prevButton.addActionListener(e -> previousStep());
        nextButton.addActionListener(e -> nextStep());
        cancelButton.addActionListener(e -> cancelWizard());
        
        // Progress label with styling
        JLabel progressLabel = new JLabel("Step 1 of 7");
        progressLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        progressLabel.setForeground(new Color(12, 45, 97));
        
        bottomPanel.add(progressLabel);
        bottomPanel.add(Box.createHorizontalStrut(100));
        bottomPanel.add(cancelButton);
        bottomPanel.add(prevButton);
        bottomPanel.add(nextButton);
        
        return bottomPanel;
    }
    
    private void styleButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            private Color originalColor;
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                originalColor = button.getBackground();
                button.setBackground(new Color(
                    Math.max(0, originalColor.getRed() - 20),
                    Math.max(0, originalColor.getGreen() - 20),
                    Math.max(0, originalColor.getBlue() - 20)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
    }

    public void showStep(int stepNumber) {
        if (stepNumber >= 0 && stepNumber < steps.size()) {
            currentStep = stepNumber;
            cardLayout.show(cardPanel, "step" + stepNumber);
        }
    }

    public void nextStep() {
        if (currentStep < steps.size() - 1) {
            showStep(currentStep + 1);
        } else {
            // Submit the request
            submitServiceRequest();
        }
    }

    public void previousStep() {
        if (currentStep > 0) {
            showStep(currentStep - 1);
        }
    }

    private void submitServiceRequest() {
        JOptionPane.showMessageDialog(this, 
            "Service request submitted successfully!\n" +
            "Invoice ID: " + requestData.invoiceId +
            "\nTotal Amount: KES " + String.format("%.2f", requestData.totalAmount),
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
        
        // Close wizard or navigate to next screen
    }

    private void cancelWizard() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel this service request?",
            "Cancel Request",
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            // Close wizard
        }
    }

    public ServiceRequestData getRequestData() {
        return requestData;
    }

    public int getClientId() {
        return clientId;
    }

    /**
     * Data class to store request information across steps
     */
    public static class ServiceRequestData {
        public String clientCategory;
        public String drillingType;
        public String pumpType;
        public int depthMeters;
        public int heightMeters;
        public String pipeType;
        public int pipeDiameterInches;
        public int pipeLengthMeters;
        public int numberOfOutlets;
        public int tankCapacityLitres;
        
        // Calculated amounts
        public double surveyFee;
        public double localAuthorityFee;
        public double drillingCost;
        public double pumpCost;
        public double plumbingCost;
        public double tankCost;
        public double depthHeightCharge;
        public double subtotal;
        public double taxAmount;
        public double totalAmount;
        public int invoiceId;
    }
}
