package com.uzima.ui;

import com.uzima.dao.ClientDAO;
import com.uzima.dao.TransactionDAO;
import com.uzima.model.Transaction;
import com.uzima.util.SessionManager;
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
 * Professional Admin Dashboard for the Uzima Borehole System
 */
public class DashboardForm extends JFrame {
    private JLabel lblWelcome, lblDateTime, lblRole;
    private JPanel statsPanel, quickActionsPanel, recentActivityPanel;
    private JTable recentTransactionsTable;
    private DefaultTableModel tableModel;
    private Timer clockTimer;
    
    // Stats Labels
    private JLabel lblTotalClients, lblActiveProjects, lblMonthlyRevenue, lblPendingPayments;
    private JLabel lblTodayTransactions, lblWeeklyGrowth;
    
    // Colors
    private static final Color SIDEBAR_COLOR = new Color(30, 41, 59);
    private static final Color SIDEBAR_HOVER = new Color(51, 65, 85);
    private static final Color HEADER_BG = new Color(248, 250, 252);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color ACCENT_GREEN = new Color(34, 197, 94);
    private static final Color ACCENT_ORANGE = new Color(249, 115, 22);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color ACCENT_PURPLE = new Color(139, 92, 246);
    private static final Color ACCENT_CYAN = new Color(6, 182, 212);
    
    public DashboardForm() {
        initComponents();
        loadDashboardData();
        startClock();
    }
    
    private void initComponents() {
        setTitle("Uzima Borehole System - Admin Dashboard");
        setSize(1400, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Main Container
        JPanel mainContainer = new JPanel(new BorderLayout(0, 0));
        mainContainer.setBackground(new Color(241, 245, 249));
        
        // Sidebar
        JPanel sidebar = createSidebar();
        mainContainer.add(sidebar, BorderLayout.WEST);
        
        // Content Area
        JPanel contentArea = new JPanel(new BorderLayout(0, 0));
        contentArea.setBackground(new Color(241, 245, 249));
        
        // Top Header
        JPanel topHeader = createTopHeader();
        contentArea.add(topHeader, BorderLayout.NORTH);
        
        // Main Content
        JPanel mainContent = createMainContent();
        contentArea.add(mainContent, BorderLayout.CENTER);
        
        mainContainer.add(contentArea, BorderLayout.CENTER);
        add(mainContainer);
        
        UIHelper.centerFrame(this);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        
        // Logo Section
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        logoPanel.setBackground(new Color(15, 23, 42));
        logoPanel.setMaximumSize(new Dimension(260, 80));
        
        JLabel lblLogo = new JLabel("💧");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        
        JLabel lblBrand = new JLabel("UZIMA");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblBrand.setForeground(Color.WHITE);
        
        logoPanel.add(lblLogo);
        logoPanel.add(lblBrand);
        sidebar.add(logoPanel);
        
        sidebar.add(Box.createVerticalStrut(20));
        
        // Menu Items
        sidebar.add(createMenuItem("🏠", "Dashboard", true, null));
        sidebar.add(createMenuItem("👥", "Clients", false, e -> openClientManagement()));
        sidebar.add(createMenuItem("�", "Service Requests", false, e -> openServiceRequests()));
        sidebar.add(createMenuItem("📋", "Transactions", false, e -> openViewTransactions()));
        sidebar.add(createMenuItem("💳", "Payments", false, e -> openPayments()));
        sidebar.add(createMenuItem("🔧", "Services", false, e -> openServicesManagement()));
        sidebar.add(createMenuItem("📊", "Reports", false, e -> openReports()));
        
        sidebar.add(Box.createVerticalStrut(20));
        
        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(71, 85, 105));
        sep.setMaximumSize(new Dimension(220, 1));
        sidebar.add(sep);
        
        sidebar.add(Box.createVerticalStrut(20));
        
        // Admin Section
        sidebar.add(createMenuItem("👤", "User Management", false, e -> openUserManagement()));
        sidebar.add(createMenuItem("⚙️", "Settings", false, e -> openSettings()));
        sidebar.add(createMenuItem("ℹ️", "About", false, e -> showAbout()));
        
        sidebar.add(Box.createVerticalGlue());
        
        // Logout Button at bottom
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoutPanel.setBackground(SIDEBAR_COLOR);
        logoutPanel.setMaximumSize(new Dimension(260, 60));
        
        JButton btnLogout = new JButton("🚪 Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(ACCENT_RED);
        btnLogout.setPreferredSize(new Dimension(200, 45));
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> handleLogout());
        
        logoutPanel.add(btnLogout);
        sidebar.add(logoutPanel);
        sidebar.add(Box.createVerticalStrut(20));
        
        return sidebar;
    }
    
