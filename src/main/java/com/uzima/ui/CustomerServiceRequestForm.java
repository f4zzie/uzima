package com.uzima.ui;

import com.uzima.dao.TransactionDAO;
import com.uzima.model.Client;
import com.uzima.model.Transaction;
import com.uzima.util.UIHelper;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Customer Service Request Form - Allows customers to request borehole services
 * Includes all pricing from official rate tables
 */
public class CustomerServiceRequestForm extends JDialog {
    private Client currentClient;
    
    // ===== OFFICIAL PRICING TABLES =====
    // Table 1: Survey and Local Authority Fees by Client Category
    private static final int SURVEY_FEE_INDUSTRIAL = 20000;
    private static final int SURVEY_FEE_COMMERCIAL = 15000;
    private static final int SURVEY_FEE_DOMESTIC = 7000;
    
    private static final int LOCAL_AUTH_FEE_INDUSTRIAL = 50000;
    private static final int LOCAL_AUTH_FEE_COMMERCIAL = 30000;
    private static final int LOCAL_AUTH_FEE_DOMESTIC = 10000;
    
    // Table 2: Drilling Services (Down Payment Fee)
    private static final int DRILLING_SYMMETRIC = 130000;
    private static final int DRILLING_CORE = 225000;
    private static final int DRILLING_GEO_TECHNICAL = 335000;
    
    // Table 3: Pump Installation
    private static final int PUMP_SUBMERSIBLE_ELECTRIC = 90000;
    private static final int PUMP_SOLAR = 65000;
    private static final int PUMP_HAND = 30000;
    
    // Table 4: Depth/Height Cost per Meter
    // 1-100m = KSh 1,000/m, 101-200m = KSh 1,500/m, 201-300m = KSh 2,000/m, 300+ = KSh 2,500/m
    
    // Tax Rate
    private static final double TAX_RATE = 0.16;
    
    // Colors
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color PRIMARY_GREEN = new Color(34, 197, 94);
    private static final Color DARK_BLUE = new Color(30, 58, 138);
    private static final Color LIGHT_BG = new Color(248, 250, 252);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(71, 85, 105);
    
    // Service Selection
    private JCheckBox chkDrilling, chkPump, chkTank, chkPlumbing, chkMaintenance;
    
    // Drilling Options
    private JComboBox<String> cmbDrillingType;
    private JSpinner spnBoreholeDepth;
    
    // Pump Options
    private JComboBox<String> cmbPumpType;
    private JSpinner spnTankHeight;
    
    // Tank Options
    private JSpinner spnTankCapacity;
    
    // Plumbing Options
    private JComboBox<String> cmbPipeType;
    private JSpinner spnPipeDiameter;
    private JSpinner spnPipeLength;
    private JSpinner spnOutlets;
    
    // Notes
    private JTextArea txtNotes;
    private JTextArea txtBoreholeLocation;
    
    // Cost display labels
    private JLabel lblSurveyFee, lblLocalAuthFee, lblDrillingCost, lblPumpCost;
    private JLabel lblDepthCost, lblHeightCost, lblTankCost, lblPlumbingCost, lblMaintenanceCost;
    private JLabel lblSubtotal, lblTax, lblGrandTotal;
    
