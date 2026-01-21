package com.uzima.ui;

import com.toedter.calendar.JDateChooser;
import com.uzima.dao.ClientDAO;
import com.uzima.dao.TransactionDAO;
import com.uzima.model.Client;
import com.uzima.model.Transaction;
import com.uzima.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * New Transaction Form - Complete billing and calculation module
 */
public class NewTransactionForm extends JFrame {
    private ClientDAO clientDAO;
    private TransactionDAO transactionDAO;
    
    // Transaction info
    private JTextField txtTransactionNo;
    private JDateChooser dateTransaction;
    private JComboBox<String> cmbClient;
    private Map<String, String> clientMap; // Display name -> Client ID
    private JTextField txtClientCategory;
    
    // Drilling section
    private JCheckBox chkDrilling;
    private JComboBox<String> cmbDrillingType;
    private JTextField txtDepth;
    private JTextField txtDrillingDownPayment;
    private JTextField txtDepthCost;
    private JTextField txtDrillingTotal;
    
    // Pump section
    private JCheckBox chkPump;
    private JComboBox<String> cmbPumpType;
    private JTextField txtHeight;
    private JTextField txtPumpBaseCost;
    private JTextField txtHeightCost;
    private JTextField txtPumpTotal;
    
    // Tank section
    private JCheckBox chkTank;
    private JComboBox<String> cmbTankType;
    private JTextField txtTankCost;
    
    // Plumbing section
    private JCheckBox chkPlumbing;
    private JTextField txtPlumbingCost;
    
    // Fees section
    private JTextField txtSurveyFee;
    private JTextField txtAuthorityFee;
    
    // Totals section
    private JTextField txtSubtotal;
    private JTextField txtTax;
    private JTextField txtGrandTotal;
    
    // Notes
    private JTextArea txtNotes;
    
    // Buttons
    private JButton btnCalculate, btnSave, btnClear, btnClose;
    
    // Data maps
    private Map<String, Double> drillingMap;
    private Map<String, Double> pumpMap;
    private Map<String, Double> tankMap;
    
    private static final double TAX_RATE = 0.16; // 16% tax
    
    public NewTransactionForm() {
        clientDAO = new ClientDAO();
        transactionDAO = new TransactionDAO();
        clientMap = new HashMap<>();
        drillingMap = new HashMap<>();
        pumpMap = new HashMap<>();
        tankMap = new HashMap<>();
        
        initComponents();
        loadClients();
        loadServices();
        setupListeners();
    }
    
