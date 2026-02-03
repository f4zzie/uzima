package com.uzima.ui;

import com.toedter.calendar.JDateChooser;
import com.uzima.dao.ClientDAO;
import com.uzima.model.Client;
import com.uzima.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;

/**
 * Client Management Form - Register, Edit, View, Delete clients
 */
public class ClientManagementForm extends JFrame {
    private ClientDAO clientDAO;
    private JTable tblClients;
    private DefaultTableModel tableModel;
    
    // Form fields
    private JTextField txtClientId;
    private JTextField txtFullName;
    private JTextArea txtAddress;
    private JTextField txtPhone1;
    private JTextField txtPhone2;
    private JTextField txtEmail;
    private JComboBox<String> cmbCategory;
    private JTextArea txtBoreholeLocation;
    private JTextField txtCounty;
    private JTextField txtGPS;
    private JTextField txtIdNumber;
    private JDateChooser dateRegistration;
    private JTextField txtSearch;
    private JComboBox<String> cmbFilterCategory;
    
    // Buttons
    private JButton btnNew, btnSave, btnUpdate, btnDelete, btnClear, btnClose;
    
    private boolean editMode = false;
    
    public ClientManagementForm() {
        clientDAO = new ClientDAO();
        initComponents();
        loadClients();
    }
    
