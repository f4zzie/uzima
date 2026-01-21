package com.uzima.ui;

import com.uzima.dao.ClientDAO;
import com.uzima.model.Client;
import com.uzima.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

/**
 * Customer Self-Registration Form with Kenya Counties and proper alignment
 */
public class CustomerRegistrationForm extends JDialog {
    private JTextField txtFullName, txtPhone1, txtPhone2, txtEmail;
    private JTextArea txtAddress, txtBoreholeLocation;
    private JComboBox<String> cmbCounty;
    private JTextField txtGPS, txtIdNumber;
    private JComboBox<String> cmbCategory;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JButton btnRegister, btnCancel;
    private ClientDAO clientDAO;
    
    private static final Color PRIMARY_GREEN = new Color(56, 142, 60);
    private static final Color DARK_GREEN = new Color(27, 94, 32);
    
    // All 47 Counties of Kenya
    private static final String[] KENYA_COUNTIES = {
        "-- Select County --",
        "Baringo", "Bomet", "Bungoma", "Busia", "Elgeyo-Marakwet",
        "Embu", "Garissa", "Homa Bay", "Isiolo", "Kajiado",
        "Kakamega", "Kericho", "Kiambu", "Kilifi", "Kirinyaga",
        "Kisii", "Kisumu", "Kitui", "Kwale", "Laikipia",
        "Lamu", "Machakos", "Makueni", "Mandera", "Marsabit",
        "Meru", "Migori", "Mombasa", "Murang'a", "Nairobi",
        "Nakuru", "Nandi", "Narok", "Nyamira", "Nyandarua",
        "Nyeri", "Samburu", "Siaya", "Taita-Taveta", "Tana River",
        "Tharaka-Nithi", "Trans-Nzoia", "Turkana", "Uasin Gishu", 
        "Vihiga", "Wajir", "West Pokot"
    };
    
    public CustomerRegistrationForm(JFrame parent) {
        super(parent, "Customer Registration", true);
        setSize(550, 750);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        clientDAO = new ClientDAO();
        initComponents();
    }
    