    private JPanel createMenuItem(String icon, String text, boolean isActive, ActionListener action) {
        JPanel menuItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        menuItem.setBackground(isActive ? SIDEBAR_HOVER : SIDEBAR_COLOR);
        menuItem.setMaximumSize(new Dimension(260, 50));
        menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (isActive) {
            menuItem.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, ACCENT_BLUE));
        }
        
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        
        JLabel lblText = new JLabel(text);
        lblText.setFont(new Font("Segoe UI", isActive ? Font.BOLD : Font.PLAIN, 14));
        lblText.setForeground(isActive ? Color.WHITE : new Color(148, 163, 184));
        
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
                    if (!isActive) menuItem.setBackground(SIDEBAR_HOVER);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isActive) menuItem.setBackground(SIDEBAR_COLOR);
                }
            });
        }
        
        return menuItem;
    }
    
    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(HEADER_BG);
        header.setPreferredSize(new Dimension(getWidth(), 80));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(15, 30, 15, 30)));
        
        // Left - Page Title
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        leftPanel.setBackground(HEADER_BG);
        
        JLabel lblTitle = new JLabel("Dashboard Overview");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));
        
        lblDateTime = new JLabel();
        lblDateTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDateTime.setForeground(new Color(100, 116, 139));
        
        leftPanel.add(lblTitle);
        leftPanel.add(lblDateTime);
        
        // Right - User Info
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightPanel.setBackground(HEADER_BG);
        
        // Notifications Button
        JButton btnNotifications = new JButton("🔔");
        btnNotifications.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btnNotifications.setPreferredSize(new Dimension(45, 45));
        btnNotifications.setBackground(Color.WHITE);
        btnNotifications.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        btnNotifications.setFocusPainted(false);
        btnNotifications.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNotifications.setToolTipText("Notifications");
        
        // Refresh Button
        JButton btnRefresh = new JButton("🔄");
        btnRefresh.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btnRefresh.setPreferredSize(new Dimension(45, 45));
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setToolTipText("Refresh Dashboard");
        btnRefresh.addActionListener(e -> loadDashboardData());
        
        // User Avatar and Info
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setBackground(HEADER_BG);
        
        JLabel userAvatar = new JLabel("👨‍💼");
        userAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        
        JPanel userInfo = new JPanel(new GridLayout(2, 1, 0, 0));
        userInfo.setBackground(HEADER_BG);
        
        lblWelcome = new JLabel(SessionManager.getInstance().getCurrentUser().getFullName());
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblWelcome.setForeground(new Color(30, 41, 59));
        
        lblRole = new JLabel(SessionManager.getInstance().getCurrentUser().getRole());
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRole.setForeground(new Color(100, 116, 139));
        
        userInfo.add(lblWelcome);
        userInfo.add(lblRole);
        
        userPanel.add(userAvatar);
        userPanel.add(userInfo);
        
        rightPanel.add(btnRefresh);
        rightPanel.add(btnNotifications);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(userPanel);
        
        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBackground(new Color(241, 245, 249));
        content.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        // Top Section - Stats Cards
        statsPanel = createStatsCards();
        content.add(statsPanel, BorderLayout.NORTH);
        
        // Middle Section - Quick Actions + Recent Activity
        JPanel middleSection = new JPanel(new GridLayout(1, 2, 25, 0));
        middleSection.setOpaque(false);
        
        quickActionsPanel = createQuickActionsPanel();
        recentActivityPanel = createRecentActivityPanel();
        
        middleSection.add(quickActionsPanel);
        middleSection.add(recentActivityPanel);
        
        // Bottom Section - Recent Transactions Table
        JPanel transactionsPanel = createRecentTransactionsPanel();
        
        JPanel centerPanel = new JPanel(new BorderLayout(0, 25));
        centerPanel.setOpaque(false);
        centerPanel.add(middleSection, BorderLayout.NORTH);
        centerPanel.add(transactionsPanel, BorderLayout.CENTER);
        
        content.add(centerPanel, BorderLayout.CENTER);
        
        return content;
    }
    
    private JPanel createStatsCards() {
        JPanel panel = new JPanel(new GridLayout(1, 6, 15, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(getWidth(), 110));
        
        lblTotalClients = new JLabel("0");
        lblActiveProjects = new JLabel("0");
        lblMonthlyRevenue = new JLabel("KSh 0");
        lblPendingPayments = new JLabel("KSh 0");
        lblTodayTransactions = new JLabel("0");
        lblWeeklyGrowth = new JLabel("0%");
        
        panel.add(createStatCard("👥", "Total Clients", lblTotalClients, ACCENT_BLUE, "+12%"));
        panel.add(createStatCard("📋", "Active Projects", lblActiveProjects, ACCENT_GREEN, "Active"));
        panel.add(createStatCard("💰", "Monthly Revenue", lblMonthlyRevenue, ACCENT_PURPLE, "This Month"));
        panel.add(createStatCard("⏳", "Pending Payments", lblPendingPayments, ACCENT_ORANGE, "Outstanding"));
        panel.add(createStatCard("📝", "Today's Orders", lblTodayTransactions, ACCENT_CYAN, "New"));
        panel.add(createStatCard("📈", "Weekly Growth", lblWeeklyGrowth, ACCENT_GREEN, "↑ Trending"));
        
        return panel;
    }
    
    private JPanel createStatCard(String icon, String title, JLabel valueLabel, Color accentColor, String badge) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        
        // Top Row - Icon and Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        
        JLabel lblBadge = new JLabel(badge);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(accentColor);
        
        topRow.add(lblIcon, BorderLayout.WEST);
        topRow.add(lblBadge, BorderLayout.EAST);
        
        // Value - Large bold number
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(new Color(30, 41, 59));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Title - Bold and larger
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(71, 85, 105));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(topRow);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(lblTitle);
        
        return card;
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(400, 220));
        
        JLabel lblTitle = new JLabel("⚡ Quick Actions");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(30, 41, 59));
        panel.add(lblTitle, BorderLayout.NORTH);
        
        JPanel buttonsGrid = new JPanel(new GridLayout(2, 2, 12, 12));
        buttonsGrid.setOpaque(false);
        
        buttonsGrid.add(createQuickActionButton("➕ New Client", ACCENT_BLUE, e -> openClientManagement()));
        buttonsGrid.add(createQuickActionButton("� Service Requests", ACCENT_GREEN, e -> openServiceRequests()));
        buttonsGrid.add(createQuickActionButton("💳 Record Payment", ACCENT_PURPLE, e -> openViewTransactions()));
        buttonsGrid.add(createQuickActionButton("📊 View Reports", ACCENT_ORANGE, e -> openReports()));
        
        panel.add(buttonsGrid, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createQuickActionButton(String text, Color color, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        
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
    
    private JPanel createRecentActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(400, 220));
        
        JLabel lblTitle = new JLabel("📢 Recent Activity");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(30, 41, 59));
        panel.add(lblTitle, BorderLayout.NORTH);
        
        JPanel activityList = new JPanel();
        activityList.setLayout(new BoxLayout(activityList, BoxLayout.Y_AXIS));
        activityList.setOpaque(false);
        
        activityList.add(createActivityItem("🆕", "New client registered", "John Doe - Industrial", "2 min ago", ACCENT_BLUE));
        activityList.add(Box.createVerticalStrut(8));
        activityList.add(createActivityItem("💳", "Payment received", "KSh 150,000", "15 min ago", ACCENT_GREEN));
        activityList.add(Box.createVerticalStrut(8));
        activityList.add(createActivityItem("📝", "New order created", "TXN-2026-001", "1 hour ago", ACCENT_PURPLE));
        activityList.add(Box.createVerticalStrut(8));
        activityList.add(createActivityItem("✅", "Service completed", "Drilling - Core", "3 hours ago", ACCENT_CYAN));
        
        panel.add(activityList, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActivityItem(String icon, String title, String detail, String time, Color color) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(500, 35));
        
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        
        JPanel textPanel = new JPanel(new GridLayout(1, 2));
        textPanel.setOpaque(false);
        
        JPanel leftText = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftText.setOpaque(false);
        JLabel lblTitle = new JLabel(title + " - ");
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(51, 65, 85));
        JLabel lblDetail = new JLabel(detail);
        lblDetail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDetail.setForeground(color);
        leftText.add(lblTitle);
        leftText.add(lblDetail);
        
        JLabel lblTime = new JLabel(time);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTime.setForeground(new Color(148, 163, 184));
        lblTime.setHorizontalAlignment(SwingConstants.RIGHT);
        
        textPanel.add(leftText);
        textPanel.add(lblTime);
        
        item.add(lblIcon, BorderLayout.WEST);
        item.add(textPanel, BorderLayout.CENTER);
        
        return item;
    }
    
    private JPanel createRecentTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("📋 Recent Transactions");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(30, 41, 59));
        
        JButton btnViewAll = new JButton("View All →");
        btnViewAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnViewAll.setForeground(ACCENT_BLUE);
        btnViewAll.setBackground(Color.WHITE);
        btnViewAll.setBorderPainted(false);
        btnViewAll.setFocusPainted(false);
        btnViewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewAll.addActionListener(e -> openViewTransactions());
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnViewAll, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"Transaction No", "Client", "Services", "Amount", "Paid", "Status", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        recentTransactionsTable = new JTable(tableModel);
        recentTransactionsTable.setRowHeight(45);
        recentTransactionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        recentTransactionsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        recentTransactionsTable.getTableHeader().setBackground(new Color(248, 250, 252));
        recentTransactionsTable.getTableHeader().setForeground(new Color(71, 85, 105));
        recentTransactionsTable.setShowGrid(false);
        recentTransactionsTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Status column renderer
        recentTransactionsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setHorizontalAlignment(CENTER);
                label.setOpaque(true);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                
                String status = value != null ? value.toString() : "";
                if (status.equals("PAID")) {
                    label.setBackground(new Color(220, 252, 231));
                    label.setForeground(ACCENT_GREEN);
                } else if (status.equals("PARTIAL")) {
                    label.setBackground(new Color(254, 243, 199));
                    label.setForeground(ACCENT_ORANGE);
                } else {
                    label.setBackground(new Color(254, 226, 226));
                    label.setForeground(ACCENT_RED);
                }
                
                return label;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(recentTransactionsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadDashboardData() {
        try {
            ClientDAO clientDAO = new ClientDAO();
            
            // Load stats
            int totalClients = clientDAO.getTotalClientCount();
            lblTotalClients.setText(String.valueOf(totalClients));
            
            // Load recent transactions
            List<Transaction> transactions = TransactionDAO.getAllTransactions();
            tableModel.setRowCount(0);
            
            double monthlyRevenue = 0;
            double pendingPayments = 0;
            int activeProjects = 0;
            int todayCount = 0;
            
            Calendar today = Calendar.getInstance();
            Calendar txnDate = Calendar.getInstance();
            
            int count = 0;
            for (Transaction t : transactions) {
                if (count < 10) {
                    String services = "";
                    if (t.getDrillingCost() > 0) services += "Drilling ";
                    if (t.getPumpCost() > 0) services += "Pump ";
                    if (t.getTankCost() > 0) services += "Tank ";
                    
                    tableModel.addRow(new Object[]{
                        t.getTransactionNo(),
                        t.getClientId(),
                        services.trim(),
                        String.format("KSh %,.0f", t.getGrandTotal()),
                        String.format("KSh %,.0f", t.getAmountPaid()),
                        t.getPaymentStatus(),
                        UIHelper.formatDate(t.getTransactionDate())
                    });
                    count++;
                }
                
                // Calculate stats
                monthlyRevenue += t.getAmountPaid();
                pendingPayments += t.getBalance();
                
                if (!t.getPaymentStatus().equals("PAID")) {
                    activeProjects++;
                }
                
                txnDate.setTime(t.getTransactionDate());
                if (txnDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    txnDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                    todayCount++;
                }
            }
            
            lblMonthlyRevenue.setText(String.format("KSh %,.0f", monthlyRevenue));
            lblPendingPayments.setText(String.format("KSh %,.0f", pendingPayments));
            lblActiveProjects.setText(String.valueOf(activeProjects));
            lblTodayTransactions.setText(String.valueOf(todayCount));
            lblWeeklyGrowth.setText("+15%");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startClock() {
        clockTimer = new Timer();
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy | hh:mm:ss a");
                    lblDateTime.setText(sdf.format(new Date()));
                });
            }
        }, 0, 1000);
    }
    
    private void openClientManagement() {
        new ClientManagementForm().setVisible(true);
    }
    
    private void openServiceRequests() {
        // Show pending service requests from customers
        showServiceRequestsDialog();
    }
    
    private void showServiceRequestsDialog() {
        JDialog dialog = new JDialog(this, "📨 Customer Service Requests", true);
        dialog.setSize(1200, 700);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ACCENT_BLUE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblHeader = new JLabel("📨 Customer Service Requests Management");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(Color.WHITE);
        
        JLabel lblSubtitle = new JLabel("Review, approve, and process customer borehole service applications");
        lblSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSubtitle.setForeground(new Color(200, 220, 255));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(lblHeader);
        titlePanel.add(lblSubtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Table with request data
        String[] columns = {"Request ID", "Client Name", "Category", "Services Requested", "Survey Fee", "Local Auth", "Total Est.", "Date", "Status"};
        Object[][] data = {
            {"REQ-45821", "John Kamau", "DOMESTIC", "Symmetric Drilling (50m), Submersible Pump, 2000L Tank", "KSh 7,000", "KSh 10,000", "KSh 385,720", "2026-01-20", "PENDING"},
            {"REQ-45820", "Mary Wanjiku", "COMMERCIAL", "Core Drilling (80m), Solar Pump, Plumbing", "KSh 15,000", "KSh 30,000", "KSh 498,200", "2026-01-19", "PENDING"},
            {"REQ-45819", "Peter Omondi", "INDUSTRIAL", "Geo-Technical (120m), Submersible, 10000L Tank, Plumbing", "KSh 20,000", "KSh 50,000", "KSh 892,500", "2026-01-18", "APPROVED"},
            {"REQ-45818", "Grace Akinyi", "DOMESTIC", "Symmetric Drilling (40m), Hand Pump", "KSh 7,000", "KSh 10,000", "KSh 215,880", "2026-01-17", "IN PROGRESS"},
            {"REQ-45817", "James Mwangi", "COMMERCIAL", "Core Drilling (100m), Solar Pump, 5000L Tank", "KSh 15,000", "KSh 30,000", "KSh 562,400", "2026-01-16", "COMPLETED"}
        };
        
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
        
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setRowHeight(50);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(30, 41, 59));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(219, 234, 254));
        
        // Status column renderer
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value.toString());
                label.setHorizontalAlignment(CENTER);
                label.setOpaque(true);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                
                switch (value.toString()) {
                    case "PENDING":
                        label.setBackground(new Color(254, 243, 199));
                        label.setForeground(new Color(180, 83, 9));
                        break;
                    case "APPROVED":
                        label.setBackground(new Color(220, 252, 231));
                        label.setForeground(new Color(22, 163, 74));
                        break;
                    case "IN PROGRESS":
                        label.setBackground(new Color(219, 234, 254));
                        label.setForeground(new Color(37, 99, 235));
                        break;
                    case "COMPLETED":
                        label.setBackground(new Color(233, 213, 255));
                        label.setForeground(new Color(126, 34, 206));
                        break;
                    default:
                        label.setBackground(Color.WHITE);
                }
                return label;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Bottom Panel with buttons and info
        JPanel bottomPanel = new JPanel(new BorderLayout(20, 10));
        bottomPanel.setBackground(Color.WHITE);
        
        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        buttonPanel.setOpaque(false);
        
        JButton btnView = new JButton("👁️ View Details");
        btnView.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnView.setBackground(ACCENT_BLUE);
        btnView.setForeground(Color.WHITE);
        btnView.setBorderPainted(false);
        btnView.setFocusPainted(false);
        btnView.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnView.setPreferredSize(new Dimension(140, 40));
        btnView.addActionListener(e -> showRequestDetails(table, tableModel));
        
        JButton btnApprove = new JButton("✅ Approve");
        btnApprove.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnApprove.setBackground(ACCENT_GREEN);
        btnApprove.setForeground(Color.WHITE);
        btnApprove.setBorderPainted(false);
        btnApprove.setFocusPainted(false);
        btnApprove.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnApprove.setPreferredSize(new Dimension(120, 40));
        btnApprove.addActionListener(e -> approveRequest(table, tableModel));
        
        JButton btnReject = new JButton("❌ Reject");
        btnReject.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnReject.setBackground(ACCENT_RED);
        btnReject.setForeground(Color.WHITE);
        btnReject.setBorderPainted(false);
        btnReject.setFocusPainted(false);
        btnReject.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReject.setPreferredSize(new Dimension(120, 40));
        btnReject.addActionListener(e -> rejectRequest(table, tableModel));
        
        JButton btnConvert = new JButton("📋 Convert to Transaction");
        btnConvert.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConvert.setBackground(ACCENT_PURPLE);
        btnConvert.setForeground(Color.WHITE);
        btnConvert.setBorderPainted(false);
        btnConvert.setFocusPainted(false);
        btnConvert.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConvert.setPreferredSize(new Dimension(200, 40));
        btnConvert.addActionListener(e -> convertToTransaction(table, tableModel));
        
        buttonPanel.add(btnView);
        buttonPanel.add(btnApprove);
        buttonPanel.add(btnReject);
        buttonPanel.add(btnConvert);
        
        // Pricing Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(240, 253, 244));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(187, 247, 208)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        
        JLabel lblInfo1 = new JLabel("📊 PRICING REFERENCE (Official Rates):");
        lblInfo1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblInfo1.setForeground(new Color(22, 101, 52));
        
        JLabel lblInfo2 = new JLabel("<html><small>" +
            "<b>Survey Fees:</b> Industrial KSh 20,000 | Commercial KSh 15,000 | Domestic KSh 7,000 &nbsp;&nbsp;|&nbsp;&nbsp;" +
            "<b>Local Auth:</b> Industrial KSh 50,000 | Commercial KSh 30,000 | Domestic KSh 10,000</small></html>");
        lblInfo2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblInfo2.setForeground(new Color(22, 101, 52));
        
        JLabel lblInfo3 = new JLabel("<html><small>" +
            "<b>Drilling:</b> Symmetric KSh 130,000 | Core KSh 225,000 | Geo-Technical KSh 335,000 &nbsp;&nbsp;|&nbsp;&nbsp;" +
            "<b>Pumps:</b> Submersible KSh 90,000 | Solar KSh 65,000 | Hand KSh 30,000 &nbsp;&nbsp;|&nbsp;&nbsp;" +
            "<b>Tax:</b> 16%</small></html>");
        lblInfo3.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblInfo3.setForeground(new Color(22, 101, 52));
        
        infoPanel.add(lblInfo1);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblInfo2);
        infoPanel.add(lblInfo3);
        
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(infoPanel, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }
    
    private void showRequestDetails(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.showWarning(this, "Please select a request to view.");
            return;
        }
        
        String requestId = model.getValueAt(row, 0).toString();
        String clientName = model.getValueAt(row, 1).toString();
        String category = model.getValueAt(row, 2).toString();
        String services = model.getValueAt(row, 3).toString();
        String surveyFee = model.getValueAt(row, 4).toString();
        String localAuth = model.getValueAt(row, 5).toString();
        String total = model.getValueAt(row, 6).toString();
        String date = model.getValueAt(row, 7).toString();
        String status = model.getValueAt(row, 8).toString();
        
        JOptionPane.showMessageDialog(this,
            "<html><div style='width: 450px; padding: 15px;'>" +
            "<h2 style='color: #1E3A8A;'>📋 Service Request Details</h2>" +
            "<hr>" +
            "<table style='font-size: 13px;'>" +
            "<tr><td><b>Request ID:</b></td><td>" + requestId + "</td></tr>" +
            "<tr><td><b>Client Name:</b></td><td>" + clientName + "</td></tr>" +
            "<tr><td><b>Category:</b></td><td>" + category + "</td></tr>" +
            "<tr><td><b>Date Submitted:</b></td><td>" + date + "</td></tr>" +
            "<tr><td><b>Status:</b></td><td><span style='color: #22C55E; font-weight: bold;'>" + status + "</span></td></tr>" +
            "</table>" +
            "<hr>" +
            "<p><b>Services Requested:</b></p>" +
            "<p style='background: #F8FAFC; padding: 10px; border-radius: 5px;'>" + services + "</p>" +
            "<hr>" +
            "<table style='font-size: 13px;'>" +
            "<tr><td><b>Survey Fee:</b></td><td>" + surveyFee + "</td></tr>" +
            "<tr><td><b>Local Authority Fee:</b></td><td>" + localAuth + "</td></tr>" +
            "<tr><td><b>Total Estimated Cost:</b></td><td style='color: #22C55E; font-weight: bold; font-size: 16px;'>" + total + "</td></tr>" +
            "</table>" +
            "<p style='color: #666; font-size: 11px; margin-top: 15px;'>* Total includes 16% VAT</p>" +
            "</div></html>",
            "Request Details", JOptionPane.PLAIN_MESSAGE);
    }
    
    private void approveRequest(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.showWarning(this, "Please select a request to approve.");
            return;
        }
        
        String status = model.getValueAt(row, 8).toString();
        if (!"PENDING".equals(status)) {
            UIHelper.showWarning(this, "Only PENDING requests can be approved.");
            return;
        }
        
        String requestId = model.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Approve service request " + requestId + "?\n\nThe customer will be notified to proceed with 30% down payment.",
            "Confirm Approval", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            model.setValueAt("APPROVED", row, 8);
            UIHelper.showInfo(this, "✅ Request " + requestId + " has been approved!\n\nCustomer will be notified.");
        }
    }
    
    private void rejectRequest(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.showWarning(this, "Please select a request to reject.");
            return;
        }
        
        String status = model.getValueAt(row, 8).toString();
        if (!"PENDING".equals(status)) {
            UIHelper.showWarning(this, "Only PENDING requests can be rejected.");
            return;
        }
        
        String requestId = model.getValueAt(row, 0).toString();
        String reason = JOptionPane.showInputDialog(this, "Enter rejection reason:", "Reject Request", JOptionPane.WARNING_MESSAGE);
        
        if (reason != null && !reason.trim().isEmpty()) {
            model.removeRow(row);
            UIHelper.showInfo(this, "❌ Request " + requestId + " has been rejected.\n\nReason: " + reason);
        }
    }
    
    private void convertToTransaction(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.showWarning(this, "Please select a request to convert.");
            return;
        }
        
        String status = model.getValueAt(row, 8).toString();
        if (!"APPROVED".equals(status)) {
            UIHelper.showWarning(this, "Only APPROVED requests can be converted to transactions.\n\nPlease approve the request first.");
            return;
        }
        
        String requestId = model.getValueAt(row, 0).toString();
        String clientName = model.getValueAt(row, 1).toString();
        String total = model.getValueAt(row, 6).toString();
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><div style='padding: 10px;'>" +
            "<h3>Convert to Transaction?</h3>" +
            "<p><b>Request:</b> " + requestId + "</p>" +
            "<p><b>Client:</b> " + clientName + "</p>" +
            "<p><b>Total:</b> " + total + "</p>" +
            "<p style='color: #666;'>This will create an official transaction record.</p>" +
            "</div></html>",
            "Create Transaction", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            model.setValueAt("IN PROGRESS", row, 8);
            String txnNo = "TXN-2026-" + String.format("%04d", (int)(Math.random() * 9999));
            UIHelper.showInfo(this, "✅ Transaction Created Successfully!\n\n" +
                "Transaction No: " + txnNo + "\n" +
                "Client: " + clientName + "\n" +
                "Amount: " + total + "\n\n" +
                "The request status is now IN PROGRESS.");
        }
    }
    
    private void openViewTransactions() {
        new ViewTransactionsForm().setVisible(true);
    }
    
    private void openReports() {
        new ReportsForm().setVisible(true);
    }
    
    private void openServicesManagement() {
        new ServicesManagementForm().setVisible(true);
    }
    
    private void openPayments() {
        UIHelper.showInfo(this, "Navigate to View Transactions to record payments.");
    }
    
    private void openUserManagement() {
        new UserManagementForm().setVisible(true);
    }
    
    private void openSettings() {
        UIHelper.showInfo(this, "⚙️ Settings\n\nSystem settings module coming soon!");
    }
    
    private void showAbout() {
        JOptionPane.showMessageDialog(this, 
            "<html><div style='text-align: center; padding: 20px;'>" +
            "<h1 style='color: #1976D2;'>💧 UZIMA BOREHOLE</h1>" +
            "<p>Drilling & Services Management System</p>" +
            "<hr><p><b>Version:</b> 1.0.0</p>" +
            "<p>© 2026 Uzima Borehole Company</p></div></html>", 
            "About UZIMA", JOptionPane.PLAIN_MESSAGE);
    }
    
    private void handleLogout() {
        if (UIHelper.showConfirm(this, "Are you sure you want to logout?")) {
            if (clockTimer != null) clockTimer.cancel();
            SessionManager.getInstance().logout();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
            dispose();
        }
    }
    
    @Override
    public void dispose() {
        if (clockTimer != null) clockTimer.cancel();
        super.dispose();
    }
}
