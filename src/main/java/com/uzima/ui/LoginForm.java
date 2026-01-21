package com.uzima.ui;

import com.uzima.dao.ClientDAO;
import com.uzima.dao.UserDAO;
import com.uzima.model.Client;
import com.uzima.model.User;
import com.uzima.util.DatabaseConnection;
import com.uzima.util.SessionManager;
import com.uzima.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Professional Login Form with Staff and Customer Portal
 */
public class LoginForm extends JFrame {
    private JTextField txtStaffUsername, txtCustomerId;
    private JPasswordField txtStaffPassword, txtCustomerPassword;
    private JButton btnStaffLogin, btnCustomerLogin, btnRegisterCustomer, btnExit;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private UserDAO userDAO;
    private ClientDAO clientDAO;
    
    // Colors
    private static final Color STAFF_COLOR = new Color(25, 118, 210);
    private static final Color CUSTOMER_COLOR = new Color(56, 142, 60);
    
    public LoginForm() {
        userDAO = new UserDAO();
        clientDAO = new ClientDAO();
        initComponents();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("UZIMA Borehole System");
        setSize(550, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Main Panel with gradient
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHints(UIHelper.getHighQualityRenderingHints());
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(30, 60, 114),
                    0, getHeight(), new Color(42, 82, 152));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        
        // Login Card
        JPanel loginCard = new JPanel(new BorderLayout(0, 0));
        loginCard.setBackground(Color.WHITE);
        loginCard.setPreferredSize(new Dimension(450, 620));
        loginCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        
        // Logo Panel
        JPanel logoPanel = createLogoPanel();
        loginCard.add(logoPanel, BorderLayout.NORTH);
        
        // Tab Buttons
        JPanel tabPanel = createTabPanel();
        
        // Card Panel for switching between Staff and Customer
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);
        
        cardPanel.add(createStaffLoginPanel(), "STAFF");
        cardPanel.add(createCustomerLoginPanel(), "CUSTOMER");
        
        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(tabPanel, BorderLayout.NORTH);
        contentPanel.add(cardPanel, BorderLayout.CENTER);
        
        loginCard.add(contentPanel, BorderLayout.CENTER);
        
        // Exit Button at bottom
        JPanel bottomPanel = createBottomPanel();
        loginCard.add(bottomPanel, BorderLayout.SOUTH);
        