    private void initComponents() {
        // Main panel with white background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = createHeader();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Form content
        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_GREEN, getWidth(), 0, DARK_GREEN);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(550, 70));
        header.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 15));
        
        JLabel lblIcon = new JLabel("📝");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        
        JLabel lblTitle = new JLabel("CUSTOMER REGISTRATION");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        
        header.add(lblIcon);
        header.add(lblTitle);
        
        return header;
    }
    
    private JPanel createFormPanel() {
        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setLayout(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Full Name
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        form.add(createLabel("Full Name *"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtFullName = createTextField();
        form.add(txtFullName, gbc);
        row++;
        
        // Phone Numbers
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        form.add(createLabel("Phone *"), gbc);
        gbc.gridx = 1;
        txtPhone1 = createTextField();
        form.add(txtPhone1, gbc);
        gbc.gridx = 2;
        txtPhone2 = createTextField();
        txtPhone2.setToolTipText("Alternative Phone (Optional)");
        form.add(txtPhone2, gbc);
        row++;
        
        // Email
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        form.add(createLabel("Email"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtEmail = createTextField();
        form.add(txtEmail, gbc);
        row++;
        
        // Physical Address
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(createLabel("Address *"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        txtAddress = new JTextArea(3, 20);
        txtAddress.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        txtAddress.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        form.add(new JScrollPane(txtAddress), gbc);
        row++;
        
        // County (Dropdown)
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(createLabel("County *"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbCounty = new JComboBox<>(KENYA_COUNTIES);
        cmbCounty.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cmbCounty.setBackground(Color.WHITE);
        cmbCounty.setPreferredSize(new Dimension(200, 40));
        form.add(cmbCounty, gbc);
        row++;
        
        // Category
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        form.add(createLabel("Category *"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        cmbCategory = new JComboBox<>(new String[]{"DOMESTIC", "COMMERCIAL", "INDUSTRIAL"});
        cmbCategory.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cmbCategory.setBackground(Color.WHITE);
        cmbCategory.setPreferredSize(new Dimension(200, 40));
        form.add(cmbCategory, gbc);
        row++;
        
        // Borehole Location
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(createLabel("Borehole Site *"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        txtBoreholeLocation = new JTextArea(2, 20);
        txtBoreholeLocation.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtBoreholeLocation.setLineWrap(true);
        txtBoreholeLocation.setWrapStyleWord(true);
        txtBoreholeLocation.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        form.add(new JScrollPane(txtBoreholeLocation), gbc);
        row++;
        
        // GPS Coordinates
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(createLabel("GPS Coords"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtGPS = createTextField();
        txtGPS.setToolTipText("e.g., -1.2921, 36.8219");
        form.add(txtGPS, gbc);
        row++;
        
        // ID Number
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        form.add(createLabel("ID/Passport"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtIdNumber = createTextField();
        form.add(txtIdNumber, gbc);
        row++;
        
        // Separator
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220, 220, 220));
        form.add(sep, gbc);
        row++;
        
        // Password Section Header
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        JLabel lblSecurity = new JLabel("🔐 Account Security");
        lblSecurity.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSecurity.setForeground(PRIMARY_GREEN);
        form.add(lblSecurity, gbc);
        row++;
        
        // Password
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        form.add(createLabel("Password *"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtPassword.setPreferredSize(new Dimension(200, 40));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_GREEN, 2),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        form.add(txtPassword, gbc);
        row++;
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        form.add(createLabel("Confirm *"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtConfirmPassword.setPreferredSize(new Dimension(200, 40));
        txtConfirmPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_GREEN, 2),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        form.add(txtConfirmPassword, gbc);
        
        return form;
    }
    
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(40, 40, 40));
        lbl.setPreferredSize(new Dimension(110, 32));
        return lbl;
    }
    
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.BOLD, 14));
        field.setPreferredSize(new Dimension(180, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        return field;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        
        btnRegister = new JButton("REGISTER");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setPreferredSize(new Dimension(150, 45));
        btnRegister.setBackground(PRIMARY_GREEN);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> handleRegistration());
        
        btnCancel = new JButton("CANCEL");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setPreferredSize(new Dimension(120, 45));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(Color.GRAY);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());
        
        panel.add(btnRegister);
        panel.add(btnCancel);
        
        return panel;
    }
    
    private void handleRegistration() {
        if (!validateForm()) {
            return;
        }
        
        try {
            String category = (String) cmbCategory.getSelectedItem();
            String clientId = clientDAO.generateClientId(category);
            
            Client client = new Client();
            client.setClientId(clientId);
            client.setFullName(txtFullName.getText().trim());
            client.setPhoneNumber(txtPhone1.getText().trim());
            client.setPhoneNumber2(txtPhone2.getText().trim());
            client.setEmail(txtEmail.getText().trim());
            client.setPhysicalAddress(txtAddress.getText().trim());
            client.setClientCategory(category);
            client.setBoreholeLocation(txtBoreholeLocation.getText().trim());
            
            String selectedCounty = (String) cmbCounty.getSelectedItem();
            client.setCounty(selectedCounty.equals("-- Select County --") ? "" : selectedCounty);
            
            client.setGpsCoordinates(txtGPS.getText().trim());
            client.setIdNumber(txtIdNumber.getText().trim());
            client.setRegistrationDate(new Date());
            client.setPassword(new String(txtPassword.getPassword()));
            client.setActive(true);
            
            boolean success = clientDAO.registerCustomer(client);
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "<html><div style='text-align: center; padding: 15px;'>" +
                    "<h2 style='color: #388E3C;'>✅ Registration Successful!</h2>" +
                    "<p>Your Customer ID is:</p>" +
                    "<h1 style='color: #1976D2;'>" + clientId + "</h1>" +
                    "<p style='color: #666;'>Please save this ID for login.</p>" +
                    "</div></html>",
                    "Success",
                    JOptionPane.PLAIN_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "<html><div style='padding: 10px;'>" +
                    "<p><b>Registration failed.</b></p>" +
                    "<p>Please ensure the database is set up correctly.</p>" +
                    "<p style='color: #666; font-size: 11px;'>Run: ALTER TABLE clients ADD password VARCHAR(255);</p>" +
                    "</div></html>",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(),
                "Registration Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateForm() {
        if (txtFullName.getText().trim().isEmpty()) {
            showError("Full Name is required");
            txtFullName.requestFocus();
            return false;
        }
        
        if (txtPhone1.getText().trim().isEmpty()) {
            showError("Phone Number is required");
            txtPhone1.requestFocus();
            return false;
        }
        
        if (txtAddress.getText().trim().isEmpty()) {
            showError("Physical Address is required");
            txtAddress.requestFocus();
            return false;
        }
        
        if (cmbCounty.getSelectedIndex() == 0) {
            showError("Please select a County");
            cmbCounty.requestFocus();
            return false;
        }
        
        if (txtBoreholeLocation.getText().trim().isEmpty()) {
            showError("Borehole Location is required");
            txtBoreholeLocation.requestFocus();
            return false;
        }
        
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        
        if (password.isEmpty()) {
            showError("Password is required");
            txtPassword.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            txtPassword.requestFocus();
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match!");
            txtConfirmPassword.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}
