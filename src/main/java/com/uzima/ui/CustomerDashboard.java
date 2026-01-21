package com.uzima.ui;

import com.uzima.dao.TransactionDAO;
import com.uzima.model.Client;
import com.uzima.model.Transaction;
import com.uzima.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * Professional Customer Dashboard for Registered Clients
 */
public class CustomerDashboard extends JFrame {
    private Client currentClient;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotalSpent, lblPendingBalance, lblTransactionCount, lblLastPayment;
    private JLabel lblDateTime;
    private Timer clockTimer;
    
    // Colors
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color PRIMARY_GREEN = new Color(34, 197, 94);
    private static final Color PRIMARY_ORANGE = new Color(249, 115, 22);
    private static final Color PRIMARY_PURPLE = new Color(139, 92, 246);
    private static final Color PRIMARY_RED = new Color(239, 68, 68);
    private static final Color BACKGROUND = new Color(241, 245, 249);
    private static final Color SIDEBAR_BG = new Color(15, 23, 42);
    private static final Color CARD_BG = Color.WHITE;
    
    public CustomerDashboard(Client client) {
        this.currentClient = client;
        initComponents();
        loadCustomerData();
        startClock();
    }
    
    private void initComponents() {
        setTitle("My Account - UZIMA Borehole Services");
        setSize(1400, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        
        // Main Container
        JPanel mainContainer = new JPanel(new BorderLayout(0, 0));
        mainContainer.setBackground(BACKGROUND);
        
        // Sidebar
        JPanel sidebar = createSidebar();
        mainContainer.add(sidebar, BorderLayout.WEST);
        
        // Content Area
        JPanel contentArea = new JPanel(new BorderLayout(0, 0));
        contentArea.setBackground(BACKGROUND);
        
        // Top Header
        JPanel topHeader = createTopHeader();
        contentArea.add(topHeader, BorderLayout.NORTH);
        
        // Main Content
        JPanel mainContent = createMainContent();
        contentArea.add(mainContent, BorderLayout.CENTER);
        
        mainContainer.add(contentArea, BorderLayout.CENTER);
        add(mainContainer);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        
        // Logo Section
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(new Color(30, 41, 59));
        logoPanel.setMaximumSize(new Dimension(280, 100));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        
        JLabel lblLogo = new JLabel("💧 UZIMA");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblPortal = new JLabel("Customer Portal");
        lblPortal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPortal.setForeground(new Color(148, 163, 184));
        lblPortal.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        logoPanel.add(lblLogo);
        logoPanel.add(Box.createVerticalStrut(5));
        logoPanel.add(lblPortal);
        sidebar.add(logoPanel);
        
        // User Profile Card
        JPanel profileCard = createProfileCard();
        sidebar.add(profileCard);
        
        sidebar.add(Box.createVerticalStrut(20));
        
        // Menu Items
        sidebar.add(createMenuItem("🏠", "Dashboard", true, null));
        sidebar.add(createMenuItem("📋", "My Services", false, e -> showMyServices()));
        sidebar.add(createMenuItem("🛒", "Request New Service", false, e -> openServiceRequest()));
        sidebar.add(createMenuItem("💳", "Payment History", false, e -> showPaymentHistory()));
        sidebar.add(createMenuItem("📄", "Invoices", false, e -> showInvoices()));
        sidebar.add(createMenuItem("📞", "Contact Support", false, e -> showSupport()));
        sidebar.add(createMenuItem("👤", "My Profile", false, e -> showProfile()));
        
        sidebar.add(Box.createVerticalGlue());
        
        // Logout Button
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoutPanel.setBackground(SIDEBAR_BG);
        logoutPanel.setMaximumSize(new Dimension(280, 70));
        
        JButton btnLogout = new JButton("🚪 Sign Out");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(PRIMARY_RED);
        btnLogout.setPreferredSize(new Dimension(220, 45));
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> logout());
        
        logoutPanel.add(btnLogout);
        sidebar.add(logoutPanel);
        sidebar.add(Box.createVerticalStrut(20));
        
        return sidebar;
    }
    