    private void initComponents() {
        setTitle("Client Management");
        setSize(1400, 800);
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
        
        JLabel lblTitle = new JLabel("CLIENT MANAGEMENT");
        lblTitle.setFont(UIHelper.TITLE_FONT);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        
        // Content Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.4);
        
        // Left Panel - Form
        JPanel formPanel = createFormPanel();
        
        // Right Panel - Table
        JPanel tablePanel = createTablePanel();
        
        splitPane.setLeftComponent(formPanel);
        splitPane.setRightComponent(tablePanel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = UIHelper.createTitledPanel("Client Information");
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            panel.getBorder(),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Form fields panel
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Client ID
        addFormField(fieldsPanel, gbc, row++, "Client ID:", 
                    txtClientId = new JTextField());
        txtClientId.setEditable(false);
        txtClientId.setBackground(Color.LIGHT_GRAY);
        
        // Full Name
        addFormField(fieldsPanel, gbc, row++, "Full Name: *", 
                    txtFullName = new JTextField());
        
        // Physical Address
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        fieldsPanel.add(UIHelper.createFormLabel("Physical Address: *"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtAddress = new JTextArea(3, 20);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        JScrollPane scrollAddress = new JScrollPane(txtAddress);
        fieldsPanel.add(scrollAddress, gbc);
        row++;
        
        // Phone 1
        addFormField(fieldsPanel, gbc, row++, "Phone Number: *", 
                    txtPhone1 = new JTextField());
        
        // Phone 2
        addFormField(fieldsPanel, gbc, row++, "Phone Number 2:", 
                    txtPhone2 = new JTextField());
        
        // Email
        addFormField(fieldsPanel, gbc, row++, "Email:", 
                    txtEmail = new JTextField());
        
        // Category
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        fieldsPanel.add(UIHelper.createFormLabel("Client Category: *"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        cmbCategory = new JComboBox<>(new String[]{"INDUSTRIAL", "COMMERCIAL", "DOMESTIC"});
        // Add listener to regenerate client ID when category changes (only in NEW mode)
        cmbCategory.addActionListener(e -> {
            if (txtClientId.getText().isEmpty() || txtClientId.getText().startsWith("uzm")) {
                String category = (String) cmbCategory.getSelectedItem();
                String newId = clientDAO.generateClientId(category);
                txtClientId.setText(newId);
            }
        });
        fieldsPanel.add(cmbCategory, gbc);
        row++;
        
        // Borehole Location
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        fieldsPanel.add(UIHelper.createFormLabel("Borehole Location: *"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtBoreholeLocation = new JTextArea(2, 20);
        txtBoreholeLocation.setLineWrap(true);
        txtBoreholeLocation.setWrapStyleWord(true);
        JScrollPane scrollLocation = new JScrollPane(txtBoreholeLocation);
        fieldsPanel.add(scrollLocation, gbc);
        row++;
        
        // County
        addFormField(fieldsPanel, gbc, row++, "County:", 
                    txtCounty = new JTextField());
        
        // GPS Coordinates
        addFormField(fieldsPanel, gbc, row++, "GPS Coordinates:", 
                    txtGPS = new JTextField());
        
        // ID Number
        addFormField(fieldsPanel, gbc, row++, "ID Number:", 
                    txtIdNumber = new JTextField());
        
        // Registration Date
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        fieldsPanel.add(UIHelper.createFormLabel("Registration Date: *"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        dateRegistration = new JDateChooser();
        dateRegistration.setDateFormatString("dd/MM/yyyy");
        dateRegistration.setDate(new Date());
        fieldsPanel.add(dateRegistration, gbc);
        
        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        btnNew = new JButton("NEW");
        btnSave = new JButton("SAVE");
        btnUpdate = new JButton("UPDATE");
        btnDelete = new JButton("DELETE");
        btnClear = new JButton("CLEAR");
        
        Dimension btnSize = new Dimension(100, 35);
        btnNew.setPreferredSize(btnSize);
        btnSave.setPreferredSize(btnSize);
        btnUpdate.setPreferredSize(btnSize);
        btnDelete.setPreferredSize(btnSize);
        btnClear.setPreferredSize(btnSize);
        
        UIHelper.stylePrimaryButton(btnNew);
        UIHelper.styleSuccessButton(btnSave);
        UIHelper.stylePrimaryButton(btnUpdate);
        UIHelper.styleDangerButton(btnDelete);
        UIHelper.styleSecondaryButton(btnClear);
        
        btnNew.addActionListener(e -> handleNew());
        btnSave.addActionListener(e -> handleSave());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());
        
        buttonPanel.add(btnNew);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        
        panel.add(new JScrollPane(fieldsPanel), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, 
                             String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        panel.add(UIHelper.createFormLabel(label), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }
    
    private JPanel createTablePanel() {
        JPanel panel = UIHelper.createTitledPanel("Registered Clients");
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            panel.getBorder(),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Search & Filter Panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search section
        JPanel searchInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchInputPanel.setBackground(Color.WHITE);
        
        searchInputPanel.add(new JLabel("⊙ Search:"));
        txtSearch = new JTextField(25);
        txtSearch.setToolTipText("Search by name, phone, email, ID number, or client ID");
        // Add real-time search
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                handleSearch();
            }
        });
        searchInputPanel.add(txtSearch);
        
        searchInputPanel.add(new JLabel("Category:"));
        cmbFilterCategory = new JComboBox<>(new String[]{"ALL", "INDUSTRIAL", "COMMERCIAL", "DOMESTIC"});
        cmbFilterCategory.setPreferredSize(new Dimension(140, 30));
        cmbFilterCategory.addActionListener(e -> handleSearch());
        searchInputPanel.add(cmbFilterCategory);
        
        JButton btnClearFilter = new JButton("CLEAR");
        UIHelper.styleSecondaryButton(btnClearFilter);
        btnClearFilter.setPreferredSize(new Dimension(90, 30));
        btnClearFilter.addActionListener(e -> {
            txtSearch.setText("");
            cmbFilterCategory.setSelectedIndex(0);
            loadClients();
        });
        searchInputPanel.add(btnClearFilter);
        
        JButton btnRefresh = new JButton("REFRESH");
        UIHelper.stylePrimaryButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(100, 30));
        btnRefresh.addActionListener(e -> loadClients());
        searchInputPanel.add(btnRefresh);
        
        searchPanel.add(searchInputPanel, BorderLayout.CENTER);
        
        // Table
        String[] columns = {"Client ID", "Full Name", "Phone 1", "Phone 2", "Email", 
                           "Category", "County", "ID Number", "GPS", "Reg. Date"};
        tableModel = UIHelper.createTableModel(columns, 0);
        tblClients = new JTable(tableModel);
        UIHelper.styleTable(tblClients);
        
        // Set column widths for better visibility
        tblClients.getColumnModel().getColumn(0).setPreferredWidth(80);  // Client ID
        tblClients.getColumnModel().getColumn(1).setPreferredWidth(150); // Full Name
        tblClients.getColumnModel().getColumn(2).setPreferredWidth(100); // Phone 1
        tblClients.getColumnModel().getColumn(3).setPreferredWidth(100); // Phone 2
        tblClients.getColumnModel().getColumn(4).setPreferredWidth(150); // Email
        tblClients.getColumnModel().getColumn(5).setPreferredWidth(100); // Category
        tblClients.getColumnModel().getColumn(6).setPreferredWidth(100); // County
        tblClients.getColumnModel().getColumn(7).setPreferredWidth(100); // ID Number
        tblClients.getColumnModel().getColumn(8).setPreferredWidth(120); // GPS
        tblClients.getColumnModel().getColumn(9).setPreferredWidth(100); // Reg. Date
        
        // Enable horizontal scrolling
        tblClients.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        tblClients.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblClients.getSelectedRow() != -1) {
                loadClientToForm();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tblClients);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void handleNew() {
        clearForm();
        txtFullName.requestFocus();
        
        // Generate new client ID
        String category = (String) cmbCategory.getSelectedItem();
        String newId = clientDAO.generateClientId(category);
        txtClientId.setText(newId);
        
        editMode = false;
        btnSave.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }
    
    private void handleSave() {
        if (!validateForm()) {
            return;
        }
        
        Client client = new Client();
        client.setClientId(txtClientId.getText().trim());
        client.setFullName(txtFullName.getText().trim());
        client.setPhysicalAddress(txtAddress.getText().trim());
        client.setPhoneNumber(txtPhone1.getText().trim());
        client.setPhoneNumber2(txtPhone2.getText().trim());
        client.setEmail(txtEmail.getText().trim());
        client.setClientCategory((String) cmbCategory.getSelectedItem());
        client.setBoreholeLocation(txtBoreholeLocation.getText().trim());
        client.setCounty(txtCounty.getText().trim());
        client.setGpsCoordinates(txtGPS.getText().trim());
        client.setIdNumber(txtIdNumber.getText().trim());
        client.setRegistrationDate(dateRegistration.getDate());
        
        if (clientDAO.addClient(client)) {
            UIHelper.showSuccess(this, "Client registered successfully!");
            clearForm();
            loadClients();
        } else {
            UIHelper.showError(this, "Failed to register client!");
        }
    }
    
    private void handleUpdate() {
        if (!validateForm()) {
            return;
        }
        
        Client client = new Client();
        client.setClientId(txtClientId.getText().trim());
        client.setFullName(txtFullName.getText().trim());
        client.setPhysicalAddress(txtAddress.getText().trim());
        client.setPhoneNumber(txtPhone1.getText().trim());
        client.setPhoneNumber2(txtPhone2.getText().trim());
        client.setEmail(txtEmail.getText().trim());
        client.setClientCategory((String) cmbCategory.getSelectedItem());
        client.setBoreholeLocation(txtBoreholeLocation.getText().trim());
        client.setCounty(txtCounty.getText().trim());
        client.setGpsCoordinates(txtGPS.getText().trim());
        client.setIdNumber(txtIdNumber.getText().trim());
        
        if (clientDAO.updateClient(client)) {
            UIHelper.showSuccess(this, "Client updated successfully!");
            clearForm();
            loadClients();
        } else {
            UIHelper.showError(this, "Failed to update client!");
        }
    }
    
    private void handleDelete() {
        if (!editMode) {
            UIHelper.showWarning(this, "Please select a client to delete!");
            return;
        }
        
        if (UIHelper.showConfirm(this, "Are you sure you want to delete this client?\n" +
                "This action cannot be undone!")) {
            
            if (clientDAO.deleteClient(txtClientId.getText().trim())) {
                UIHelper.showSuccess(this, "Client deleted successfully!");
                clearForm();
                loadClients();
            } else {
                UIHelper.showError(this, "Failed to delete client!");
            }
        }
    }
    
    private void handleSearch() {
        String searchTerm = txtSearch.getText().trim();
        String category = (String) cmbFilterCategory.getSelectedItem();
        
        // Get all clients first
        List<Client> clients;
        
        if (searchTerm.isEmpty()) {
            clients = clientDAO.getAllClients();
        } else {
            clients = clientDAO.searchClients(searchTerm);
        }
        
        // Apply category filter if not "ALL"
        if (category != null && !category.equals("ALL")) {
            clients = clients.stream()
                .filter(c -> c.getClientCategory().equals(category))
                .collect(java.util.stream.Collectors.toList());
        }
        
        displayClients(clients);
    }
    
    private void loadClients() {
        List<Client> clients = clientDAO.getAllClients();
        displayClients(clients);
    }
    
    private void displayClients(List<Client> clients) {
        tableModel.setRowCount(0);
        
        for (Client client : clients) {
            Object[] row = {
                client.getClientId(),
                client.getFullName(),
                client.getPhoneNumber(),
                client.getPhoneNumber2() != null ? client.getPhoneNumber2() : "",
                client.getEmail() != null ? client.getEmail() : "",
                client.getClientCategory(),
                client.getCounty() != null ? client.getCounty() : "",
                client.getIdNumber() != null ? client.getIdNumber() : "",
                client.getGpsCoordinates() != null ? client.getGpsCoordinates() : "",
                UIHelper.formatDate(client.getRegistrationDate())
            };
            tableModel.addRow(row);
        }
    }
    
    private void loadClientToForm() {
        int row = tblClients.getSelectedRow();
        
        if (row == -1) return;
        
        String clientId = (String) tableModel.getValueAt(row, 0);
        Client client = clientDAO.getClientById(clientId);
        
        if (client != null) {
            txtClientId.setText(client.getClientId());
            txtFullName.setText(client.getFullName());
            txtAddress.setText(client.getPhysicalAddress());
            txtPhone1.setText(client.getPhoneNumber());
            txtPhone2.setText(client.getPhoneNumber2());
            txtEmail.setText(client.getEmail());
            cmbCategory.setSelectedItem(client.getClientCategory());
            txtBoreholeLocation.setText(client.getBoreholeLocation());
            txtCounty.setText(client.getCounty());
            txtGPS.setText(client.getGpsCoordinates());
            txtIdNumber.setText(client.getIdNumber());
            dateRegistration.setDate(client.getRegistrationDate());
            
            editMode = true;
            btnSave.setEnabled(false);
            btnUpdate.setEnabled(true);
            btnDelete.setEnabled(true);
        }
    }
    
    private boolean validateForm() {
        if (!UIHelper.validateRequired(txtFullName, "Full Name", this)) return false;
        if (txtAddress.getText().trim().isEmpty()) {
            UIHelper.showError(this, "Physical Address is required!");
            txtAddress.requestFocus();
            return false;
        }
        if (!UIHelper.validateRequired(txtPhone1, "Phone Number", this)) return false;
        if (txtBoreholeLocation.getText().trim().isEmpty()) {
            UIHelper.showError(this, "Borehole Location is required!");
            txtBoreholeLocation.requestFocus();
            return false;
        }
        if (dateRegistration.getDate() == null) {
            UIHelper.showError(this, "Registration Date is required!");
            return false;
        }
        return true;
    }
    
    private void clearForm() {
        txtClientId.setText("");
        txtFullName.setText("");
        txtAddress.setText("");
        txtPhone1.setText("");
        txtPhone2.setText("");
        txtEmail.setText("");
        cmbCategory.setSelectedIndex(0);
        txtBoreholeLocation.setText("");
        txtCounty.setText("");
        txtGPS.setText("");
        txtIdNumber.setText("");
        dateRegistration.setDate(new Date());
        txtSearch.setText("");
        
        editMode = false;
        btnSave.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        
        tblClients.clearSelection();
    }
}
