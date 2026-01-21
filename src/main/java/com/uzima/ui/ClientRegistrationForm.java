package com.uzima.ui;

import com.uzima.dao.ClientDAO;
import com.uzima.model.Client;
import com.uzima.util.UIHelper;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

/**
 * Client Registration Form for Staff
 */
public class ClientRegistrationForm extends JDialog {
    private JTextField txtFullName, txtPhone1, txtPhone2, txtEmail;
    private JTextArea txtAddress, txtBoreholeLocation;
    private JTextField txtCounty, txtGPS, txtIdNumber;
    private JComboBox<String> cmbCategory;
    private JDateChooser dateRegistration;
    private JButton btnRegister, btnCancel;
    private ClientDAO clientDAO;
    
    public ClientRegistrationForm(JFrame parent) {
        super(parent, "Client Registration", true);
        setSize(700, 750);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        clientDAO = new ClientDAO();
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UIHelper.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(700, 80));
        
        JLabel lblTitle = new JLabel("CLIENT REGISTRATION");
        lblTitle.setFont(UIHelper.TITLE_FONT);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Full Name
        addFormField(formPanel, gbc, row++, "Full Name *:", txtFullName = new JTextField(25));
        
        // Phone Numbers
        addFormField(formPanel, gbc, row++, "Phone Number *:", txtPhone1 = new JTextField(25));
        addFormField(formPanel, gbc, row++, "Alt. Phone:", txtPhone2 = new JTextField(25));
        
        // Email
        addFormField(formPanel, gbc, row++, "Email:", txtEmail = new JTextField(25));
        
        // Physical Address
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lblAddress = new JLabel("Physical Address *:");
        lblAddress.setFont(UIHelper.LABEL_FONT);
        formPanel.add(lblAddress, gbc);
        
        gbc.gridx = 1;
        txtAddress = new JTextArea(3, 25);
        txtAddress.setFont(UIHelper.TEXT_FONT);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        txtAddress.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        formPanel.add(new JScrollPane(txtAddress), gbc);
        row++;
        
        // Client Category
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lblCategory = new JLabel("Client Category *:");
        lblCategory.setFont(UIHelper.LABEL_FONT);
        formPanel.add(lblCategory, gbc);
        
        gbc.gridx = 1;
        cmbCategory = new JComboBox<>(new String[]{"INDUSTRIAL", "COMMERCIAL", "DOMESTIC"});
        cmbCategory.setFont(UIHelper.TEXT_FONT);
        formPanel.add(cmbCategory, gbc);
        row++;
        
        // Borehole Location
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lblLocation = new JLabel("Borehole Location *:");
        lblLocation.setFont(UIHelper.LABEL_FONT);
        formPanel.add(lblLocation, gbc);
        
        gbc.gridx = 1;
        txtBoreholeLocation = new JTextArea(3, 25);
        txtBoreholeLocation.setFont(UIHelper.TEXT_FONT);
        txtBoreholeLocation.setLineWrap(true);
        txtBoreholeLocation.setWrapStyleWord(true);
        txtBoreholeLocation.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        formPanel.add(new JScrollPane(txtBoreholeLocation), gbc);
        row++;
        
        // County
        addFormField(formPanel, gbc, row++, "County:", txtCounty = new JTextField(25));
        
        // GPS Coordinates
        addFormField(formPanel, gbc, row++, "GPS Coordinates:", txtGPS = new JTextField(25));
        
        // ID Number
        addFormField(formPanel, gbc, row++, "ID/Passport Number:", txtIdNumber = new JTextField(25));
        
        // Registration Date
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lblDate = new JLabel("Registration Date *:");
        lblDate.setFont(UIHelper.LABEL_FONT);
        formPanel.add(lblDate, gbc);
        
        gbc.gridx = 1;
        dateRegistration = new JDateChooser();
        dateRegistration.setDate(new Date());
        dateRegistration.setFont(UIHelper.TEXT_FONT);
        formPanel.add(dateRegistration, gbc);
        
        add(new JScrollPane(formPanel), BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        
        btnRegister = new JButton("Register");
        UIHelper.styleSuccessButton(btnRegister);
        btnRegister.addActionListener(e -> handleRegistration());
        buttonPanel.add(btnRegister);
        
        btnCancel = new JButton("Cancel");
        UIHelper.styleDangerButton(btnCancel);
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnCancel);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIHelper.LABEL_FONT);
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        field.setFont(UIHelper.TEXT_FONT);
        field.setPreferredSize(new Dimension(250, 35));
        panel.add(field, gbc);
    }
    
    private void handleRegistration() {
        if (!validateForm()) {
            return;
        }
        
        try {
            String clientId = clientDAO.generateClientId((String) cmbCategory.getSelectedItem());
            
            Client client = new Client();
            client.setClientId(clientId);
            client.setFullName(txtFullName.getText().trim());
            client.setPhoneNumber(txtPhone1.getText().trim());
            client.setPhoneNumber2(txtPhone2.getText().trim());
            client.setEmail(txtEmail.getText().trim());
            client.setPhysicalAddress(txtAddress.getText().trim());
            client.setClientCategory((String) cmbCategory.getSelectedItem());
            client.setBoreholeLocation(txtBoreholeLocation.getText().trim());
            client.setCounty(txtCounty.getText().trim());
            client.setGpsCoordinates(txtGPS.getText().trim());
            client.setIdNumber(txtIdNumber.getText().trim());
            client.setRegistrationDate(dateRegistration.getDate());
            client.setActive(true);
            
            boolean success = clientDAO.addClient(client);
            
            if (success) {
                UIHelper.showSuccess(this, 
                    "Registration Successful!\n\n" +
                    "Client ID: " + clientId + "\n\n" +
                    "Please note this ID for future reference.");
                dispose();
            } else {
                UIHelper.showError(this, "Registration failed. Please try again.");
            }
        } catch (Exception e) {
            UIHelper.showError(this, "Error during registration: " + e.getMessage());
        }
    }
    
    private boolean validateForm() {
        if (txtFullName.getText().trim().isEmpty()) {
            UIHelper.showWarning(this, "Full Name is required");
            txtFullName.requestFocus();
            return false;
        }
        
        if (txtPhone1.getText().trim().isEmpty()) {
            UIHelper.showWarning(this, "Phone Number is required");
            txtPhone1.requestFocus();
            return false;
        }
        
        if (txtAddress.getText().trim().isEmpty()) {
            UIHelper.showWarning(this, "Physical Address is required");
            txtAddress.requestFocus();
            return false;
        }
        
        if (txtBoreholeLocation.getText().trim().isEmpty()) {
            UIHelper.showWarning(this, "Borehole Location is required");
            txtBoreholeLocation.requestFocus();
            return false;
        }
        
        if (dateRegistration.getDate() == null) {
            UIHelper.showWarning(this, "Registration Date is required");
            return false;
        }
        
        return true;
    }
}