    private JPanel createProfileCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(30, 41, 59));
        card.setMaximumSize(new Dimension(280, 150));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Avatar
        JLabel avatar = new JLabel("👤");
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Name
        JLabel lblName = new JLabel(currentClient.getFullName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblName.setForeground(Color.WHITE);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Client ID
        JLabel lblId = new JLabel(currentClient.getClientId());
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblId.setForeground(PRIMARY_BLUE);
        lblId.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Category Badge
        JLabel lblCategory = new JLabel("● " + currentClient.getClientCategory());
        lblCategory.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblCategory.setForeground(PRIMARY_GREEN);
        lblCategory.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(avatar);
        card.add(Box.createVerticalStrut(10));
        card.add(lblName);
        card.add(Box.createVerticalStrut(3));
        card.add(lblId);
        card.add(Box.createVerticalStrut(5));
        card.add(lblCategory);
        
        return card;
    }
    
    private JPanel createMenuItem(String icon, String text, boolean isActive, ActionListener action) {
        JPanel menuItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 12));
        menuItem.setBackground(isActive ? new Color(51, 65, 85) : SIDEBAR_BG);
        menuItem.setMaximumSize(new Dimension(280, 50));
        menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (isActive) {
            menuItem.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, PRIMARY_GREEN));
        }
        
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        
        JLabel lblText = new JLabel(text);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblText.setForeground(isActive ? Color.WHITE : new Color(180, 190, 200));
        
        menuItem.add(lblIcon);
        menuItem.add(lblText);
        
        if (action != null) {
            menuItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    action.actionPerformed(null);
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isActive) menuItem.setBackground(new Color(51, 65, 85));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isActive) menuItem.setBackground(SIDEBAR_BG);
                }
            });
        }
        
        return menuItem;
    }
    
    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(CARD_BG);
        header.setPreferredSize(new Dimension(getWidth(), 80));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(15, 30, 15, 30)));
        
        // Left - Welcome Message
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        leftPanel.setBackground(CARD_BG);
        
        JLabel lblTitle = new JLabel("Welcome back, " + currentClient.getFullName().split(" ")[0] + "! 👋");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));
        
        lblDateTime = new JLabel();
        lblDateTime.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDateTime.setForeground(new Color(100, 116, 139));
        
        leftPanel.add(lblTitle);
        leftPanel.add(lblDateTime);
        
        // Right - Quick Actions
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightPanel.setBackground(CARD_BG);
        
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setPreferredSize(new Dimension(110, 40));
        btnRefresh.setBackground(new Color(241, 245, 249));
        btnRefresh.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadCustomerData());
        
        JButton btnSupport = new JButton("📞 Support");
        btnSupport.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSupport.setPreferredSize(new Dimension(110, 40));
        btnSupport.setBackground(PRIMARY_GREEN);
        btnSupport.setForeground(Color.WHITE);
        btnSupport.setBorderPainted(false);
        btnSupport.setFocusPainted(false);
        btnSupport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSupport.addActionListener(e -> showSupport());
        
        rightPanel.add(btnRefresh);
        rightPanel.add(btnSupport);
        
        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        // Top - Stats Cards
        JPanel statsPanel = createStatsPanel();
        content.add(statsPanel, BorderLayout.NORTH);
        
        // Center - Main Content Grid
        JPanel centerContent = new JPanel(new BorderLayout(20, 20));
        centerContent.setOpaque(false);
        
        // Left Column - Transactions Table
        JPanel transactionsPanel = createTransactionsPanel();
        
        // Right Column - Quick Info Cards
        JPanel infoPanel = createInfoPanel();
        
        centerContent.add(transactionsPanel, BorderLayout.CENTER);
        centerContent.add(infoPanel, BorderLayout.EAST);
        
        content.add(centerContent, BorderLayout.CENTER);
        
        return content;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(getWidth(), 120));
        
        lblTransactionCount = new JLabel("0");
        lblTotalSpent = new JLabel("KSh 0");
        lblPendingBalance = new JLabel("KSh 0");
        lblLastPayment = new JLabel("-");
        
        panel.add(createStatCard("📋", "Total Services", lblTransactionCount, PRIMARY_BLUE, "All time"));
        panel.add(createStatCard("💰", "Total Invested", lblTotalSpent, PRIMARY_GREEN, "Payments made"));
        panel.add(createStatCard("⏳", "Outstanding", lblPendingBalance, PRIMARY_ORANGE, "Balance due"));
        panel.add(createStatCard("📅", "Last Payment", lblLastPayment, PRIMARY_PURPLE, "Recent"));
        
        return panel;
    }
    
    private JPanel createStatCard(String icon, String title, JLabel valueLabel, Color color, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(15, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Accent line at top
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth(), 4, 16, 16);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        
        // Left - Icon
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(50, 50));
        iconPanel.setLayout(new GridBagLayout());
        
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconPanel.add(lblIcon);
        
        // Right - Content
        JPanel contentPanel = new JPanel(new GridLayout(3, 1, 0, 2));
        contentPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(71, 85, 105));
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(new Color(30, 41, 59));
        
        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSubtitle.setForeground(color);
        
        contentPanel.add(lblTitle);
        contentPanel.add(valueLabel);
        contentPanel.add(lblSubtitle);
        
        card.add(iconPanel, BorderLayout.WEST);
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("📋 My Service History");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        
        JButton btnExport = new JButton("📥 Export");
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExport.setForeground(PRIMARY_BLUE);
        btnExport.setBackground(Color.WHITE);
        btnExport.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE));
        btnExport.setPreferredSize(new Dimension(100, 35));
        btnExport.setFocusPainted(false);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnExport, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"Service #", "Date", "Services", "Total", "Paid", "Balance", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(50);
        transactionTable.setFont(new Font("Segoe UI", Font.BOLD, 13));
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        transactionTable.getTableHeader().setBackground(new Color(248, 250, 252));
        transactionTable.getTableHeader().setForeground(new Color(71, 85, 105));
        transactionTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        transactionTable.setShowGrid(false);
        transactionTable.setIntercellSpacing(new Dimension(0, 5));
        transactionTable.setSelectionBackground(new Color(239, 246, 255));
        
        // Status column renderer
        transactionTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setHorizontalAlignment(CENTER);
                label.setOpaque(true);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                
                String status = value != null ? value.toString() : "";
                if (status.equals("PAID")) {
                    label.setBackground(new Color(220, 252, 231));
                    label.setForeground(PRIMARY_GREEN);
                } else if (status.equals("PARTIAL")) {
                    label.setBackground(new Color(254, 243, 199));
                    label.setForeground(PRIMARY_ORANGE);
                } else {
                    label.setBackground(new Color(254, 226, 226));
                    label.setForeground(PRIMARY_RED);
                }
                
                return label;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Footer with View Details button
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        footerPanel.setOpaque(false);
        
        JButton btnDetails = new JButton("View Details");
        btnDetails.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDetails.setForeground(Color.WHITE);
        btnDetails.setBackground(PRIMARY_BLUE);
        btnDetails.setPreferredSize(new Dimension(130, 40));
        btnDetails.setBorderPainted(false);
        btnDetails.setFocusPainted(false);
        btnDetails.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDetails.addActionListener(e -> viewTransactionDetails());
        
        footerPanel.add(btnDetails);
        panel.add(footerPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(320, 0));
        
        // Account Summary Card
        panel.add(createAccountSummaryCard());
        panel.add(Box.createVerticalStrut(20));
        
        // Quick Actions Card
        panel.add(createQuickActionsCard());
        panel.add(Box.createVerticalStrut(20));
        
        // Contact Card
        panel.add(createContactCard());
        
        return panel;
    }
    
    private JPanel createAccountSummaryCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setMaximumSize(new Dimension(320, 200));
        
        JLabel lblTitle = new JLabel("📊 Account Summary");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(30, 41, 59));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(15));
        
        card.add(createInfoRow("Client ID", currentClient.getClientId()));
        card.add(createInfoRow("Category", currentClient.getClientCategory()));
        card.add(createInfoRow("Phone", currentClient.getPhoneNumber()));
        card.add(createInfoRow("Location", currentClient.getCounty() != null ? currentClient.getCounty() : "N/A"));
        
        return card;
    }
    
    private JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(280, 30));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLabel.setForeground(new Color(71, 85, 105));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblValue.setForeground(new Color(30, 41, 59));
        
        row.add(lblLabel, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.EAST);
        
        return row;
    }
    
    private JPanel createQuickActionsCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setMaximumSize(new Dimension(320, 180));
        
        JLabel lblTitle = new JLabel("⚡ Quick Actions");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(30, 41, 59));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(15));
        
        JButton btnPayment = createActionButton("💳 Make Payment", PRIMARY_GREEN);
        JButton btnRequest = createActionButton("📝 Request Service", PRIMARY_BLUE);
        JButton btnDownload = createActionButton("📄 Download Invoice", PRIMARY_PURPLE);
        
        card.add(btnPayment);
        card.add(Box.createVerticalStrut(10));
        card.add(btnRequest);
        card.add(Box.createVerticalStrut(10));
        card.add(btnDownload);
        
        return card;
    }
    
    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setMaximumSize(new Dimension(280, 38));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
            }
        });
        
        return btn;
    }
    
    private JPanel createContactCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_BLUE, getWidth(), getHeight(), PRIMARY_PURPLE);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setMaximumSize(new Dimension(320, 150));
        
        JLabel lblIcon = new JLabel("📞");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        lblIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblTitle = new JLabel("Need Help?");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblPhone = new JLabel("+254 700 000 000");
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPhone.setForeground(new Color(200, 220, 255));
        lblPhone.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblEmail = new JLabel("support@uzimaborehole.co.ke");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEmail.setForeground(new Color(200, 220, 255));
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(lblIcon);
        card.add(Box.createVerticalStrut(10));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(5));
        card.add(lblPhone);
        card.add(lblEmail);
        
        return card;
    }
    
    private void loadCustomerData() {
        try {
            List<Transaction> transactions = TransactionDAO.getTransactionsByClientId(currentClient.getClientId());
            
            tableModel.setRowCount(0);
            
            double totalAmount = 0;
            double totalPaid = 0;
            String lastPaymentDate = "-";
            
            for (Transaction t : transactions) {
                StringBuilder services = new StringBuilder();
                if (t.getDrillingCost() > 0) services.append("Drilling, ");
                if (t.getPumpCost() > 0) services.append("Pump, ");
                if (t.getTankCost() > 0) services.append("Tank, ");
                if (t.getPlumbingCost() > 0) services.append("Plumbing, ");
                String serviceList = services.length() > 0 ? services.substring(0, services.length() - 2) : "N/A";
                
                tableModel.addRow(new Object[]{
                    t.getTransactionNo(),
                    UIHelper.formatDate(t.getTransactionDate()),
                    serviceList,
                    String.format("KSh %,.0f", t.getGrandTotal()),
                    String.format("KSh %,.0f", t.getAmountPaid()),
                    String.format("KSh %,.0f", t.getBalance()),
                    t.getPaymentStatus()
                });
                
                totalAmount += t.getGrandTotal();
                totalPaid += t.getAmountPaid();
                
                if (t.getAmountPaid() > 0) {
                    lastPaymentDate = UIHelper.formatDate(t.getTransactionDate());
                }
            }
            
            lblTransactionCount.setText(String.valueOf(transactions.size()));
            lblTotalSpent.setText(String.format("KSh %,.0f", totalPaid));
            lblPendingBalance.setText(String.format("KSh %,.0f", totalAmount - totalPaid));
            lblLastPayment.setText(lastPaymentDate);
            
        } catch (Exception e) {
            e.printStackTrace();
            UIHelper.showError(this, "Error loading data: " + e.getMessage());
        }
    }
    
    private void viewTransactionDetails() {
        int row = transactionTable.getSelectedRow();
        if (row < 0) {
            UIHelper.showWarning(this, "Please select a transaction to view details.");
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("<html><div style='padding: 20px; font-family: Segoe UI;'>");
        details.append("<h2 style='color: #1E40AF;'>Transaction Details</h2>");
        details.append("<hr>");
        details.append("<table style='font-size: 14px;'>");
        details.append("<tr><td><b>Service #:</b></td><td>").append(tableModel.getValueAt(row, 0)).append("</td></tr>");
        details.append("<tr><td><b>Date:</b></td><td>").append(tableModel.getValueAt(row, 1)).append("</td></tr>");
        details.append("<tr><td><b>Services:</b></td><td>").append(tableModel.getValueAt(row, 2)).append("</td></tr>");
        details.append("<tr><td><b>Total:</b></td><td>").append(tableModel.getValueAt(row, 3)).append("</td></tr>");
        details.append("<tr><td><b>Paid:</b></td><td style='color: green;'>").append(tableModel.getValueAt(row, 4)).append("</td></tr>");
        details.append("<tr><td><b>Balance:</b></td><td style='color: orange;'>").append(tableModel.getValueAt(row, 5)).append("</td></tr>");
        details.append("<tr><td><b>Status:</b></td><td>").append(tableModel.getValueAt(row, 6)).append("</td></tr>");
        details.append("</table>");
        details.append("<hr>");
        details.append("<p style='color: #666;'>For payment inquiries: +254 700 000 000</p>");
        details.append("</div></html>");
        
        JOptionPane.showMessageDialog(this, details.toString(), "Service Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showSupport() {
        JOptionPane.showMessageDialog(this,
            "<html><div style='padding: 15px; font-family: Segoe UI;'>" +
            "<h2>📞 Contact Support</h2>" +
            "<p><b>Phone:</b> +254 700 000 000</p>" +
            "<p><b>Email:</b> support@uzimaborehole.co.ke</p>" +
            "<p><b>Hours:</b> Mon-Fri, 8am - 6pm</p>" +
            "<hr>" +
            "<p style='color: #666;'>Our team is here to help!</p>" +
            "</div></html>",
            "Contact Support", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showProfile() {
        JOptionPane.showMessageDialog(this,
            "<html><div style='padding: 15px; font-family: Segoe UI;'>" +
            "<h2>👤 My Profile</h2>" +
            "<p><b>Name:</b> " + currentClient.getFullName() + "</p>" +
            "<p><b>ID:</b> " + currentClient.getClientId() + "</p>" +
            "<p><b>Category:</b> " + currentClient.getClientCategory() + "</p>" +
            "<p><b>Phone:</b> " + currentClient.getPhoneNumber() + "</p>" +
            "<p><b>Email:</b> " + (currentClient.getEmail() != null ? currentClient.getEmail() : "N/A") + "</p>" +
            "<p><b>Address:</b> " + currentClient.getPhysicalAddress() + "</p>" +
            "</div></html>",
            "My Profile", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showMyServices() {
        // Show a dialog with all services for this customer
        JDialog servicesDialog = new JDialog(this, "My Services", true);
        servicesDialog.setSize(900, 600);
        servicesDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel title = new JLabel("📋 My Borehole Services");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 41, 59));
        panel.add(title, BorderLayout.NORTH);
        
        String[] cols = {"Service #", "Date", "Type", "Depth/Size", "Cost", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        
        try {
            List<Transaction> transactions = TransactionDAO.getTransactionsByClientId(currentClient.getClientId());
            for (Transaction t : transactions) {
                String type = "";
                String details = "";
                if (t.getDrillingCost() > 0) { type = "Drilling"; details = t.getBoreholeDepth() + "m"; }
                else if (t.getPumpCost() > 0) { type = "Pump"; details = "-"; }
                else if (t.getTankCost() > 0) { type = "Tank"; details = "-"; }
                
                model.addRow(new Object[]{
                    t.getTransactionNo(),
                    UIHelper.formatDate(t.getTransactionDate()),
                    type.isEmpty() ? "Multiple" : type,
                    details,
                    String.format("KSh %,.0f", t.getGrandTotal()),
                    t.getJobStatus()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY_BLUE);
        table.getTableHeader().setForeground(Color.WHITE);
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setPreferredSize(new Dimension(120, 40));
        btnClose.addActionListener(e -> servicesDialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(btnClose);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        servicesDialog.setContentPane(panel);
        servicesDialog.setVisible(true);
    }
    
    private void openServiceRequest() {
        // Open the customer service request form
        CustomerServiceRequestForm requestForm = new CustomerServiceRequestForm(this, currentClient);
        requestForm.setVisible(true);
        loadCustomerData(); // Refresh after closing
    }
    
    private void showPaymentHistory() {
        JDialog paymentDialog = new JDialog(this, "Payment History", true);
        paymentDialog.setSize(800, 500);
        paymentDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel title = new JLabel("💳 Payment History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 41, 59));
        panel.add(title, BorderLayout.NORTH);
        
        String[] cols = {"Date", "Service #", "Amount Paid", "Method", "Balance After"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        
        try {
            List<Transaction> transactions = TransactionDAO.getTransactionsByClientId(currentClient.getClientId());
            for (Transaction t : transactions) {
                if (t.getAmountPaid() > 0) {
                    model.addRow(new Object[]{
                        UIHelper.formatDate(t.getTransactionDate()),
                        t.getTransactionNo(),
                        String.format("KSh %,.0f", t.getAmountPaid()),
                        "Cash/Bank",
                        String.format("KSh %,.0f", t.getBalance())
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setPreferredSize(new Dimension(120, 40));
        btnClose.addActionListener(e -> paymentDialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(btnClose);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        paymentDialog.setContentPane(panel);
        paymentDialog.setVisible(true);
    }
    
    private void showInvoices() {
        JDialog invoiceDialog = new JDialog(this, "My Invoices", true);
        invoiceDialog.setSize(900, 550);
        invoiceDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel title = new JLabel("📄 Invoices & Quotations");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 41, 59));
        panel.add(title, BorderLayout.NORTH);
        
        String[] cols = {"Invoice #", "Date", "Services", "Total", "Status", "Action"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 5; }
        };
        
        try {
            List<Transaction> transactions = TransactionDAO.getTransactionsByClientId(currentClient.getClientId());
            for (Transaction t : transactions) {
                StringBuilder services = new StringBuilder();
                if (t.getDrillingCost() > 0) services.append("Drilling, ");
                if (t.getPumpCost() > 0) services.append("Pump, ");
                if (t.getTankCost() > 0) services.append("Tank, ");
                String svcList = services.length() > 0 ? services.substring(0, services.length() - 2) : "N/A";
                
                model.addRow(new Object[]{
                    "INV-" + t.getTransactionNo(),
                    UIHelper.formatDate(t.getTransactionDate()),
                    svcList,
                    String.format("KSh %,.0f", t.getGrandTotal()),
                    t.getPaymentStatus(),
                    "Download"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY_PURPLE);
        table.getTableHeader().setForeground(Color.WHITE);
        
        // Download button renderer
        table.getColumnModel().getColumn(5).setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
            JButton btn = new JButton("📥 Download");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setBackground(PRIMARY_BLUE);
            btn.setForeground(Color.WHITE);
            return btn;
        });
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (col == 5 && row >= 0) {
                    String invoiceNo = (String) table.getValueAt(row, 0);
                    JOptionPane.showMessageDialog(invoiceDialog,
                        "Invoice " + invoiceNo + " download started.\nCheck your Downloads folder.",
                        "Download Started", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setPreferredSize(new Dimension(120, 40));
        btnClose.addActionListener(e -> invoiceDialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(btnClose);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        invoiceDialog.setContentPane(panel);
        invoiceDialog.setVisible(true);
    }
    
    private void startClock() {
        clockTimer = new Timer();
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy | hh:mm a");
                    lblDateTime.setText(sdf.format(new Date()));
                });
            }
        }, 0, 1000);
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to sign out?", 
            "Confirm Sign Out", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (clockTimer != null) clockTimer.cancel();
            dispose();
            new LoginForm().setVisible(true);
        }
    }
    
    @Override
    public void dispose() {
        if (clockTimer != null) clockTimer.cancel();
        super.dispose();
    }
}
