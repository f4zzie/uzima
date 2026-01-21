package com.uzima.ui;

import com.uzima.dao.UserDAO;
import com.uzima.model.User;
import com.uzima.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * User Management Module - Full CRUD operations for system users
 */
public class UserManagementForm extends JPanel {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbRoleFilter;
    private UserDAO userDAO;
    
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color DARK_BLUE = new Color(30, 58, 138);
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);
    private static final Color DANGER_RED = new Color(239, 68, 68);
    private static final Color WARNING_ORANGE = new Color(249, 115, 22);
    private static final Color LIGHT_BG = new Color(248, 250, 252);
    
    public UserManagementForm() {
        setLayout(new BorderLayout());
        setBackground(LIGHT_BG);
        
        userDAO = new UserDAO();
        initComponents();
        loadUsers();
    }
    
    private void initComponents() {
        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
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
        header.setPreferredSize(new Dimension(0, 80));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        
        // Title
        JLabel lblTitle = new JLabel("👥 User Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        
        // Right side - Add User button
        JButton btnAdd = createStyledButton("➕ Add New User", SUCCESS_GREEN);
        btnAdd.addActionListener(e -> showAddUserDialog());
        
        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBackground(LIGHT_BG);
        content.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        // Toolbar
        content.add(createToolbar(), BorderLayout.NORTH);
        
        // Table
        content.add(createTablePanel(), BorderLayout.CENTER);
        
        return content;
    }
    
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(15, 0));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        
        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        
        JLabel lblSearch = new JLabel("🔍");
        lblSearch.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        
        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(200, 35));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtSearch.addActionListener(e -> filterUsers());
        
        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        
        // Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);
        
        JLabel lblFilter = new JLabel("Role:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        cmbRoleFilter = new JComboBox<>(new String[]{"All Roles", "ADMIN", "MANAGER", "CLERK"});
        cmbRoleFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbRoleFilter.setPreferredSize(new Dimension(120, 35));
        cmbRoleFilter.addActionListener(e -> filterUsers());
        
        filterPanel.add(lblFilter);
        filterPanel.add(cmbRoleFilter);
        
        // Actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);
        
        JButton btnRefresh = createSmallButton("🔄 Refresh", PRIMARY_BLUE);
        btnRefresh.addActionListener(e -> loadUsers());
        
        actionsPanel.add(btnRefresh);
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(searchPanel);
        leftPanel.add(filterPanel);
        
        toolbar.add(leftPanel, BorderLayout.WEST);
        toolbar.add(actionsPanel, BorderLayout.EAST);
        
        return toolbar;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        
        String[] columns = {"ID", "Username", "Full Name", "Email", "Role", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only actions column
            }
        };
        
        userTable = new JTable(tableModel);
        userTable.setRowHeight(50);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userTable.setShowGrid(false);
        userTable.setIntercellSpacing(new Dimension(0, 1));
        userTable.setSelectionBackground(new Color(239, 246, 255));
        userTable.setSelectionForeground(Color.BLACK);
        
        // Header styling
        JTableHeader header = userTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 45));
        
        // Column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        userTable.getColumnModel().getColumn(6).setPreferredWidth(150);
        
        // Custom renderers
        userTable.getColumnModel().getColumn(4).setCellRenderer(new RoleCellRenderer());
        userTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
        userTable.getColumnModel().getColumn(6).setCellRenderer(new ActionsCellRenderer());
        userTable.getColumnModel().getColumn(6).setCellEditor(new ActionsCellEditor());
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(scrollPane);
        return panel;
    }
    
    private void loadUsers() {
        tableModel.setRowCount(0);
        try {
            List<User> users = userDAO.getAllUsers();
            for (User user : users) {
                tableModel.addRow(new Object[]{
                    user.getUserId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole(),
                    user.isActive() ? "Active" : "Inactive",
                    "actions"
                });
            }
        } catch (Exception e) {
            // Handle case when table doesn't exist yet
            JOptionPane.showMessageDialog(this,
                "Could not load users. Please ensure the database is set up.",
                "Database Error",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void filterUsers() {
        String search = txtSearch.getText().toLowerCase().trim();
        String roleFilter = (String) cmbRoleFilter.getSelectedItem();
        
        tableModel.setRowCount(0);
        try {
            List<User> users = userDAO.getAllUsers();
            for (User user : users) {
                boolean matchesSearch = search.isEmpty() ||
                    user.getUsername().toLowerCase().contains(search) ||
                    user.getFullName().toLowerCase().contains(search) ||
                    user.getEmail().toLowerCase().contains(search);
                    
                boolean matchesRole = "All Roles".equals(roleFilter) ||
                    user.getRole().equals(roleFilter);
                
                if (matchesSearch && matchesRole) {
                    tableModel.addRow(new Object[]{
                        user.getUserId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole(),
                        user.isActive() ? "Active" : "Inactive",
                        "actions"
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New User", true);
        dialog.setSize(450, 480);
        dialog.setLocationRelativeTo(this);
        
        JPanel form = createUserForm(dialog, null);
        dialog.setContentPane(form);
        dialog.setVisible(true);
    }
    
    private void showEditUserDialog(int userId) {
        try {
            User user = userDAO.getUserById(userId);
            if (user != null) {
                JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit User", true);
                dialog.setSize(450, 480);
                dialog.setLocationRelativeTo(this);
                
                JPanel form = createUserForm(dialog, user);
                dialog.setContentPane(form);
                dialog.setVisible(true);
            }
        } catch (Exception e) {
            showError("Failed to load user: " + e.getMessage());
        }
    }
    
    private JPanel createUserForm(JDialog dialog, User existingUser) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Form header
        JPanel header = new JPanel();
        header.setBackground(PRIMARY_BLUE);
        header.setPreferredSize(new Dimension(0, 50));
        JLabel lblTitle = new JLabel(existingUser == null ? "Add New User" : "Edit User");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle);
        
        // Form fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField txtUsername = new JTextField(20);
        JTextField txtFullName = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JPasswordField txtPassword = new JPasswordField(20);
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"CLERK", "MANAGER", "ADMIN"});
        JCheckBox chkActive = new JCheckBox("Active");
        chkActive.setSelected(true);
        
        if (existingUser != null) {
            txtUsername.setText(existingUser.getUsername());
            txtFullName.setText(existingUser.getFullName());
            txtEmail.setText(existingUser.getEmail());
            cmbRole.setSelectedItem(existingUser.getRole());
            chkActive.setSelected(existingUser.isActive());
        }
        
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        form.add(txtUsername, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        form.add(txtFullName, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        form.add(txtEmail, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel(existingUser == null ? "Password:" : "New Password:"), gbc);
        gbc.gridx = 1;
        form.add(txtPassword, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        form.add(cmbRole, gbc);
        
        row++;
        gbc.gridx = 1; gbc.gridy = row;
        form.add(chkActive, gbc);
        
        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttons.setBackground(LIGHT_BG);
        
        JButton btnSave = createStyledButton("💾 Save", SUCCESS_GREEN);
        btnSave.addActionListener(e -> {
            try {
                User user = existingUser != null ? existingUser : new User();
                user.setUsername(txtUsername.getText().trim());
                user.setFullName(txtFullName.getText().trim());
                user.setEmail(txtEmail.getText().trim());
                user.setRole((String) cmbRole.getSelectedItem());
                user.setActive(chkActive.isSelected());
                
                String password = new String(txtPassword.getPassword());
                if (!password.isEmpty()) {
                    user.setPassword(password);
                }
                
                boolean success;
                if (existingUser == null) {
                    success = userDAO.addUser(user);
                } else {
                    success = userDAO.updateUser(user);
                }
                
                if (success) {
                    dialog.dispose();
                    loadUsers();
                    JOptionPane.showMessageDialog(this, "User saved successfully!");
                } else {
                    showError("Failed to save user");
                }
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
        });
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dialog.dispose());
        
        buttons.add(btnSave);
        buttons.add(btnCancel);
        
        panel.add(header, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void deleteUser(int userId) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this user?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (userDAO.deleteUser(userId)) {
                    loadUsers();
                    JOptionPane.showMessageDialog(this, "User deleted successfully!");
                } else {
                    showError("Failed to delete user");
                }
            } catch (Exception e) {
                showError("Error: " + e.getMessage());
            }
        }
    }
    
    private void resetPassword(int userId) {
        String newPassword = JOptionPane.showInputDialog(this,
            "Enter new password for user:",
            "Reset Password",
            JOptionPane.QUESTION_MESSAGE);
            
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            try {
                if (userDAO.updatePassword(userId, newPassword)) {
                    JOptionPane.showMessageDialog(this, "Password reset successfully!");
                } else {
                    showError("Failed to reset password");
                }
            } catch (Exception e) {
                showError("Error: " + e.getMessage());
            }
        }
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 38));
        return btn;
    }
    
    private JButton createSmallButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    // Custom cell renderers
    private class RoleCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel(value.toString());
            label.setHorizontalAlignment(CENTER);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            
            Color bgColor;
            switch (value.toString()) {
                case "ADMIN": bgColor = new Color(220, 38, 38); break;
                case "MANAGER": bgColor = new Color(234, 88, 12); break;
                default: bgColor = new Color(37, 99, 235); break;
            }
            
            label.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            label.setForeground(bgColor);
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            return label;
        }
    }
    
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel(value.toString());
            label.setHorizontalAlignment(CENTER);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            
            if ("Active".equals(value.toString())) {
                label.setForeground(SUCCESS_GREEN);
            } else {
                label.setForeground(DANGER_RED);
            }
            
            label.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return label;
        }
    }
    
    private class ActionsCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            JButton btnEdit = new JButton("✏️");
            btnEdit.setToolTipText("Edit");
            btnEdit.setPreferredSize(new Dimension(32, 28));
            btnEdit.setFocusPainted(false);
            
            JButton btnKey = new JButton("🔑");
            btnKey.setToolTipText("Reset Password");
            btnKey.setPreferredSize(new Dimension(32, 28));
            btnKey.setFocusPainted(false);
            
            JButton btnDelete = new JButton("🗑️");
            btnDelete.setToolTipText("Delete");
            btnDelete.setPreferredSize(new Dimension(32, 28));
            btnDelete.setFocusPainted(false);
            
            panel.add(btnEdit);
            panel.add(btnKey);
            panel.add(btnDelete);
            
            return panel;
        }
    }
    
    private class ActionsCellEditor extends DefaultCellEditor {
        private JPanel panel;
        private int currentUserId;
        
        public ActionsCellEditor() {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            
            JButton btnEdit = new JButton("✏️");
            btnEdit.setToolTipText("Edit");
            btnEdit.setPreferredSize(new Dimension(32, 28));
            btnEdit.addActionListener(e -> {
                stopCellEditing();
                showEditUserDialog(currentUserId);
            });
            
            JButton btnKey = new JButton("🔑");
            btnKey.setToolTipText("Reset Password");
            btnKey.setPreferredSize(new Dimension(32, 28));
            btnKey.addActionListener(e -> {
                stopCellEditing();
                resetPassword(currentUserId);
            });
            
            JButton btnDelete = new JButton("🗑️");
            btnDelete.setToolTipText("Delete");
            btnDelete.setPreferredSize(new Dimension(32, 28));
            btnDelete.addActionListener(e -> {
                stopCellEditing();
                deleteUser(currentUserId);
            });
            
            panel.add(btnEdit);
            panel.add(btnKey);
            panel.add(btnDelete);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentUserId = (int) table.getValueAt(row, 0);
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "actions";
        }
    }
}
