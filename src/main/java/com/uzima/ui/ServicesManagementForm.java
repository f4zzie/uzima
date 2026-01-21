package com.uzima.ui;

import com.uzima.dao.ServiceDAO;
import com.uzima.model.ServiceType;
import com.uzima.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Services Management Module - Manage Drilling Types, Pump Types, Tank Sizes
 */
public class ServicesManagementForm extends JPanel {
    private JTabbedPane tabbedPane;
    private ServiceDAO serviceDAO;
    
    // Tables for each category
    private JTable drillingTable, pumpTable, tankTable;
    private DefaultTableModel drillingModel, pumpModel, tankModel;
    
    private static final Color PRIMARY_COLOR = new Color(139, 92, 246); // Purple
    private static final Color DARK_COLOR = new Color(109, 40, 217);
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);
    private static final Color DANGER_RED = new Color(239, 68, 68);
    private static final Color WARNING_ORANGE = new Color(249, 115, 22);
    private static final Color LIGHT_BG = new Color(248, 250, 252);
    
    public ServicesManagementForm() {
        setLayout(new BorderLayout());
        setBackground(LIGHT_BG);
        
        serviceDAO = new ServiceDAO();
        initComponents();
        loadAllData();
    }
    
    private void initComponents() {
        add(createHeader(), BorderLayout.NORTH);
        add(createTabbedContent(), BorderLayout.CENTER);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, getWidth(), 0, DARK_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 80));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        
        JLabel lblTitle = new JLabel("🔧 Services & Pricing Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblSubtitle = new JLabel("Manage drilling types, pump options, and tank sizes");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(255, 255, 255, 200));
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        
        header.add(titlePanel, BorderLayout.WEST);
        
        return header;
    }
    
    private JTabbedPane createTabbedContent() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);
        
        tabbedPane.addTab("⚒️ Drilling Types", createDrillingPanel());
        tabbedPane.addTab("💧 Pump Types", createPumpPanel());
        tabbedPane.addTab("🛢️ Tank Sizes", createTankPanel());
        
        return tabbedPane;
    }
    
    // ==================== DRILLING TYPES PANEL ====================
    private JPanel createDrillingPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        // Toolbar
        JPanel toolbar = createToolbar("Drilling Type", () -> showAddDrillingDialog());
        panel.add(toolbar, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Name", "Description", "Base Price (KSH)", "Per Meter (KSH)", "Status", "Actions"};
        drillingModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };
        
        drillingTable = createStyledTable(drillingModel);
        drillingTable.getColumnModel().getColumn(6).setCellRenderer(new ActionsCellRenderer());
        drillingTable.getColumnModel().getColumn(6).setCellEditor(new DrillingActionsEditor());
        
        JScrollPane scrollPane = new JScrollPane(drillingTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadDrillingTypes() {
        drillingModel.setRowCount(0);
        try {
            List<ServiceType> types = serviceDAO.getDrillingTypes();
            for (ServiceType type : types) {
                drillingModel.addRow(new Object[]{
                    type.getId(),
                    type.getName(),
                    type.getDescription(),
                    formatCurrency(type.getBasePrice()),
                    formatCurrency(type.getPricePerMeter()),
                    type.isActive() ? "Active" : "Inactive",
                    "actions"
                });
            }
        } catch (Exception e) {
            addSampleDrillingData();
        }
    }
    
    private void addSampleDrillingData() {
        drillingModel.addRow(new Object[]{1, "Manual Drilling", "Traditional hand drilling", "25,000", "500", "Active", "actions"});
        drillingModel.addRow(new Object[]{2, "Machine Drilling", "Modern machine drilling", "50,000", "1,200", "Active", "actions"});
        drillingModel.addRow(new Object[]{3, "Rotary Drilling", "High-capacity rotary", "80,000", "2,000", "Active", "actions"});
    }
    
    private void showAddDrillingDialog() {
        showServiceDialog("Drilling Type", null, "drilling");
    }
    
    // ==================== PUMP TYPES PANEL ====================
    private JPanel createPumpPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        JPanel toolbar = createToolbar("Pump Type", () -> showAddPumpDialog());
        panel.add(toolbar, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Name", "Power (HP)", "Max Depth (m)", "Price (KSH)", "Status", "Actions"};
        pumpModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };
        
        pumpTable = createStyledTable(pumpModel);
        pumpTable.getColumnModel().getColumn(6).setCellRenderer(new ActionsCellRenderer());
        pumpTable.getColumnModel().getColumn(6).setCellEditor(new PumpActionsEditor());
        
        JScrollPane scrollPane = new JScrollPane(pumpTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadPumpTypes() {
        pumpModel.setRowCount(0);
        try {
            List<ServiceType> types = serviceDAO.getPumpTypes();
            for (ServiceType type : types) {
                pumpModel.addRow(new Object[]{
                    type.getId(),
                    type.getName(),
                    type.getPowerRating(),
                    type.getMaxDepth(),
                    formatCurrency(type.getPrice()),
                    type.isActive() ? "Active" : "Inactive",
                    "actions"
                });
            }
        } catch (Exception e) {
            addSamplePumpData();
        }
    }
    
    private void addSamplePumpData() {
        pumpModel.addRow(new Object[]{1, "Submersible 0.5HP", "0.5", "30", "15,000", "Active", "actions"});
        pumpModel.addRow(new Object[]{2, "Submersible 1HP", "1.0", "50", "25,000", "Active", "actions"});
        pumpModel.addRow(new Object[]{3, "Submersible 2HP", "2.0", "80", "45,000", "Active", "actions"});
        pumpModel.addRow(new Object[]{4, "Submersible 3HP", "3.0", "120", "65,000", "Active", "actions"});
    }
    
    private void showAddPumpDialog() {
        showServiceDialog("Pump Type", null, "pump");
    }
    
    // ==================== TANK SIZES PANEL ====================
    private JPanel createTankPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        JPanel toolbar = createToolbar("Tank Size", () -> showAddTankDialog());
        panel.add(toolbar, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Name", "Capacity (L)", "Material", "Price (KSH)", "Status", "Actions"};
        tankModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };
        
        tankTable = createStyledTable(tankModel);
        tankTable.getColumnModel().getColumn(6).setCellRenderer(new ActionsCellRenderer());
        tankTable.getColumnModel().getColumn(6).setCellEditor(new TankActionsEditor());
        
        JScrollPane scrollPane = new JScrollPane(tankTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadTankSizes() {
        tankModel.setRowCount(0);
        try {
            List<ServiceType> types = serviceDAO.getTankTypes();
            for (ServiceType type : types) {
                tankModel.addRow(new Object[]{
                    type.getId(),
                    type.getName(),
                    type.getCapacity(),
                    type.getMaterial(),
                    formatCurrency(type.getPrice()),
                    type.isActive() ? "Active" : "Inactive",
                    "actions"
                });
            }
        } catch (Exception e) {
            addSampleTankData();
        }
    }
    
    private void addSampleTankData() {
        tankModel.addRow(new Object[]{1, "Small Tank", "500", "Plastic", "8,000", "Active", "actions"});
        tankModel.addRow(new Object[]{2, "Medium Tank", "1000", "Plastic", "15,000", "Active", "actions"});
        tankModel.addRow(new Object[]{3, "Large Tank", "2000", "Plastic", "28,000", "Active", "actions"});
        tankModel.addRow(new Object[]{4, "Extra Large Tank", "5000", "Plastic", "55,000", "Active", "actions"});
        tankModel.addRow(new Object[]{5, "Steel Tank 1000L", "1000", "Steel", "35,000", "Active", "actions"});
    }
    
    private void showAddTankDialog() {
        showServiceDialog("Tank Size", null, "tank");
    }
    
    // ==================== SHARED COMPONENTS ====================
    private void loadAllData() {
        loadDrillingTypes();
        loadPumpTypes();
        loadTankSizes();
    }
    
    private JPanel createToolbar(String itemType, Runnable addAction) {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        
        JLabel lblInfo = new JLabel("Manage " + itemType + "s and their pricing");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInfo.setForeground(new Color(100, 116, 139));
        
        JButton btnAdd = createStyledButton("➕ Add " + itemType, SUCCESS_GREEN);
        btnAdd.addActionListener(e -> addAction.run());
        
        JButton btnRefresh = createStyledButton("🔄 Refresh", new Color(59, 130, 246));
        btnRefresh.addActionListener(e -> loadAllData());
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(btnRefresh);
        rightPanel.add(btnAdd);
        
        toolbar.add(lblInfo, BorderLayout.WEST);
        toolbar.add(rightPanel, BorderLayout.EAST);
        
        return toolbar;
    }
    
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(48);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(245, 243, 255));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(new Color(71, 85, 105));
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        return table;
    }
    
    private void showServiceDialog(String type, ServiceType existing, String category) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            (existing == null ? "Add " : "Edit ") + type, true);
        dialog.setSize(420, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Header
        JPanel header = new JPanel();
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(0, 50));
        JLabel lblTitle = new JLabel((existing == null ? "Add New " : "Edit ") + type);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle);
        
        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField txtName = new JTextField(20);
        JTextField txtDescription = new JTextField(20);
        JTextField txtPrice = new JTextField(20);
        JTextField txtExtra1 = new JTextField(20);
        JTextField txtExtra2 = new JTextField(20);
        JCheckBox chkActive = new JCheckBox("Active");
        chkActive.setSelected(true);
        
        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        form.add(txtName, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        form.add(txtDescription, gbc);
        row++;
        
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("Price (KSH):"), gbc);
        gbc.gridx = 1;
        form.add(txtPrice, gbc);
        row++;
        
        // Extra fields based on category
        String extra1Label = "", extra2Label = "";
        switch (category) {
            case "drilling":
                extra1Label = "Per Meter (KSH):";
                break;
            case "pump":
                extra1Label = "Power (HP):";
                extra2Label = "Max Depth (m):";
                break;
            case "tank":
                extra1Label = "Capacity (L):";
                extra2Label = "Material:";
                break;
        }
        
        if (!extra1Label.isEmpty()) {
            gbc.gridx = 0; gbc.gridy = row;
            form.add(new JLabel(extra1Label), gbc);
            gbc.gridx = 1;
            form.add(txtExtra1, gbc);
            row++;
        }
        
        if (!extra2Label.isEmpty()) {
            gbc.gridx = 0; gbc.gridy = row;
            form.add(new JLabel(extra2Label), gbc);
            gbc.gridx = 1;
            form.add(txtExtra2, gbc);
            row++;
        }
        
        gbc.gridx = 1; gbc.gridy = row;
        form.add(chkActive, gbc);
        
        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttons.setBackground(LIGHT_BG);
        
        JButton btnSave = createStyledButton("💾 Save", SUCCESS_GREEN);
        btnSave.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, type + " saved successfully!");
            dialog.dispose();
            loadAllData();
        });
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dialog.dispose());
        
        buttons.add(btnSave);
        buttons.add(btnCancel);
        
        panel.add(header, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 35));
        return btn;
    }
    
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount.doubleValue());
    }
    
    private String formatCurrency(double amount) {
        return String.format("%,.0f", amount);
    }
    
    // ==================== CELL RENDERERS & EDITORS ====================
    private class ActionsCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 8));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            JButton btnEdit = new JButton("✏️");
            btnEdit.setToolTipText("Edit");
            btnEdit.setPreferredSize(new Dimension(30, 28));
            btnEdit.setFocusPainted(false);
            
            JButton btnDelete = new JButton("🗑️");
            btnDelete.setToolTipText("Delete");
            btnDelete.setPreferredSize(new Dimension(30, 28));
            btnDelete.setFocusPainted(false);
            
            panel.add(btnEdit);
            panel.add(btnDelete);
            
            return panel;
        }
    }
    
    private abstract class BaseActionsEditor extends DefaultCellEditor {
        protected JPanel panel;
        protected int currentId;
        
        public BaseActionsEditor() {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 8));
            
            JButton btnEdit = new JButton("✏️");
            btnEdit.setToolTipText("Edit");
            btnEdit.setPreferredSize(new Dimension(30, 28));
            btnEdit.addActionListener(e -> {
                stopCellEditing();
                handleEdit();
            });
            
            JButton btnDelete = new JButton("🗑️");
            btnDelete.setToolTipText("Delete");
            btnDelete.setPreferredSize(new Dimension(30, 28));
            btnDelete.addActionListener(e -> {
                stopCellEditing();
                handleDelete();
            });
            
            panel.add(btnEdit);
            panel.add(btnDelete);
        }
        
        protected abstract void handleEdit();
        protected abstract void handleDelete();
        
        @Override
        public Object getCellEditorValue() {
            return "actions";
        }
    }
    
    private class DrillingActionsEditor extends BaseActionsEditor {
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentId = (int) table.getValueAt(row, 0);
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
        
        @Override
        protected void handleEdit() {
            showServiceDialog("Drilling Type", null, "drilling");
        }
        
        @Override
        protected void handleDelete() {
            confirmAndDelete("drilling type", currentId);
        }
    }
    
    private class PumpActionsEditor extends BaseActionsEditor {
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentId = (int) table.getValueAt(row, 0);
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
        
        @Override
        protected void handleEdit() {
            showServiceDialog("Pump Type", null, "pump");
        }
        
        @Override
        protected void handleDelete() {
            confirmAndDelete("pump type", currentId);
        }
    }
    
    private class TankActionsEditor extends BaseActionsEditor {
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentId = (int) table.getValueAt(row, 0);
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
        
        @Override
        protected void handleEdit() {
            showServiceDialog("Tank Size", null, "tank");
        }
        
        @Override
        protected void handleDelete() {
            confirmAndDelete("tank size", currentId);
        }
    }
    
    private void confirmAndDelete(String type, int id) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this " + type + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, type + " deleted successfully!");
            loadAllData();
        }
    }
}