    private void initComponents() {
        setTitle("New Transaction / Service Order");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(UIHelper.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIHelper.PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel("NEW TRANSACTION / SERVICE ORDER");
        lblTitle.setFont(UIHelper.TITLE_FONT);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        
        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(UIHelper.BACKGROUND_COLOR);
        
        // Left panel - Transaction details
        JPanel leftPanel = createLeftPanel();
        
        // Right panel - Calculations
        JPanel rightPanel = createRightPanel();
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(700);
        splitPane.setResizeWeight(0.5);
        
        contentPanel.add(splitPane, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = createButtonPanel();
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIHelper.BACKGROUND_COLOR);
        
        // Transaction Info Panel
        JPanel infoPanel = createTransactionInfoPanel();
        
        // Services Panel
        JPanel servicesPanel = new JPanel();
        servicesPanel.setLayout(new BoxLayout(servicesPanel, BoxLayout.Y_AXIS));
        servicesPanel.setBackground(UIHelper.BACKGROUND_COLOR);
        
        servicesPanel.add(createDrillingPanel());
        servicesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        servicesPanel.add(createPumpPanel());
        servicesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        servicesPanel.add(createTankPanel());
        servicesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        servicesPanel.add(createPlumbingPanel());
        
        JScrollPane scrollPane = new JScrollPane(servicesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTransactionInfoPanel() {
        JPanel panel = UIHelper.createTitledPanel("Transaction Information");
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Transaction Number
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panel.add(UIHelper.createFormLabel("Transaction No:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtTransactionNo = new JTextField();
        txtTransactionNo.setEditable(false);
        txtTransactionNo.setBackground(Color.LIGHT_GRAY);
        txtTransactionNo.setText(transactionDAO.generateTransactionNo());
        panel.add(txtTransactionNo, gbc);
        
        // Transaction Date
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(UIHelper.createFormLabel("Transaction Date:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        dateTransaction = new JDateChooser();
        dateTransaction.setDateFormatString("dd/MM/yyyy");
        dateTransaction.setDate(new Date());
        panel.add(dateTransaction, gbc);
        
        // Client Selection
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        panel.add(UIHelper.createFormLabel("Select Client: *"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbClient = new JComboBox<>();
        panel.add(cmbClient, gbc);
        
        // Client Category
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        panel.add(UIHelper.createFormLabel("Client Category:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtClientCategory = new JTextField();
        txtClientCategory.setEditable(false);
        txtClientCategory.setBackground(Color.LIGHT_GRAY);
        panel.add(txtClientCategory, gbc);
        
        return panel;
    }
    
    private JPanel createDrillingPanel() {
        JPanel panel = UIHelper.createTitledPanel("Drilling Service");
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Enable checkbox
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        chkDrilling = new JCheckBox("Include Drilling Service");
        chkDrilling.setFont(UIHelper.LABEL_FONT);
        panel.add(chkDrilling, gbc);
        
        gbc.gridwidth = 1;
        
        // Drilling Type
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(UIHelper.createFormLabel("Drilling Type:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 0.7;
        cmbDrillingType = new JComboBox<>();
        panel.add(cmbDrillingType, gbc);
        
        gbc.gridwidth = 1;
        
        // Down Payment
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(UIHelper.createFormLabel("Down Payment:"), gbc);
        
        gbc.gridx = 1;
        txtDrillingDownPayment = new JTextField();
        txtDrillingDownPayment.setEditable(false);
        txtDrillingDownPayment.setBackground(Color.LIGHT_GRAY);
        panel.add(txtDrillingDownPayment, gbc);
        
        // Depth
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(UIHelper.createFormLabel("Borehole Depth (m):"), gbc);
        
        gbc.gridx = 1;
        txtDepth = new JTextField("0");
        panel.add(txtDepth, gbc);
        
        // Depth Cost
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(UIHelper.createFormLabel("Depth Cost:"), gbc);
        
        gbc.gridx = 1;
        txtDepthCost = new JTextField("0");
        txtDepthCost.setEditable(false);
        txtDepthCost.setBackground(Color.LIGHT_GRAY);
        panel.add(txtDepthCost, gbc);
        
        // Total Drilling Cost
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel lblDrillingTotal = UIHelper.createFormLabel("Total Drilling Cost:");
        lblDrillingTotal.setFont(UIHelper.LABEL_FONT);
        panel.add(lblDrillingTotal, gbc);
        
        gbc.gridx = 1;
        txtDrillingTotal = new JTextField("0");
        txtDrillingTotal.setEditable(false);
        txtDrillingTotal.setBackground(new Color(200, 255, 200));
        txtDrillingTotal.setFont(UIHelper.LABEL_FONT);
        panel.add(txtDrillingTotal, gbc);
        
        return panel;
    }
    
    private JPanel createPumpPanel() {
        JPanel panel = UIHelper.createTitledPanel("Pump Installation");
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Enable checkbox
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        chkPump = new JCheckBox("Include Pump Installation");
        chkPump.setFont(UIHelper.LABEL_FONT);
        panel.add(chkPump, gbc);
        
        gbc.gridwidth = 1;
        
        // Pump Type
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(UIHelper.createFormLabel("Pump Type:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 0.7;
        cmbPumpType = new JComboBox<>();
        panel.add(cmbPumpType, gbc);
        
        gbc.gridwidth = 1;
        
        // Base Cost
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(UIHelper.createFormLabel("Base Cost:"), gbc);
        
        gbc.gridx = 1;
        txtPumpBaseCost = new JTextField();
        txtPumpBaseCost.setEditable(false);
        txtPumpBaseCost.setBackground(Color.LIGHT_GRAY);
        panel.add(txtPumpBaseCost, gbc);
        
        // Tank Height
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(UIHelper.createFormLabel("Tank Height (m):"), gbc);
        
        gbc.gridx = 1;
        txtHeight = new JTextField("0");
        panel.add(txtHeight, gbc);
        
        // Height Cost
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(UIHelper.createFormLabel("Height Cost:"), gbc);
        
        gbc.gridx = 1;
        txtHeightCost = new JTextField("0");
        txtHeightCost.setEditable(false);
        txtHeightCost.setBackground(Color.LIGHT_GRAY);
        panel.add(txtHeightCost, gbc);
        
        // Total Pump Cost
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel lblPumpTotal = UIHelper.createFormLabel("Total Pump Cost:");
        lblPumpTotal.setFont(UIHelper.LABEL_FONT);
        panel.add(lblPumpTotal, gbc);
        
        gbc.gridx = 1;
        txtPumpTotal = new JTextField("0");
        txtPumpTotal.setEditable(false);
        txtPumpTotal.setBackground(new Color(200, 255, 200));
        txtPumpTotal.setFont(UIHelper.LABEL_FONT);
        panel.add(txtPumpTotal, gbc);
        
        return panel;
    }
    
    private JPanel createTankPanel() {
        JPanel panel = UIHelper.createTitledPanel("Tank Installation");
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Enable checkbox
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        chkTank = new JCheckBox("Include Tank Installation");
        chkTank.setFont(UIHelper.LABEL_FONT);
        panel.add(chkTank, gbc);
        
        gbc.gridwidth = 1;
        
        // Tank Type
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panel.add(UIHelper.createFormLabel("Tank Capacity:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 0.7;
        cmbTankType = new JComboBox<>();
        panel.add(cmbTankType, gbc);
        
        gbc.gridwidth = 1;
        
        // Tank Cost
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblTankCost = UIHelper.createFormLabel("Tank Cost:");
        lblTankCost.setFont(UIHelper.LABEL_FONT);
        panel.add(lblTankCost, gbc);
        
        gbc.gridx = 1;
        txtTankCost = new JTextField("0");
        txtTankCost.setEditable(false);
        txtTankCost.setBackground(new Color(200, 255, 200));
        txtTankCost.setFont(UIHelper.LABEL_FONT);
        panel.add(txtTankCost, gbc);
        
        return panel;
    }
    
    private JPanel createPlumbingPanel() {
        JPanel panel = UIHelper.createTitledPanel("Plumbing Service");
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Enable checkbox
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        chkPlumbing = new JCheckBox("Include Plumbing Service");
        chkPlumbing.setFont(UIHelper.LABEL_FONT);
        panel.add(chkPlumbing, gbc);
        
        gbc.gridwidth = 1;
        
        // Plumbing Cost
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblPlumbingCost = UIHelper.createFormLabel("Plumbing Cost:");
        lblPlumbingCost.setFont(UIHelper.LABEL_FONT);
        panel.add(lblPlumbingCost, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtPlumbingCost = new JTextField("0");
        txtPlumbingCost.setFont(UIHelper.LABEL_FONT);
        panel.add(txtPlumbingCost, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        JLabel lblNote = new JLabel("(Enter manual plumbing cost or use detailed calculator)");
        lblNote.setFont(UIHelper.TEXT_FONT);
        lblNote.setForeground(Color.GRAY);
        panel.add(lblNote, gbc);
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIHelper.BACKGROUND_COLOR);
        
        // Fees Panel
        JPanel feesPanel = createFeesPanel();
        
        // Totals Panel
        JPanel totalsPanel = createTotalsPanel();
        
        // Notes Panel
        JPanel notesPanel = createNotesPanel();
        
        JPanel combinedPanel = new JPanel();
        combinedPanel.setLayout(new BoxLayout(combinedPanel, BoxLayout.Y_AXIS));
        combinedPanel.setBackground(UIHelper.BACKGROUND_COLOR);
        combinedPanel.add(feesPanel);
        combinedPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        combinedPanel.add(totalsPanel);
        combinedPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        combinedPanel.add(notesPanel);
        
        panel.add(combinedPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFeesPanel() {
        JPanel panel = UIHelper.createTitledPanel("Fees");
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Survey Fee
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        JLabel lblSurvey = UIHelper.createFormLabel("Survey Fee:");
        lblSurvey.setFont(UIHelper.LABEL_FONT);
        panel.add(lblSurvey, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.6;
        txtSurveyFee = new JTextField("0");
        txtSurveyFee.setEditable(false);
        txtSurveyFee.setBackground(Color.LIGHT_GRAY);
        txtSurveyFee.setFont(UIHelper.LABEL_FONT);
        panel.add(txtSurveyFee, gbc);
        
        // Authority Fee
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblAuthority = UIHelper.createFormLabel("Authority Fee:");
        lblAuthority.setFont(UIHelper.LABEL_FONT);
        panel.add(lblAuthority, gbc);
        
        gbc.gridx = 1;
        txtAuthorityFee = new JTextField("0");
        txtAuthorityFee.setEditable(false);
        txtAuthorityFee.setBackground(Color.LIGHT_GRAY);
        txtAuthorityFee.setFont(UIHelper.LABEL_FONT);
        panel.add(txtAuthorityFee, gbc);
        
        return panel;
    }
    
    private JPanel createTotalsPanel() {
        JPanel panel = UIHelper.createTitledPanel("Totals");
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Subtotal
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        JLabel lblSubtotal = UIHelper.createFormLabel("Subtotal:");
        lblSubtotal.setFont(UIHelper.HEADER_FONT);
        panel.add(lblSubtotal, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.6;
        txtSubtotal = new JTextField("0");
        txtSubtotal.setEditable(false);
        txtSubtotal.setBackground(Color.WHITE);
        txtSubtotal.setFont(UIHelper.HEADER_FONT);
        panel.add(txtSubtotal, gbc);
        
        // Tax (16%)
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblTax = UIHelper.createFormLabel("Tax (16%):");
        lblTax.setFont(UIHelper.HEADER_FONT);
        panel.add(lblTax, gbc);
        
        gbc.gridx = 1;
        txtTax = new JTextField("0");
        txtTax.setEditable(false);
        txtTax.setBackground(Color.WHITE);
        txtTax.setFont(UIHelper.HEADER_FONT);
        panel.add(txtTax, gbc);
        
        // Grand Total
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblGrandTotal = UIHelper.createFormLabel("GRAND TOTAL:");
        lblGrandTotal.setFont(UIHelper.HEADER_FONT);
        lblGrandTotal.setForeground(UIHelper.SUCCESS_COLOR);
        panel.add(lblGrandTotal, gbc);
        
        gbc.gridx = 1;
        txtGrandTotal = new JTextField("0");
        txtGrandTotal.setEditable(false);
        txtGrandTotal.setBackground(new Color(200, 255, 200));
        txtGrandTotal.setFont(new Font("Arial", Font.BOLD, 20));
        txtGrandTotal.setForeground(UIHelper.SUCCESS_COLOR);
        panel.add(txtGrandTotal, gbc);
        
        return panel;
    }
    
    private JPanel createNotesPanel() {
        JPanel panel = UIHelper.createTitledPanel("Notes");
        panel.setLayout(new BorderLayout());
        
        txtNotes = new JTextArea(5, 30);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(txtNotes);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(UIHelper.BACKGROUND_COLOR);
        
        btnCalculate = new JButton("CALCULATE TOTAL");
        btnSave = new JButton("SAVE TRANSACTION");
        btnClear = new JButton("CLEAR FORM");
        btnClose = new JButton("CLOSE");
        
        Dimension btnSize = new Dimension(180, 40);
        btnCalculate.setPreferredSize(btnSize);
        btnSave.setPreferredSize(btnSize);
        btnClear.setPreferredSize(btnSize);
        btnClose.setPreferredSize(btnSize);
        
        UIHelper.stylePrimaryButton(btnCalculate);
        UIHelper.styleSuccessButton(btnSave);
        UIHelper.styleSecondaryButton(btnClear);
        UIHelper.styleDangerButton(btnClose);
        
        btnCalculate.addActionListener(e -> calculateTotals());
        btnSave.addActionListener(e -> saveTransaction());
        btnClear.addActionListener(e -> clearForm());
        btnClose.addActionListener(e -> dispose());
        
        panel.add(btnCalculate);
        panel.add(btnSave);
        panel.add(btnClear);
        panel.add(btnClose);
        
        return panel;
    }
    
    private void setupListeners() {
        // Client selection listener
        cmbClient.addActionListener(e -> {
            String selectedDisplay = (String) cmbClient.getSelectedItem();
            if (selectedDisplay != null && !selectedDisplay.isEmpty()) {
                String clientId = clientMap.get(selectedDisplay);
                Client client = clientDAO.getClientById(clientId);
                if (client != null) {
                    txtClientCategory.setText(client.getClientCategory());
                    loadFeesForCategory(client.getClientCategory());
                }
            }
        });
        
        // Drilling type listener
        cmbDrillingType.addActionListener(e -> {
            String selected = (String) cmbDrillingType.getSelectedItem();
            if (selected != null && drillingMap.containsKey(selected)) {
                txtDrillingDownPayment.setText(String.format("%.2f", drillingMap.get(selected)));
            }
        });
        
        // Pump type listener
        cmbPumpType.addActionListener(e -> {
            String selected = (String) cmbPumpType.getSelectedItem();
            if (selected != null && pumpMap.containsKey(selected)) {
                txtPumpBaseCost.setText(String.format("%.2f", pumpMap.get(selected)));
            }
        });
        
        // Tank type listener
        cmbTankType.addActionListener(e -> {
            String selected = (String) cmbTankType.getSelectedItem();
            if (selected != null && tankMap.containsKey(selected)) {
                txtTankCost.setText(String.format("%.2f", tankMap.get(selected)));
            }
        });
        
        // Depth change listener
        txtDepth.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                calculateDepthCost();
            }
        });
        
        // Height change listener
        txtHeight.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                calculateHeightCost();
            }
        });
    }
    
    private void loadClients() {
        List<Client> clients = clientDAO.getAllClients();
        cmbClient.removeAllItems();
        cmbClient.addItem("");
        clientMap.clear();
        
        for (Client client : clients) {
            String display = client.getClientId() + " - " + client.getFullName();
            cmbClient.addItem(display);
            clientMap.put(display, client.getClientId());
        }
    }
    
    private void loadServices() {
        // Load drilling types
        List<String[]> drillingTypes = transactionDAO.getDrillingTypes();
        cmbDrillingType.addItem("");
        for (String[] type : drillingTypes) {
            String name = type[1];
            double cost = Double.parseDouble(type[2]);
            cmbDrillingType.addItem(name);
            drillingMap.put(name, cost);
        }
        
        // Load pump types
        List<String[]> pumpTypes = transactionDAO.getPumpTypes();
        cmbPumpType.addItem("");
        for (String[] type : pumpTypes) {
            String name = type[1];
            double cost = Double.parseDouble(type[2]);
            cmbPumpType.addItem(name);
            pumpMap.put(name, cost);
        }
        
        // Load tank types
        List<String[]> tankTypes = transactionDAO.getTankTypes();
        cmbTankType.addItem("");
        for (String[] type : tankTypes) {
            String capacity = type[1] + "L";
            double cost = Double.parseDouble(type[2]);
            cmbTankType.addItem(capacity);
            tankMap.put(capacity, cost);
        }
    }
    
    private void loadFeesForCategory(String category) {
        double[] fees = transactionDAO.getSurveyAuthorityFees(category);
        txtSurveyFee.setText(String.format("%.2f", fees[0]));
        txtAuthorityFee.setText(String.format("%.2f", fees[1]));
    }
    
    private void calculateDepthCost() {
        try {
            double depth = Double.parseDouble(txtDepth.getText().trim());
            double depthCost = TransactionDAO.calculateDepthCost(depth);
            txtDepthCost.setText(String.format("%.2f", depthCost));
            calculateDrillingTotal();
        } catch (NumberFormatException e) {
            txtDepthCost.setText("0");
        } catch (SQLException e) {
            txtDepthCost.setText("0");
            UIHelper.showError(this, "Error calculating depth cost: " + e.getMessage());
        }
    }
    
    private void calculateHeightCost() {
        try {
            double height = Double.parseDouble(txtHeight.getText().trim());
            double heightCost = TransactionDAO.calculateDepthCost(height);
            txtHeightCost.setText(String.format("%.2f", heightCost));
            calculatePumpTotal();
        } catch (NumberFormatException e) {
            txtHeightCost.setText("0");
        } catch (SQLException e) {
            txtHeightCost.setText("0");
            UIHelper.showError(this, "Error calculating height cost: " + e.getMessage());
        }
    }
    
    private void calculateDrillingTotal() {
        try {
            double downPayment = Double.parseDouble(txtDrillingDownPayment.getText().trim());
            double depthCost = Double.parseDouble(txtDepthCost.getText().trim());
            double total = downPayment + depthCost;
            txtDrillingTotal.setText(String.format("%.2f", total));
        } catch (NumberFormatException e) {
            txtDrillingTotal.setText("0");
        }
    }
    
    private void calculatePumpTotal() {
        try {
            double baseCost = Double.parseDouble(txtPumpBaseCost.getText().trim());
            double heightCost = Double.parseDouble(txtHeightCost.getText().trim());
            double total = baseCost + heightCost;
            txtPumpTotal.setText(String.format("%.2f", total));
        } catch (NumberFormatException e) {
            txtPumpTotal.setText("0");
        }
    }
    
    private void calculateTotals() {
        try {
            double subtotal = 0;
            
            // Add drilling cost
            if (chkDrilling.isSelected()) {
                subtotal += Double.parseDouble(txtDrillingTotal.getText().trim());
            }
            
            // Add pump cost
            if (chkPump.isSelected()) {
                subtotal += Double.parseDouble(txtPumpTotal.getText().trim());
            }
            
            // Add tank cost
            if (chkTank.isSelected()) {
                subtotal += Double.parseDouble(txtTankCost.getText().trim());
            }
            
            // Add plumbing cost
            if (chkPlumbing.isSelected()) {
                subtotal += Double.parseDouble(txtPlumbingCost.getText().trim());
            }
            
            // Add fees
            subtotal += Double.parseDouble(txtSurveyFee.getText().trim());
            subtotal += Double.parseDouble(txtAuthorityFee.getText().trim());
            
            // Calculate tax
            double tax = subtotal * TAX_RATE;
            
            // Calculate grand total
            double grandTotal = subtotal + tax;
            
            txtSubtotal.setText(String.format("%.2f", subtotal));
            txtTax.setText(String.format("%.2f", tax));
            txtGrandTotal.setText(String.format("%.2f", grandTotal));
            
            UIHelper.showSuccess(this, "Calculation completed successfully!");
            
        } catch (NumberFormatException e) {
            UIHelper.showError(this, "Invalid number format in one or more fields!");
        }
    }
    
    private void saveTransaction() {
        // Validate
        if (cmbClient.getSelectedIndex() == 0) {
            UIHelper.showError(this, "Please select a client!");
            return;
        }
        
        if (!chkDrilling.isSelected() && !chkPump.isSelected() && 
            !chkTank.isSelected() && !chkPlumbing.isSelected()) {
            UIHelper.showError(this, "Please select at least one service!");
            return;
        }
        
        // Calculate if not done
        calculateTotals();
        
        try {
            Transaction transaction = new Transaction();
            
            // Basic info
            transaction.setTransactionNo(txtTransactionNo.getText().trim());
            String selectedClient = (String) cmbClient.getSelectedItem();
            transaction.setClientId(clientMap.get(selectedClient));
            transaction.setTransactionDate(dateTransaction.getDate());
            
            // Drilling
            if (chkDrilling.isSelected()) {
                String drillingType = (String) cmbDrillingType.getSelectedItem();
                // Find drilling type ID (simplified - you'd query the database)
                transaction.setDrillingTypeId(1); // Placeholder
                transaction.setBoreholeDepth(Double.parseDouble(txtDepth.getText().trim()));
                transaction.setDrillingCost(Double.parseDouble(txtDrillingTotal.getText().trim()));
            }
            
            // Pump
            if (chkPump.isSelected()) {
                transaction.setPumpTypeId(1); // Placeholder
                transaction.setTankHeight(Double.parseDouble(txtHeight.getText().trim()));
                transaction.setPumpCost(Double.parseDouble(txtPumpTotal.getText().trim()));
            }
            
            // Tank
            if (chkTank.isSelected()) {
                transaction.setTankTypeId(1); // Placeholder
                transaction.setTankCost(Double.parseDouble(txtTankCost.getText().trim()));
            }
            
            // Plumbing
            if (chkPlumbing.isSelected()) {
                transaction.setHasPlumbing(true);
                transaction.setPlumbingCost(Double.parseDouble(txtPlumbingCost.getText().trim()));
            }
            
            // Fees and totals
            transaction.setSurveyFee(Double.parseDouble(txtSurveyFee.getText().trim()));
            transaction.setAuthorityFee(Double.parseDouble(txtAuthorityFee.getText().trim()));
            transaction.setSubtotal(Double.parseDouble(txtSubtotal.getText().trim()));
            transaction.setTaxAmount(Double.parseDouble(txtTax.getText().trim()));
            transaction.setGrandTotal(Double.parseDouble(txtGrandTotal.getText().trim()));
            
            // Payment status
            transaction.setPaymentStatus("NOT_PAID");
            transaction.setAmountPaid(0);
            transaction.setBalance(transaction.getGrandTotal());
            
            // Job status
            transaction.setJobStatus("PENDING_SURVEY");
            
            // Notes
            transaction.setNotes(txtNotes.getText().trim());
            
            // Save
            if (transactionDAO.saveTransaction(transaction)) {
                UIHelper.showSuccess(this, "Transaction saved successfully!\n\nTransaction No: " + 
                                    transaction.getTransactionNo() + "\nGrand Total: KSh " + 
                                    String.format("%.2f", transaction.getGrandTotal()));
                clearForm();
            } else {
                UIHelper.showError(this, "Failed to save transaction!");
            }
            
        } catch (Exception e) {
            UIHelper.showError(this, "Error saving transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void clearForm() {
        txtTransactionNo.setText(transactionDAO.generateTransactionNo());
        dateTransaction.setDate(new Date());
        cmbClient.setSelectedIndex(0);
        txtClientCategory.setText("");
        
        chkDrilling.setSelected(false);
        cmbDrillingType.setSelectedIndex(0);
        txtDepth.setText("0");
        txtDrillingDownPayment.setText("0");
        txtDepthCost.setText("0");
        txtDrillingTotal.setText("0");
        
        chkPump.setSelected(false);
        cmbPumpType.setSelectedIndex(0);
        txtHeight.setText("0");
        txtPumpBaseCost.setText("0");
        txtHeightCost.setText("0");
        txtPumpTotal.setText("0");
        
        chkTank.setSelected(false);
        cmbTankType.setSelectedIndex(0);
        txtTankCost.setText("0");
        
        chkPlumbing.setSelected(false);
        txtPlumbingCost.setText("0");
        
        txtSurveyFee.setText("0");
        txtAuthorityFee.setText("0");
        txtSubtotal.setText("0");
        txtTax.setText("0");
        txtGrandTotal.setText("0");
        txtNotes.setText("");
    }
}