    public CustomerServiceRequestForm(JFrame parent, Client client) {
        super(parent, "Request Borehole Services", true);
        this.currentClient = client;
        // Make the dialog nearly full screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int)(screenSize.width * 0.95), (int)(screenSize.height * 0.90));
        setLocationRelativeTo(parent);
        setResizable(true);
        initComponents();
        calculateTotal();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Header
        mainPanel.add(createHeader(), BorderLayout.NORTH);
        
        // Content - Split into left (services) and right (cost summary)
        JPanel content = new JPanel(new BorderLayout(20, 0));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        // Scrollable services panel
        JScrollPane servicesScroll = new JScrollPane(createServicesPanel());
        servicesScroll.setBorder(null);
        servicesScroll.getVerticalScrollBar().setUnitIncrement(16);
        
        content.add(servicesScroll, BorderLayout.CENTER);
        content.add(createSummaryPanel(), BorderLayout.EAST);
        
        mainPanel.add(content, BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_BLUE, getWidth(), 0, DARK_BLUE);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(1100, 100));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel lblTitle = new JLabel("🛒 Request Borehole Services");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblSubtitle = new JLabel("Select the services you need - All prices are in Kenya Shillings (KSh)");
        lblSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSubtitle.setForeground(new Color(200, 220, 255));
        
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblSubtitle);
        
        JPanel clientPanel = new JPanel();
        clientPanel.setOpaque(false);
        clientPanel.setLayout(new BoxLayout(clientPanel, BoxLayout.Y_AXIS));
        
        JLabel lblClient = new JLabel("Customer: " + currentClient.getFullName());
        lblClient.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblClient.setForeground(Color.WHITE);
        lblClient.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JLabel lblCategory = new JLabel("Category: " + currentClient.getClientCategory());
        lblCategory.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCategory.setForeground(new Color(200, 220, 255));
        lblCategory.setHorizontalAlignment(SwingConstants.RIGHT);
        
        clientPanel.add(lblClient);
        clientPanel.add(Box.createVerticalStrut(3));
        clientPanel.add(lblCategory);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(clientPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createServicesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        // Borehole Location Section
        panel.add(createLocationSection());
        panel.add(Box.createVerticalStrut(15));
        
        // Mandatory Fees Info
        panel.add(createMandatoryFeesSection());
        panel.add(Box.createVerticalStrut(15));
        
        // Drilling Section
        panel.add(createDrillingSection());
        panel.add(Box.createVerticalStrut(15));
        
        // Pump Section
        panel.add(createPumpSection());
        panel.add(Box.createVerticalStrut(15));
        
        // Tank Section
        panel.add(createTankSection());
        panel.add(Box.createVerticalStrut(15));
        
        // Plumbing Section
        panel.add(createPlumbingSection());
        panel.add(Box.createVerticalStrut(15));
        
        // Maintenance Section
        panel.add(createMaintenanceSection());
        panel.add(Box.createVerticalStrut(15));
        
        // Notes Section
        panel.add(createNotesSection());
        
        return panel;
    }
    
    private JPanel createLocationSection() {
        JPanel section = createSectionPanel("📍 Borehole Location Details");
        
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setOpaque(false);
        
        JLabel lbl = new JLabel("Describe the intended borehole location (address, landmarks, GPS if available):");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        content.add(lbl, BorderLayout.NORTH);
        
        txtBoreholeLocation = new JTextArea(3, 40);
        txtBoreholeLocation.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtBoreholeLocation.setLineWrap(true);
        txtBoreholeLocation.setWrapStyleWord(true);
        txtBoreholeLocation.setText(currentClient.getBoreholeLocation() != null ? currentClient.getBoreholeLocation() : "");
        txtBoreholeLocation.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        content.add(new JScrollPane(txtBoreholeLocation), BorderLayout.CENTER);
        
        section.add(content);
        return section;
    }
    
    private JPanel createMandatoryFeesSection() {
        JPanel section = createSectionPanel("📋 Mandatory Fees (Based on Client Category: " + currentClient.getClientCategory() + ")");
        section.setBackground(new Color(254, 249, 195));
        
        int surveyFee = getSurveyFee();
        int localAuthFee = getLocalAuthorityFee();
        
        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 8));
        grid.setOpaque(false);
        
        grid.add(createBoldLabel("Survey Fees:"));
        lblSurveyFee = new JLabel(String.format("KSh %,d", surveyFee));
        lblSurveyFee.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSurveyFee.setForeground(PRIMARY_GREEN);
        grid.add(lblSurveyFee);
        
        grid.add(createBoldLabel("Local Authority Fees:"));
        lblLocalAuthFee = new JLabel(String.format("KSh %,d", localAuthFee));
        lblLocalAuthFee.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLocalAuthFee.setForeground(PRIMARY_GREEN);
        grid.add(lblLocalAuthFee);
        
        section.add(grid);
        
        JLabel note = new JLabel("* These fees are mandatory for all service requests");
        note.setFont(new Font("Segoe UI", Font.BOLD, 11));
        note.setForeground(new Color(146, 64, 14));
        section.add(Box.createVerticalStrut(5));
        section.add(note);
        
        return section;
    }
    
    private JPanel createDrillingSection() {
        JPanel section = createSectionPanel("⚒️ Drilling Service (Table 2)");
        
        chkDrilling = new JCheckBox("Include Drilling Service");
        chkDrilling.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkDrilling.setOpaque(false);
        chkDrilling.addActionListener(e -> calculateTotal());
        section.add(chkDrilling);
        section.add(Box.createVerticalStrut(10));
        
        JPanel optionsPanel = new JPanel(new GridLayout(2, 4, 15, 8));
        optionsPanel.setOpaque(false);
        
        optionsPanel.add(createBoldLabel("Drilling Type:"));
        cmbDrillingType = new JComboBox<>(new String[]{
            "Symmetric Drilling - KSh 130,000",
            "Core Drilling - KSh 225,000", 
            "Geo-Technical Drilling - KSh 335,000"
        });
        cmbDrillingType.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbDrillingType.addActionListener(e -> calculateTotal());
        optionsPanel.add(cmbDrillingType);
        
        optionsPanel.add(createBoldLabel("Drilling Cost:"));
        lblDrillingCost = new JLabel("KSh 0");
        lblDrillingCost.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDrillingCost.setForeground(PRIMARY_GREEN);
        optionsPanel.add(lblDrillingCost);
        
        optionsPanel.add(createBoldLabel("Borehole Depth (m):"));
        spnBoreholeDepth = new JSpinner(new SpinnerNumberModel(50, 10, 500, 10));
        spnBoreholeDepth.setFont(new Font("Segoe UI", Font.BOLD, 13));
        spnBoreholeDepth.addChangeListener(e -> calculateTotal());
        optionsPanel.add(spnBoreholeDepth);
        
        optionsPanel.add(createBoldLabel("Depth Cost (Table 4):"));
        lblDepthCost = new JLabel("KSh 0");
        lblDepthCost.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDepthCost.setForeground(PRIMARY_GREEN);
        optionsPanel.add(lblDepthCost);
        
        section.add(optionsPanel);
        
        // Depth pricing info
        JLabel depthInfo = new JLabel("<html><small>Depth Rates: 1-100m = KSh 1,000/m | 101-200m = KSh 1,500/m | 201-300m = KSh 2,000/m | 300+m = KSh 2,500/m</small></html>");
        depthInfo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        depthInfo.setForeground(TEXT_SECONDARY);
        section.add(Box.createVerticalStrut(8));
        section.add(depthInfo);
        
        return section;
    }
    
    private JPanel createPumpSection() {
        JPanel section = createSectionPanel("💧 Pump Installation (Table 3 & 4)");
        
        chkPump = new JCheckBox("Include Pump Installation");
        chkPump.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkPump.setOpaque(false);
        chkPump.addActionListener(e -> calculateTotal());
        section.add(chkPump);
        section.add(Box.createVerticalStrut(10));
        
        JPanel optionsPanel = new JPanel(new GridLayout(2, 4, 15, 8));
        optionsPanel.setOpaque(false);
        
        optionsPanel.add(createBoldLabel("Pump Type:"));
        cmbPumpType = new JComboBox<>(new String[]{
            "Submersible Electric Pump - KSh 90,000",
            "Solar Pump - KSh 65,000",
            "Hand Pump - KSh 30,000"
        });
        cmbPumpType.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbPumpType.addActionListener(e -> calculateTotal());
        optionsPanel.add(cmbPumpType);
        
        optionsPanel.add(createBoldLabel("Pump Cost:"));
        lblPumpCost = new JLabel("KSh 0");
        lblPumpCost.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPumpCost.setForeground(PRIMARY_GREEN);
        optionsPanel.add(lblPumpCost);
        
        optionsPanel.add(createBoldLabel("Tank Height (m):"));
        spnTankHeight = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        spnTankHeight.setFont(new Font("Segoe UI", Font.BOLD, 13));
        spnTankHeight.addChangeListener(e -> calculateTotal());
        optionsPanel.add(spnTankHeight);
        
        optionsPanel.add(createBoldLabel("Height Cost (Table 4):"));
        lblHeightCost = new JLabel("KSh 0");
        lblHeightCost.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeightCost.setForeground(PRIMARY_GREEN);
        optionsPanel.add(lblHeightCost);
        
        section.add(optionsPanel);
        
        JLabel heightInfo = new JLabel("<html><small>Height Rates: 1-100m = KSh 1,000/m | 101-200m = KSh 1,500/m | Uses same Table 4 rates</small></html>");
        heightInfo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        heightInfo.setForeground(TEXT_SECONDARY);
        section.add(Box.createVerticalStrut(8));
        section.add(heightInfo);
        
        return section;
    }
    
    private JPanel createTankSection() {
        JPanel section = createSectionPanel("🛢️ Water Tank Installation");
        
        chkTank = new JCheckBox("Include Tank Installation");
        chkTank.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkTank.setOpaque(false);
        chkTank.addActionListener(e -> calculateTotal());
        section.add(chkTank);
        section.add(Box.createVerticalStrut(10));
        
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        optionsPanel.setOpaque(false);
        
        optionsPanel.add(createBoldLabel("Tank Capacity (Litres):"));
        spnTankCapacity = new JSpinner(new SpinnerNumberModel(1000, 500, 50000, 500));
        spnTankCapacity.setFont(new Font("Segoe UI", Font.BOLD, 13));
        spnTankCapacity.addChangeListener(e -> calculateTotal());
        optionsPanel.add(spnTankCapacity);
        
        optionsPanel.add(Box.createHorizontalStrut(30));
        optionsPanel.add(createBoldLabel("Tank Cost:"));
        lblTankCost = new JLabel("KSh 0");
        lblTankCost.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTankCost.setForeground(PRIMARY_GREEN);
        optionsPanel.add(lblTankCost);
        
        section.add(optionsPanel);
        
        JLabel tankInfo = new JLabel("<html><small>Tank pricing: KSh 15 per litre capacity (e.g., 1,000L = KSh 15,000)</small></html>");
        tankInfo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tankInfo.setForeground(TEXT_SECONDARY);
        section.add(Box.createVerticalStrut(8));
        section.add(tankInfo);
        
        return section;
    }
    
    private JPanel createPlumbingSection() {
        JPanel section = createSectionPanel("🔧 Plumbing Services");
        
        chkPlumbing = new JCheckBox("Include Plumbing Services");
        chkPlumbing.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkPlumbing.setOpaque(false);
        chkPlumbing.addActionListener(e -> calculateTotal());
        section.add(chkPlumbing);
        section.add(Box.createVerticalStrut(10));
        
        JPanel optionsPanel = new JPanel(new GridLayout(2, 4, 15, 8));
        optionsPanel.setOpaque(false);
        
        optionsPanel.add(createBoldLabel("Pipe Type:"));
        cmbPipeType = new JComboBox<>(new String[]{
            "PVC Pipes - KSh 500/m",
            "HDPE Pipes - KSh 800/m",
            "Galvanized Pipes - KSh 1,200/m"
        });
        cmbPipeType.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbPipeType.addActionListener(e -> calculateTotal());
        optionsPanel.add(cmbPipeType);
        
        optionsPanel.add(createBoldLabel("Pipe Diameter (inches):"));
        spnPipeDiameter = new JSpinner(new SpinnerNumberModel(1, 0.5, 6, 0.5));
        spnPipeDiameter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        spnPipeDiameter.addChangeListener(e -> calculateTotal());
        optionsPanel.add(spnPipeDiameter);
        
        optionsPanel.add(createBoldLabel("Pipe Length (m):"));
        spnPipeLength = new JSpinner(new SpinnerNumberModel(20, 5, 500, 5));
        spnPipeLength.setFont(new Font("Segoe UI", Font.BOLD, 13));
        spnPipeLength.addChangeListener(e -> calculateTotal());
        optionsPanel.add(spnPipeLength);
        
        optionsPanel.add(createBoldLabel("Number of Outlets:"));
        spnOutlets = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));
        spnOutlets.setFont(new Font("Segoe UI", Font.BOLD, 13));
        spnOutlets.addChangeListener(e -> calculateTotal());
        optionsPanel.add(spnOutlets);
        
        section.add(optionsPanel);
        
        JPanel costRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        costRow.setOpaque(false);
        costRow.add(createBoldLabel("Total Plumbing Cost:"));
        lblPlumbingCost = new JLabel("KSh 0");
        lblPlumbingCost.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPlumbingCost.setForeground(PRIMARY_GREEN);
        costRow.add(lblPlumbingCost);
        section.add(Box.createVerticalStrut(8));
        section.add(costRow);
        
        JLabel plumbInfo = new JLabel("<html><small>Plumbing = (Pipe cost × length) + (Diameter factor × 500) + (Outlets × KSh 2,000 each)</small></html>");
        plumbInfo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        plumbInfo.setForeground(TEXT_SECONDARY);
        section.add(plumbInfo);
        
        return section;
    }
    
    private JPanel createMaintenanceSection() {
        JPanel section = createSectionPanel("🔩 Pump Maintenance Services");
        
        chkMaintenance = new JCheckBox("Include Annual Maintenance Package");
        chkMaintenance.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkMaintenance.setOpaque(false);
        chkMaintenance.addActionListener(e -> calculateTotal());
        section.add(chkMaintenance);
        
        JPanel costRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        costRow.setOpaque(false);
        costRow.add(createBoldLabel("Annual Maintenance Cost:"));
        lblMaintenanceCost = new JLabel("KSh 25,000");
        lblMaintenanceCost.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMaintenanceCost.setForeground(PRIMARY_GREEN);
        costRow.add(lblMaintenanceCost);
        section.add(costRow);
        
        JLabel maintInfo = new JLabel("<html><small>Includes quarterly inspections, minor repairs, and 24/7 emergency support</small></html>");
        maintInfo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        maintInfo.setForeground(TEXT_SECONDARY);
        section.add(maintInfo);
        
        return section;
    }
    
    private JPanel createNotesSection() {
        JPanel section = createSectionPanel("📝 Additional Notes & Requirements");
        
        txtNotes = new JTextArea(3, 40);
        txtNotes.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        section.add(new JScrollPane(txtNotes));
        
        return section;
    }
    
    private JPanel createSectionPanel(String title) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(LIGHT_BG);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(lblTitle);
        section.add(Box.createVerticalStrut(12));
        
        return section;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(LIGHT_BG);
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        
        JLabel lblTitle = new JLabel("💰 Cost Summary");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(20));
        
        // Client Info
        panel.add(createSummaryLabel("Client:", currentClient.getFullName()));
        panel.add(createSummaryLabel("Category:", currentClient.getClientCategory()));
        panel.add(createSummaryLabel("Client ID:", currentClient.getClientId()));
        panel.add(Box.createVerticalStrut(15));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));
        
        // Cost breakdown
        lblSubtotal = new JLabel("KSh 0");
        lblTax = new JLabel("KSh 0");
        lblGrandTotal = new JLabel("KSh 0");
        
        panel.add(createSummaryRow("Subtotal:", lblSubtotal));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createSummaryRow("Tax (16%):", lblTax));
        panel.add(Box.createVerticalStrut(12));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(12));
        
        // Grand Total
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setMaximumSize(new Dimension(280, 45));
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblTotalLabel = new JLabel("GRAND TOTAL:");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalLabel.setForeground(TEXT_PRIMARY);
        
        lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGrandTotal.setForeground(PRIMARY_GREEN);
        
        totalRow.add(lblTotalLabel, BorderLayout.WEST);
        totalRow.add(lblGrandTotal, BorderLayout.EAST);
        panel.add(totalRow);
        
        panel.add(Box.createVerticalStrut(25));
        
        // Important Notes
        JPanel notesPanel = new JPanel();
        notesPanel.setLayout(new BoxLayout(notesPanel, BoxLayout.Y_AXIS));
        notesPanel.setBackground(new Color(254, 243, 199));
        notesPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        notesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblNote1 = new JLabel("<html><b>📌 Important:</b></html>");
        lblNote1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        notesPanel.add(lblNote1);
        
        JLabel lblNote2 = new JLabel("<html><small>• 30% down payment required<br>• Request reviewed within 24hrs<br>• Site survey scheduled after approval</small></html>");
        lblNote2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNote2.setForeground(new Color(146, 64, 14));
        notesPanel.add(lblNote2);
        
        panel.add(notesPanel);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createSummaryLabel(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(280, 22));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLabel.setForeground(TEXT_SECONDARY);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblValue.setForeground(TEXT_PRIMARY);
        
        row.add(lblLabel, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.EAST);
        
        return row;
    }
    
    private JPanel createSummaryRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(280, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLabel.setForeground(TEXT_SECONDARY);
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(TEXT_PRIMARY);
        
        row.add(lblLabel, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        
        return row;
    }
    
    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        footer.setBackground(LIGHT_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        
        JButton btnSubmit = new JButton("✅ Submit Service Request");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSubmit.setPreferredSize(new Dimension(220, 50));
        btnSubmit.setBackground(PRIMARY_GREEN);
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setBorderPainted(false);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmit.addActionListener(e -> submitRequest());
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setPreferredSize(new Dimension(120, 50));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());
        
        footer.add(btnSubmit);
        footer.add(btnCancel);
        
        return footer;
    }
    
    private JLabel createBoldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }
    
    // ===== PRICING CALCULATION METHODS =====
    
    private int getSurveyFee() {
        String category = currentClient.getClientCategory();
        if ("INDUSTRIAL".equals(category)) return SURVEY_FEE_INDUSTRIAL;
        if ("COMMERCIAL".equals(category)) return SURVEY_FEE_COMMERCIAL;
        return SURVEY_FEE_DOMESTIC;
    }
    
    private int getLocalAuthorityFee() {
        String category = currentClient.getClientCategory();
        if ("INDUSTRIAL".equals(category)) return LOCAL_AUTH_FEE_INDUSTRIAL;
        if ("COMMERCIAL".equals(category)) return LOCAL_AUTH_FEE_COMMERCIAL;
        return LOCAL_AUTH_FEE_DOMESTIC;
    }
    
    private int getDrillingBaseCost() {
        String selected = (String) cmbDrillingType.getSelectedItem();
        if (selected.contains("Symmetric")) return DRILLING_SYMMETRIC;
        if (selected.contains("Core")) return DRILLING_CORE;
        return DRILLING_GEO_TECHNICAL;
    }
    
    private int getPumpBaseCost() {
        String selected = (String) cmbPumpType.getSelectedItem();
        if (selected.contains("Submersible")) return PUMP_SUBMERSIBLE_ELECTRIC;
        if (selected.contains("Solar")) return PUMP_SOLAR;
        return PUMP_HAND;
    }
    
    /**
     * Calculate depth/height cost using Table 4 rates
     * 1-100m = 1,000/m, 101-200m = 1,500/m, 201-300m = 2,000/m, 300+ = 2,500/m
     */
    private int calculateDepthHeightCost(int meters) {
        int cost = 0;
        if (meters <= 100) {
            cost = meters * 1000;
        } else if (meters <= 200) {
            cost = 100 * 1000 + (meters - 100) * 1500;
        } else if (meters <= 300) {
            cost = 100 * 1000 + 100 * 1500 + (meters - 200) * 2000;
        } else {
            cost = 100 * 1000 + 100 * 1500 + 100 * 2000 + (meters - 300) * 2500;
        }
        return cost;
    }
    
    private int getPipeCostPerMeter() {
        String selected = (String) cmbPipeType.getSelectedItem();
        if (selected.contains("PVC")) return 500;
        if (selected.contains("HDPE")) return 800;
        return 1200; // Galvanized
    }
    
    private void calculateTotal() {
        double total = 0;
        
        // Mandatory fees (always included)
        int surveyFee = getSurveyFee();
        int localAuthFee = getLocalAuthorityFee();
        total += surveyFee + localAuthFee;
        
        // Drilling costs
        if (chkDrilling.isSelected()) {
            int drillingBase = getDrillingBaseCost();
            int boreholeDepth = (int) spnBoreholeDepth.getValue();
            int depthCost = calculateDepthHeightCost(boreholeDepth);
            
            lblDrillingCost.setText(String.format("KSh %,d", drillingBase));
            lblDepthCost.setText(String.format("KSh %,d", depthCost));
            
            total += drillingBase + depthCost;
        } else {
            lblDrillingCost.setText("KSh 0");
            lblDepthCost.setText("KSh 0");
        }
        
        // Pump costs
        if (chkPump.isSelected()) {
            int pumpBase = getPumpBaseCost();
            int tankHeight = (int) spnTankHeight.getValue();
            int heightCost = calculateDepthHeightCost(tankHeight);
            
            lblPumpCost.setText(String.format("KSh %,d", pumpBase));
            lblHeightCost.setText(String.format("KSh %,d", heightCost));
            
            total += pumpBase + heightCost;
        } else {
            lblPumpCost.setText("KSh 0");
            lblHeightCost.setText("KSh 0");
        }
        
        // Tank costs (KSh 15 per litre)
        if (chkTank.isSelected()) {
            int capacity = (int) spnTankCapacity.getValue();
            int tankCost = capacity * 15;
            lblTankCost.setText(String.format("KSh %,d", tankCost));
            total += tankCost;
        } else {
            lblTankCost.setText("KSh 0");
        }
        
        // Plumbing costs
        if (chkPlumbing.isSelected()) {
            int pipeCost = getPipeCostPerMeter();
            int length = (int) spnPipeLength.getValue();
            double diameter = (double) spnPipeDiameter.getValue();
            int outlets = (int) spnOutlets.getValue();
            
            int plumbingCost = (int) ((pipeCost * length) + (diameter * 500) + (outlets * 2000));
            lblPlumbingCost.setText(String.format("KSh %,d", plumbingCost));
            total += plumbingCost;
        } else {
            lblPlumbingCost.setText("KSh 0");
        }
        
        // Maintenance costs
        if (chkMaintenance.isSelected()) {
            total += 25000;
            lblMaintenanceCost.setText("KSh 25,000");
        } else {
            lblMaintenanceCost.setText("KSh 0");
        }
        
        // Calculate tax (16%)
        double tax = total * TAX_RATE;
        double grandTotal = total + tax;
        
        // Update summary labels
        lblSubtotal.setText(String.format("KSh %,.0f", total));
        lblTax.setText(String.format("KSh %,.0f", tax));
        lblGrandTotal.setText(String.format("KSh %,.0f", grandTotal));
    }
    
    private void submitRequest() {
        // Validate location
        if (txtBoreholeLocation.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please provide the borehole location details.", 
                "Location Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Build services summary
        StringBuilder services = new StringBuilder();
        if (chkDrilling.isSelected()) {
            services.append("• ").append(cmbDrillingType.getSelectedItem()).append("\n");
            services.append("  Depth: ").append(spnBoreholeDepth.getValue()).append("m\n");
        }
        if (chkPump.isSelected()) {
            services.append("• ").append(cmbPumpType.getSelectedItem()).append("\n");
            services.append("  Tank Height: ").append(spnTankHeight.getValue()).append("m\n");
        }
        if (chkTank.isSelected()) {
            services.append("• Tank Installation: ").append(spnTankCapacity.getValue()).append("L\n");
        }
        if (chkPlumbing.isSelected()) {
            services.append("• Plumbing: ").append(cmbPipeType.getSelectedItem()).append("\n");
            services.append("  Length: ").append(spnPipeLength.getValue()).append("m, Outlets: ").append(spnOutlets.getValue()).append("\n");
        }
        if (chkMaintenance.isSelected()) {
            services.append("• Annual Maintenance Package\n");
        }
        
        if (services.length() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select at least one service.", 
                "No Services Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Calculate costs for database
        double surveyFee = getSurveyFee();
        double authorityFee = getLocalAuthorityFee();
        double drillingCost = 0;
        double pumpCost = 0;
        double tankCost = 0;
        double plumbingCost = 0;
        
        if (chkDrilling.isSelected()) {
            int drillingBase = getDrillingBaseCost();
            int depth = (int) spnBoreholeDepth.getValue();
            drillingCost = drillingBase + calculateDepthHeightCost(depth);
        }
        
        if (chkPump.isSelected()) {
            int pumpBase = getPumpBaseCost();
            int height = (int) spnTankHeight.getValue();
            pumpCost = pumpBase + calculateDepthHeightCost(height);
        }
        
        if (chkTank.isSelected()) {
            int capacity = (int) spnTankCapacity.getValue();
            tankCost = capacity * 15;
        }
        
        if (chkPlumbing.isSelected()) {
            int pipeCostPerMeter = getPipeCostPerMeter();
            int length = (int) spnPipeLength.getValue();
            double diameter = (double) spnPipeDiameter.getValue();
            int outlets = (int) spnOutlets.getValue();
            plumbingCost = (pipeCostPerMeter * length) + (diameter * 500) + (outlets * 2000);
        }
        
        double subtotal = surveyFee + authorityFee + drillingCost + pumpCost + tankCost + plumbingCost;
        double tax = subtotal * TAX_RATE;
        double grandTotal = subtotal + tax;
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><div style='width: 400px; padding: 10px;'>" +
            "<h2 style='color: #1E3A8A;'>\ud83d\udccb Confirm Service Request</h2>" +
            "<p><b>Selected Services:</b></p>" +
            "<div style='background: #F3F4F6; padding: 10px; border-radius: 5px;'>" + 
            services.toString().replace("\n", "<br>") + 
            "</div>" +
            "<hr>" +
            "<p><b>Total Cost:</b> <span style='color: #22C55E; font-size: 18px;'>" + lblGrandTotal.getText() + "</span></p>" +
            "<p style='color: #92400E; background: #FEF3C7; padding: 8px;'>\u26a0\ufe0f A 30% down payment will be required upon approval.</p>" +
            "</div></html>",
            "Confirm Request", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Create and save transaction
                TransactionDAO dao = new TransactionDAO();
                Transaction transaction = new Transaction();
                
                // Generate transaction number
                transaction.setTransactionNo(dao.generateTransactionNo());
                transaction.setClientId(currentClient.getClientId());
                transaction.setTransactionDate(new Date());
                
                // Set drilling details
                if (chkDrilling.isSelected()) {
                    transaction.setBoreholeDepth((int) spnBoreholeDepth.getValue());
                    transaction.setDrillingCost(drillingCost);
                }
                
                // Set pump details
                if (chkPump.isSelected()) {
                    transaction.setTankHeight((int) spnTankHeight.getValue());
                    transaction.setPumpCost(pumpCost);
                }
                
                // Set tank details
                if (chkTank.isSelected()) {
                    transaction.setTankCost(tankCost);
                }
                
                // Set plumbing
                transaction.setHasPlumbing(chkPlumbing.isSelected());
                transaction.setPlumbingCost(plumbingCost);
                
                // Set fees and totals
                transaction.setSurveyFee(surveyFee);
                transaction.setAuthorityFee(authorityFee);
                transaction.setSubtotal(subtotal);
                transaction.setTaxAmount(tax);
                transaction.setGrandTotal(grandTotal);
                
                // Set payment status
                transaction.setPaymentStatus("NOT_PAID");
                transaction.setAmountPaid(0);
                transaction.setBalance(grandTotal);
                
                // Set job status to PENDING_SURVEY (awaiting staff approval)
                transaction.setJobStatus("PENDING_SURVEY");
                
                // Set notes
                String notes = "Location: " + txtBoreholeLocation.getText().trim();
                if (!txtNotes.getText().trim().isEmpty()) {
                    notes += "\n\nNotes: " + txtNotes.getText().trim();
                }
                transaction.setNotes(notes);
                
                // Save to database
                if (dao.saveTransaction(transaction)) {
                    JOptionPane.showMessageDialog(this,
                        "<html><div style='padding: 20px; text-align: center;'>" +
                        "<h2 style='color: #22C55E;'>✅ Request Submitted Successfully!</h2>" +
                        "<p><b>Transaction No:</b> " + transaction.getTransactionNo() + "</p>" +
                        "<p>Your service request has been received.</p>" +
                        "<p style='color: #666;'>Our team will review and contact you within 24 hours.</p>" +
                        "<p><b>Date:</b> " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "</p>" +
                        "</div></html>",
                        "Success", JOptionPane.PLAIN_MESSAGE);
                    dispose();
                } else {
                    UIHelper.showError(this, "Failed to save service request. Please try again.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                UIHelper.showError(this, "Error submitting request: " + e.getMessage());
            }
        }
    }
}
