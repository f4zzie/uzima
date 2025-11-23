package com.uzima;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import com.uzima.services.ClientService;
import com.uzima.models.Client;
import com.uzima.ui.MainMenuPanel;

/**
 * UZIMA BOREHOLE DRILLING SYSTEM - MAIN APPLICATION ENTRY POINT
 * Integrated application with Login, Sign Up, Admin Panel, and Main Menu
 */
public class Main extends JFrame {
    
    // Color Palette
    private static final Color PRIMARY_DARK = new Color(12, 45, 97);
    private static final Color PRIMARY_ACCENT = new Color(0, 122, 204);
    private static final Color SECONDARY_ACCENT = new Color(52, 168, 224);
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255);
    private static final Color FIELD_BACKGROUND = new Color(248, 249, 252);
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color INFO_COLOR = new Color(23, 162, 184);
    
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    public Main() {
        setTitle("UZIMA Borehole Drilling System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Initialize card layout for switching between screens
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Add different screens
        contentPanel.add(createLoginPanel(), "LOGIN");
        contentPanel.add(createSignUpPanel(), "SIGNUP");
        contentPanel.add(createAdminPanel(), "ADMIN");
        
        setContentPane(contentPanel);
        showLogin();
        setVisible(true);
    }
    
    // ============ LOGIN SCREEN ============
    private JPanel createLoginPanel() {
        JPanel mainContainer = createGradientPanel();
        mainContainer.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Left side - Branding
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(PRIMARY_DARK);
        leftPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints leftGbc = new GridBagConstraints();
        leftGbc.gridx = 0;
        leftGbc.gridy = 0;
        leftGbc.insets = new Insets(30, 50, 30, 50);
        leftGbc.anchor = GridBagConstraints.CENTER;
        
        JLabel logoLabel = new JLabel("UZIMA");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        logoLabel.setForeground(SECONDARY_ACCENT);
        leftPanel.add(logoLabel, leftGbc);
        
        leftGbc.gridy++;
        leftGbc.insets = new Insets(10, 50, 30, 50);
        JLabel tagline = new JLabel("Borehole Drilling Solutions");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tagline.setForeground(Color.WHITE);
        leftPanel.add(tagline, leftGbc);
        
        leftGbc.gridy++;
        leftGbc.insets = new Insets(40, 50, 30, 50);
        JLabel descLabel = new JLabel("<html>Professional borehole drilling and <br>pump installation services across Kenya</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(200, 200, 200));
        leftPanel.add(descLabel, leftGbc);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        mainContainer.add(leftPanel, gbc);
        
        // Right side - Login Form
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(CARD_BACKGROUND);
        rightPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightGbc.gridwidth = 2;
        rightGbc.insets = new Insets(40, 60, 20, 60);
        rightGbc.anchor = GridBagConstraints.CENTER;
        
        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_DARK);
        rightPanel.add(titleLabel, rightGbc);
        
        rightGbc.gridy++;
        rightGbc.insets = new Insets(0, 60, 40, 60);
        JLabel subtitleLabel = new JLabel("Sign in to your account to continue");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        rightPanel.add(subtitleLabel, rightGbc);
        
        // Email field
        rightGbc.gridy++;
        rightGbc.gridwidth = 1;
        rightGbc.insets = new Insets(15, 60, 5, 60);
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setForeground(TEXT_PRIMARY);
        rightPanel.add(emailLabel, rightGbc);
        
        rightGbc.gridy++;
        rightGbc.insets = new Insets(5, 60, 15, 60);
        JTextField emailField = new JTextField();
        styleTextField(emailField);
        rightPanel.add(emailField, rightGbc);
        
        // Password field
        rightGbc.gridy++;
        rightGbc.insets = new Insets(15, 60, 5, 60);
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordLabel.setForeground(TEXT_PRIMARY);
        rightPanel.add(passwordLabel, rightGbc);
        
        rightGbc.gridy++;
        rightGbc.insets = new Insets(5, 60, 15, 60);
        JPasswordField passwordField = new JPasswordField();
        styleTextField(passwordField);
        rightPanel.add(passwordField, rightGbc);
        
        // Login Button
        rightGbc.gridy++;
        rightGbc.insets = new Insets(25, 60, 10, 60);
        JButton loginButton = new JButton("Sign In");
        styleButton(loginButton, PRIMARY_ACCENT);
        loginButton.addActionListener(e -> handleLogin(emailField.getText(), new String(passwordField.getPassword())));
        rightPanel.add(loginButton, rightGbc);
        
        // Sign Up Link
        rightGbc.gridy++;
        rightGbc.insets = new Insets(10, 60, 40, 60);
        JLabel signUpPrompt = new JLabel("Don't have an account? Sign up");
        signUpPrompt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        signUpPrompt.setForeground(PRIMARY_ACCENT);
        signUpPrompt.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpPrompt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showSignUp();
            }
        });
        rightPanel.add(signUpPrompt, rightGbc);
        
        // Demo Credentials
        rightGbc.gridy++;
        rightGbc.insets = new Insets(10, 60, 20, 60);
        JLabel demoLabel = new JLabel("<html><b>Demo:</b> admin@uzima.com / password123</html>");
        demoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        demoLabel.setForeground(WARNING_COLOR);
        rightPanel.add(demoLabel, rightGbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        mainContainer.add(rightPanel, gbc);
        
        return mainContainer;
    }
    
    // ============ SIGN UP SCREEN ============
    private JPanel createSignUpPanel() {
        JPanel mainContainer = new JPanel();
        mainContainer.setBackground(CARD_BACKGROUND);
        mainContainer.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Header
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 60, 10, 60);
        gbc.anchor = GridBagConstraints.CENTER;
        
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_DARK);
        mainContainer.add(titleLabel, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 60, 30, 60);
        JLabel subtitleLabel = new JLabel("Register a new account to get started");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        mainContainer.add(subtitleLabel, gbc);
        
        // Form fields
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Full Name
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(10, 60, 2, 30);
        mainContainer.add(new JLabel("Full Name:"), gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(2, 60, 10, 30);
        JTextField nameField = new JTextField();
        styleTextField(nameField);
        mainContainer.add(nameField, gbc);
        
        // Email
        gbc.gridx = 1;
        gbc.gridy -= 2;
        gbc.insets = new Insets(10, 30, 2, 60);
        mainContainer.add(new JLabel("Email Address:"), gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(2, 30, 10, 60);
        JTextField emailField = new JTextField();
        styleTextField(emailField);
        mainContainer.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(10, 60, 2, 30);
        mainContainer.add(new JLabel("Phone Number:"), gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(2, 60, 10, 30);
        JTextField phoneField = new JTextField();
        styleTextField(phoneField);
        mainContainer.add(phoneField, gbc);
        
        // County
        gbc.gridx = 1;
        gbc.gridy -= 2;
        gbc.insets = new Insets(10, 30, 2, 60);
        mainContainer.add(new JLabel("County:"), gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(2, 30, 10, 60);
        String[] counties = {"Nairobi", "Mombasa", "Kisumu", "Nakuru", "Uasin Gishu", "Kiambu", "Muranga", "Nyeri"};
        JComboBox<String> countyBox = new JComboBox<>(counties);
        mainContainer.add(countyBox, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(10, 60, 2, 30);
        mainContainer.add(new JLabel("Password:"), gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(2, 60, 10, 30);
        JPasswordField passwordField = new JPasswordField();
        styleTextField(passwordField);
        mainContainer.add(passwordField, gbc);
        
        // Confirm Password
        gbc.gridx = 1;
        gbc.gridy -= 2;
        gbc.insets = new Insets(10, 30, 2, 60);
        mainContainer.add(new JLabel("Confirm Password:"), gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(2, 30, 10, 60);
        JPasswordField confirmPasswordField = new JPasswordField();
        styleTextField(confirmPasswordField);
        mainContainer.add(confirmPasswordField, gbc);
        
        // Service Category
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 60, 2, 60);
        mainContainer.add(new JLabel("Service Category:"), gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(2, 60, 15, 60);
        String[] categories = {"Domestic", "Commercial", "Industrial"};
        JComboBox<String> categoryBox = new JComboBox<>(categories);
        mainContainer.add(categoryBox, gbc);
        
        // Sign Up Button
        gbc.gridy++;
        gbc.insets = new Insets(20, 60, 10, 60);
        JButton signUpButton = new JButton("Create Account");
        styleButton(signUpButton, SUCCESS_COLOR);
        signUpButton.addActionListener(e -> handleSignUp(
            nameField.getText(),
            emailField.getText(),
            phoneField.getText(),
            (String) countyBox.getSelectedItem(),
            (String) categoryBox.getSelectedItem(),
            new String(passwordField.getPassword()),
            new String(confirmPasswordField.getPassword())
        ));
        mainContainer.add(signUpButton, gbc);
        
        // Back to Login Link
        gbc.gridy++;
        gbc.insets = new Insets(10, 60, 40, 60);
        JLabel backLabel = new JLabel("Already have an account? Sign in");
        backLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backLabel.setForeground(PRIMARY_ACCENT);
        backLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showLogin();
            }
        });
        mainContainer.add(backLabel, gbc);
        
        return mainContainer;
    }
    
    // ============ ADMIN PANEL SCREEN ============
    private JPanel createAdminPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(CARD_BACKGROUND);
        
        // Top panel with title and logout
        JPanel topPanel = new JPanel();
        topPanel.setBackground(PRIMARY_DARK);
        topPanel.setLayout(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Admin Dashboard - Client Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton logoutButton = new JButton("🚪 Logout");
        styleButton(logoutButton, DANGER_COLOR);
        logoutButton.addActionListener(e -> showLogin());
        topPanel.add(logoutButton, BorderLayout.EAST);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Search and Filter Panel
        JPanel filterPanel = new JPanel();
        filterPanel.setBackground(new Color(240, 242, 245));
        filterPanel.setLayout(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Search field
        JLabel searchLabel = new JLabel("🔍 Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(searchLabel, gbc);
        
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        styleTextField(searchField);
        gbc.gridx = 1;
        filterPanel.add(searchField, gbc);
        
        // Sort By dropdown
        JLabel sortLabel = new JLabel("🔤 Sort By:");
        sortLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridx = 2;
        filterPanel.add(sortLabel, gbc);
        
        String[] sortOptions = {"Name (A-Z)", "Name (Z-A)", "Email (A-Z)", "ID (Ascending)", "ID (Descending)", "County"};
        JComboBox<String> sortCombo = new JComboBox<>(sortOptions);
        sortCombo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        gbc.gridx = 3;
        filterPanel.add(sortCombo, gbc);
        
        // Filter by Category
        JLabel categoryLabel = new JLabel("📂 Category:");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        gbc.gridx = 4;
        filterPanel.add(categoryLabel, gbc);
        
        String[] categories = {"All", "Borehole", "Pump Installation", "Plumbing", "Tank Installation"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        gbc.gridx = 5;
        filterPanel.add(categoryCombo, gbc);
        
        mainPanel.add(filterPanel, BorderLayout.NORTH);
        
        // Center - Clients Table
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columnNames = {"ID", "Name", "Email", "Phone", "County", "Category"};
        Object[][] data = loadClientsData();
        
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.getTableHeader().setBackground(PRIMARY_DARK);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setGridColor(new Color(200, 200, 200));
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        bottomPanel.setBackground(new Color(240, 242, 245));
        
        // Refresh button
        JButton refreshButton = new JButton("🔄 Refresh");
        styleButton(refreshButton, INFO_COLOR);
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            sortCombo.setSelectedIndex(0);
            categoryCombo.setSelectedIndex(0);
            refreshTableData(model);
        });
        bottomPanel.add(refreshButton);
        
        // Clear Filters button
        JButton clearButton = new JButton("✖ Clear All");
        styleButton(clearButton, new Color(156, 39, 176));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            sortCombo.setSelectedIndex(0);
            categoryCombo.setSelectedIndex(0);
            refreshTableData(model);
        });
        bottomPanel.add(clearButton);
        
        // Export button
        JButton exportButton = new JButton("💾 Export");
        styleButton(exportButton, new Color(76, 175, 80));
        bottomPanel.add(exportButton);
        
        // Edit button
        JButton editButton = new JButton("✏️ Edit");
        styleButton(editButton, new Color(255, 193, 7)); // Orange/Yellow
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(mainPanel, "Please select a client to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            } else {
                int clientId = (Integer) table.getValueAt(selectedRow, 0);
                editClient(clientId, mainPanel);
            }
        });
        bottomPanel.add(editButton);
        
        // Delete button
        JButton deleteButton = new JButton("🗑️ Delete");
        styleButton(deleteButton, new Color(244, 67, 54)); // Red
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(mainPanel, "Please select a client to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            } else {
                int clientId = (Integer) table.getValueAt(selectedRow, 0);
                String clientName = (String) table.getValueAt(selectedRow, 1);
                deleteClient(clientId, clientName, model);
            }
        });
        bottomPanel.add(deleteButton);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add listeners for search, sort, and filter
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(table, model, searchField.getText(), (String)sortCombo.getSelectedItem(), (String)categoryCombo.getSelectedItem()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(table, model, searchField.getText(), (String)sortCombo.getSelectedItem(), (String)categoryCombo.getSelectedItem()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(table, model, searchField.getText(), (String)sortCombo.getSelectedItem(), (String)categoryCombo.getSelectedItem()); }
        });
        
        sortCombo.addActionListener(e -> applyFilters(table, model, searchField.getText(), (String)sortCombo.getSelectedItem(), (String)categoryCombo.getSelectedItem()));
        
        categoryCombo.addActionListener(e -> applyFilters(table, model, searchField.getText(), (String)sortCombo.getSelectedItem(), (String)categoryCombo.getSelectedItem()));
        
        return mainPanel;
    }
    
    private Object[][] loadClientsData() {
        try {
            List<Client> clients = ClientService.getAllClients();
            if (clients == null || clients.isEmpty()) {
                return new Object[0][6];  // Return empty array if no clients
            }
            Object[][] data = new Object[clients.size()][6];
            
            for (int i = 0; i < clients.size(); i++) {
                Client client = clients.get(i);
                data[i][0] = client.getClientId();
                data[i][1] = client.getFullName();
                data[i][2] = client.getEmail();
                data[i][3] = client.getPhone() != null ? client.getPhone() : "";
                data[i][4] = client.getAddress() != null ? client.getAddress() : "";
                data[i][5] = client.getCategory() != null ? client.getCategory() : "";
            }
            
            return data;
        } catch (Exception e) {
            System.err.println("Error loading clients: " + e.getMessage());
            e.printStackTrace();
            return new Object[0][6];  // Return empty array on error
        }
    }
    
    private void refreshTableData(javax.swing.table.DefaultTableModel model) {
        Object[][] newData = loadClientsData();
        model.setRowCount(0);
        for (Object[] row : newData) {
            model.addRow(row);
        }
    }
    
    private void applyFilters(JTable table, javax.swing.table.DefaultTableModel model, String searchText, String sortBy, String category) {
        try {
            List<Client> allClients = ClientService.getAllClients();
            if (allClients == null) allClients = new java.util.ArrayList<>();
            
            // Filter by search text
            List<Client> filteredClients = new java.util.ArrayList<>();
            for (Client client : allClients) {
                if (searchText.isEmpty() || 
                    client.getFullName().toLowerCase().contains(searchText.toLowerCase()) ||
                    client.getEmail().toLowerCase().contains(searchText.toLowerCase()) ||
                    client.getPhone().toLowerCase().contains(searchText.toLowerCase()) ||
                    client.getAddress().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredClients.add(client);
                }
            }
            
            // Filter by category
            if (!category.equals("All")) {
                List<Client> categoryFiltered = new java.util.ArrayList<>();
                for (Client client : filteredClients) {
                    if (client.getCategory() != null && client.getCategory().equals(category)) {
                        categoryFiltered.add(client);
                    }
                }
                filteredClients = categoryFiltered;
            }
            
            // Sort
            if (sortBy.equals("Name (A-Z)")) {
                filteredClients.sort((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()));
            } else if (sortBy.equals("Name (Z-A)")) {
                filteredClients.sort((a, b) -> b.getFullName().compareToIgnoreCase(a.getFullName()));
            } else if (sortBy.equals("Email (A-Z)")) {
                filteredClients.sort((a, b) -> a.getEmail().compareToIgnoreCase(b.getEmail()));
            } else if (sortBy.equals("ID (Ascending)")) {
                filteredClients.sort((a, b) -> Integer.compare(a.getClientId(), b.getClientId()));
            } else if (sortBy.equals("ID (Descending)")) {
                filteredClients.sort((a, b) -> Integer.compare(b.getClientId(), a.getClientId()));
            } else if (sortBy.equals("County")) {
                filteredClients.sort((a, b) -> a.getAddress().compareToIgnoreCase(b.getAddress()));
            }
            
            // Update table
            model.setRowCount(0);
            for (Client client : filteredClients) {
                Object[] row = {
                    client.getClientId(),
                    client.getFullName(),
                    client.getEmail(),
                    client.getPhone() != null ? client.getPhone() : "",
                    client.getAddress() != null ? client.getAddress() : "",
                    client.getCategory() != null ? client.getCategory() : ""
                };
                model.addRow(row);
            }
        } catch (Exception e) {
            System.err.println("Error applying filters: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void editClient(int clientId, JPanel parentPanel) {
        Client client = ClientService.getClientById(clientId);
        if (client == null) {
            JOptionPane.showMessageDialog(parentPanel, "Client not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create edit dialog
        JDialog editDialog = new JDialog((java.awt.Frame) null, "Edit Client - " + client.getFullName(), true);
        editDialog.setSize(500, 400);
        editDialog.setLocationRelativeTo(parentPanel);
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBackground(CARD_BACKGROUND);
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Edit Client Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_DARK);
        contentPane.add(titleLabel, gbc);
        
        // Full Name
        gbc.gridy++;
        gbc.gridwidth = 1;
        contentPane.add(new JLabel("Full Name:"), gbc);
        
        JTextField nameField = new JTextField(client.getFullName());
        styleTextField(nameField);
        gbc.gridx = 1;
        contentPane.add(nameField, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(new JLabel("Email:"), gbc);
        
        JTextField emailField = new JTextField(client.getEmail());
        styleTextField(emailField);
        gbc.gridx = 1;
        contentPane.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(new JLabel("Phone:"), gbc);
        
        JTextField phoneField = new JTextField(client.getPhone() != null ? client.getPhone() : "");
        styleTextField(phoneField);
        gbc.gridx = 1;
        contentPane.add(phoneField, gbc);
        
        // Address
        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(new JLabel("County:"), gbc);
        
        JTextField addressField = new JTextField(client.getAddress() != null ? client.getAddress() : "");
        styleTextField(addressField);
        gbc.gridx = 1;
        contentPane.add(addressField, gbc);
        
        // Category
        gbc.gridx = 0;
        gbc.gridy++;
        contentPane.add(new JLabel("Category:"), gbc);
        
        String[] categories = {"Borehole", "Pump Installation", "Plumbing", "Tank Installation"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setSelectedItem(client.getCategory() != null ? client.getCategory() : "Borehole");
        gbc.gridx = 1;
        contentPane.add(categoryCombo, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton saveButton = new JButton("💾 Save Changes");
        styleButton(saveButton, new Color(76, 175, 80));
        saveButton.addActionListener(e -> {
            try {
                ClientService.updateClient(clientId, nameField.getText(), phoneField.getText(), addressField.getText(), addressField.getText());
                
                JOptionPane.showMessageDialog(editDialog, "Client updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                editDialog.dispose();
                // Refresh admin panel
                javax.swing.SwingUtilities.getWindowAncestor(parentPanel).validate();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(editDialog, "Error updating client: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);
        
        JButton cancelButton = new JButton("✖ Cancel");
        styleButton(cancelButton, new Color(244, 67, 54));
        cancelButton.addActionListener(e -> editDialog.dispose());
        buttonPanel.add(cancelButton);
        
        contentPane.add(buttonPanel, gbc);
        
        editDialog.add(contentPane);
        editDialog.setVisible(true);
    }
    
    private void deleteClient(int clientId, String clientName, javax.swing.table.DefaultTableModel model) {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete " + clientName + "?\n\nThis action cannot be undone!",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                ClientService.deleteClient(clientId);
                JOptionPane.showMessageDialog(this, clientName + " has been deleted successfully.", "Delete Successful", JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh the table
                Object[][] newData = loadClientsData();
                model.setRowCount(0);
                for (Object[] row : newData) {
                    model.addRow(row);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting client: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // ============ EVENT HANDLERS ============
    private void handleLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter email and password", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Client client = ClientService.authenticateClient(email, password);
        
        if (client != null) {
            // Check if admin
            if (email.equalsIgnoreCase("admin@uzima.com")) {
                cardLayout.show(contentPanel, "ADMIN");
            } else {
                // Show main menu
                showMainMenu(client);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid email or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleSignUp(String name, String email, String phone, String county, String category, String password, String confirmPassword) {
        // Validation
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check email uniqueness
        Client existing = ClientService.getClientByEmail(email);
        if (existing != null) {
            JOptionPane.showMessageDialog(this, "Email already exists", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Register client
        if (ClientService.registerClient(name, email, phone, category, county, county, password)) {
            JOptionPane.showMessageDialog(this, "Account created successfully! Logging in...", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Auto-login
            Client newClient = ClientService.getClientByEmail(email);
            if (newClient != null) {
                showMainMenu(newClient);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create account", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showMainMenu(Client client) {
        MainMenuPanel mainMenu = new MainMenuPanel(client.getClientId());
        
        JFrame mainFrame = new JFrame("UZIMA - Main Menu");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1200, 800);
        mainFrame.setLocationRelativeTo(this);
        mainFrame.setContentPane(mainMenu);
        mainFrame.setVisible(true);
        
        // Hide login window
        setVisible(false);
    }
    
    // ============ UI HELPERS ============
    private JPanel createGradientPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gp = new GradientPaint(
                    0, 0, PRIMARY_DARK,
                    getWidth(), getHeight(), new Color(20, 60, 120)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }
    
    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void styleTextField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (field instanceof JTextField) {
            ((JTextField) field).setBackground(FIELD_BACKGROUND);
            ((JTextField) field).setCaretColor(PRIMARY_ACCENT);
            ((JTextField) field).setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        } else if (field instanceof JPasswordField) {
            ((JPasswordField) field).setBackground(FIELD_BACKGROUND);
            ((JPasswordField) field).setCaretColor(PRIMARY_ACCENT);
            ((JPasswordField) field).setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        }
    }
    
    private void showLogin() {
        cardLayout.show(contentPanel, "LOGIN");
    }
    
    private void showSignUp() {
        cardLayout.show(contentPanel, "SIGNUP");
    }
    
    // ============ APPLICATION ENTRY POINT ============
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}