        mainPanel.add(loginCard);
        add(mainPanel);
    }
    
    private JPanel createLogoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));
        
        // Logo icon
        JLabel lblIcon = new JLabel("💧");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Title
        JLabel lblTitle = new JLabel("UZIMA BOREHOLE");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(30, 60, 114));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Subtitle
        JLabel lblSubtitle = new JLabel("Drilling & Services Management");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(Color.GRAY);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(lblIcon);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblSubtitle);
        
        return panel;
    }
    
    private JPanel createTabPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 0, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        JButton btnStaffTab = createTabButton("👤 STAFF LOGIN", STAFF_COLOR, true);
        JButton btnCustomerTab = createTabButton("🏠 CUSTOMER PORTAL", CUSTOMER_COLOR, false);
        
        btnStaffTab.addActionListener(e -> {
            cardLayout.show(cardPanel, "STAFF");
            btnStaffTab.setBackground(STAFF_COLOR);
            btnStaffTab.setForeground(Color.WHITE);
            btnCustomerTab.setBackground(new Color(245, 245, 245));
            btnCustomerTab.setForeground(Color.GRAY);
        });
        
        btnCustomerTab.addActionListener(e -> {
            cardLayout.show(cardPanel, "CUSTOMER");
            btnCustomerTab.setBackground(CUSTOMER_COLOR);
            btnCustomerTab.setForeground(Color.WHITE);
            btnStaffTab.setBackground(new Color(245, 245, 245));
            btnStaffTab.setForeground(Color.GRAY);
        });
        
        panel.add(btnStaffTab);
        panel.add(btnCustomerTab);
        
        return panel;
    }
    
    private JButton createTabButton(String text, Color activeColor, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(200, 50));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (isActive) {
            btn.setBackground(activeColor);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(245, 245, 245));
            btn.setForeground(Color.GRAY);
        }
        
        return btn;
    }
    
    private JPanel createStaffLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));
        
        // Username
        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(UIHelper.LABEL_FONT);
        lblUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtStaffUsername = createStyledTextField();
        
        // Password
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(UIHelper.LABEL_FONT);
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtStaffPassword = createStyledPasswordField();
        
        // Login Button
        btnStaffLogin = new JButton("LOGIN TO DASHBOARD");
        btnStaffLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnStaffLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnStaffLogin.setMaximumSize(new Dimension(370, 50));
        btnStaffLogin.setPreferredSize(new Dimension(370, 50));
        btnStaffLogin.setBackground(STAFF_COLOR);
        btnStaffLogin.setForeground(Color.WHITE);
        btnStaffLogin.setFocusPainted(false);
        btnStaffLogin.setBorderPainted(false);
        btnStaffLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStaffLogin.addActionListener(e -> handleStaffLogin());
        
        // Credentials hint
        JPanel hintPanel = new JPanel();
        hintPanel.setLayout(new BoxLayout(hintPanel, BoxLayout.Y_AXIS));
        hintPanel.setBackground(new Color(232, 245, 253));
        hintPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(33, 150, 243), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        hintPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        hintPanel.setMaximumSize(new Dimension(370, 60));
        
        JLabel lblHint = new JLabel("Demo: admin / admin123");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(new Color(33, 150, 243));
        lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hintPanel.add(lblHint);
        
        panel.add(lblUsername);
        panel.add(Box.createVerticalStrut(8));
        panel.add(txtStaffUsername);
        panel.add(Box.createVerticalStrut(20));
        panel.add(lblPassword);
        panel.add(Box.createVerticalStrut(8));
        panel.add(txtStaffPassword);
        panel.add(Box.createVerticalStrut(25));
        panel.add(btnStaffLogin);
        panel.add(Box.createVerticalStrut(20));
        panel.add(hintPanel);
        
        // Enter key to login
        txtStaffPassword.addActionListener(e -> handleStaffLogin());
        
        return panel;
    }
    
    private JPanel createCustomerLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));
        
        // Customer ID
        JLabel lblCustomerId = new JLabel("Customer ID (e.g., UZM-IND-001)");
        lblCustomerId.setFont(UIHelper.LABEL_FONT);
        lblCustomerId.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtCustomerId = createStyledTextField();
        
        // Password
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(UIHelper.LABEL_FONT);
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtCustomerPassword = createStyledPasswordField();
        
        // Login Button
        btnCustomerLogin = new JButton("LOGIN TO MY ACCOUNT");
        btnCustomerLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCustomerLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCustomerLogin.setMaximumSize(new Dimension(370, 50));
        btnCustomerLogin.setPreferredSize(new Dimension(370, 50));
        btnCustomerLogin.setBackground(CUSTOMER_COLOR);
        btnCustomerLogin.setForeground(Color.WHITE);
        btnCustomerLogin.setFocusPainted(false);
        btnCustomerLogin.setBorderPainted(false);
        btnCustomerLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCustomerLogin.addActionListener(e -> handleCustomerLogin());
        
        // Register Button
        btnRegisterCustomer = new JButton("REGISTER AS NEW CUSTOMER");
        btnRegisterCustomer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegisterCustomer.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegisterCustomer.setMaximumSize(new Dimension(370, 50));
        btnRegisterCustomer.setPreferredSize(new Dimension(370, 50));
        btnRegisterCustomer.setBackground(Color.WHITE);
        btnRegisterCustomer.setForeground(CUSTOMER_COLOR);
        btnRegisterCustomer.setFocusPainted(false);
        btnRegisterCustomer.setBorder(BorderFactory.createLineBorder(CUSTOMER_COLOR, 2));
        btnRegisterCustomer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegisterCustomer.addActionListener(e -> openCustomerRegistration());
        
        panel.add(lblCustomerId);
        panel.add(Box.createVerticalStrut(8));
        panel.add(txtCustomerId);
        panel.add(Box.createVerticalStrut(20));
        panel.add(lblPassword);
        panel.add(Box.createVerticalStrut(8));
        panel.add(txtCustomerPassword);
        panel.add(Box.createVerticalStrut(25));
        panel.add(btnCustomerLogin);
        panel.add(Box.createVerticalStrut(15));
        panel.add(btnRegisterCustomer);
        
        // Enter key to login
        txtCustomerPassword.addActionListener(e -> handleCustomerLogin());
        
        return panel;
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(370, 48));
        field.setPreferredSize(new Dimension(370, 48));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Focus effects
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(STAFF_COLOR, 2),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)));
            }
        });
        
        return field;
    }
    
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(370, 48));
        field.setPreferredSize(new Dimension(370, 48));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(STAFF_COLOR, 2),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)));
            }
        });
        
        return field;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        
        btnExit = new JButton("Exit Application");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExit.setForeground(Color.GRAY);
        btnExit.setBackground(new Color(250, 250, 250));
        btnExit.setBorderPainted(false);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.addActionListener(e -> System.exit(0));
        
        panel.add(btnExit);
        
        return panel;
    }
    
    private void handleStaffLogin() {
        String username = txtStaffUsername.getText().trim();
        String password = new String(txtStaffPassword.getPassword());
        
        if (username.isEmpty()) {
            UIHelper.showError(this, "Please enter username!");
            txtStaffUsername.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            UIHelper.showError(this, "Please enter password!");
            txtStaffPassword.requestFocus();
            return;
        }
        
        if (!DatabaseConnection.testConnection()) {
            UIHelper.showError(this, "Cannot connect to database!");
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        User user = userDAO.authenticate(username, password);
        setCursor(Cursor.getDefaultCursor());
        
        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            UIHelper.showSuccess(this, "Welcome, " + user.getFullName() + "!");
            
            SwingUtilities.invokeLater(() -> {
                DashboardForm dashboard = new DashboardForm();
                dashboard.setVisible(true);
            });
            dispose();
        } else {
            UIHelper.showError(this, "Invalid username or password!");
            txtStaffPassword.setText("");
            txtStaffUsername.requestFocus();
        }
    }
    
    private void handleCustomerLogin() {
        String customerId = txtCustomerId.getText().trim().toUpperCase();
        String password = new String(txtCustomerPassword.getPassword());
        
        if (customerId.isEmpty()) {
            UIHelper.showError(this, "Please enter your Customer ID!");
            txtCustomerId.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            UIHelper.showError(this, "Please enter your password!");
            txtCustomerPassword.requestFocus();
            return;
        }
        
        if (!DatabaseConnection.testConnection()) {
            UIHelper.showError(this, "Cannot connect to database!");
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        try {
            Client client = clientDAO.authenticateClient(customerId, password);
            setCursor(Cursor.getDefaultCursor());
            
            if (client != null) {
                UIHelper.showSuccess(this, "Welcome back, " + client.getFullName() + "!");
                
                SwingUtilities.invokeLater(() -> {
                    CustomerDashboard dashboard = new CustomerDashboard(client);
                    dashboard.setVisible(true);
                });
                dispose();
            } else {
                UIHelper.showError(this, "Invalid Customer ID or password!");
                txtCustomerPassword.setText("");
                txtCustomerId.requestFocus();
            }
        } catch (Exception e) {
            setCursor(Cursor.getDefaultCursor());
            UIHelper.showError(this, "Login error: " + e.getMessage());
        }
    }
    
    private void openCustomerRegistration() {
        CustomerRegistrationForm regForm = new CustomerRegistrationForm(this);
        regForm.setVisible(true);
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }
}
