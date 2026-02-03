package com.uzima.util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * UI Helper class for common Swing UI operations and styling
 */
public class UIHelper {
    
    // Modern Professional Color Scheme
    public static final Color PRIMARY_COLOR = new Color(25, 118, 210);      // Deep Blue
    public static final Color PRIMARY_DARK = new Color(13, 71, 161);       // Darker Blue
    public static final Color SECONDARY_COLOR = new Color(66, 165, 245);    // Light Blue
    public static final Color SUCCESS_COLOR = new Color(56, 142, 60);       // Green
    public static final Color WARNING_COLOR = new Color(251, 140, 0);       // Orange
    public static final Color DANGER_COLOR = new Color(229, 57, 53);        // Red
    public static final Color BACKGROUND_COLOR = new Color(250, 250, 250);  // Light Gray
    public static final Color PANEL_COLOR = Color.WHITE;
    public static final Color TEXT_COLOR = new Color(33, 33, 33);
    public static final Color HEADER_COLOR = new Color(21, 101, 192);       // Professional Blue
    public static final Color HOVER_COLOR = new Color(227, 242, 253);       // Light Blue Hover
    
    // Professional Fonts (Bold and clear for Kali Linux)
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    
    // Initialize font rendering for Kali Linux
    static {
        initializeFontRendering();
    }
    
    /**
     * Initialize high-quality font rendering for Kali Linux
     */
    private static void initializeFontRendering() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("swing.plaf.metal.controlFont", "Segoe UI-BOLD-14");
        System.setProperty("swing.plaf.metal.userFont", "Segoe UI-PLAIN-14");
        
        // Set rendering hints for better text quality
        System.setProperty("sun.java2d.xrender", "true");
        System.setProperty("sun.java2d.opengl", "true");
        
        // Enable LCD text rendering
        System.setProperty("awt.text.antialiasing", "LCD_HRGB");
    }
    
    /**
     * Get rendering hints for high quality text on Kali Linux
     */
    public static RenderingHints getHighQualityRenderingHints() {
        RenderingHints hints = new RenderingHints(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        return hints;
    }
    
    // Number formats
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    /**
     * Format currency value
     */
    public static String formatCurrency(double amount) {
        return "KSh " + CURRENCY_FORMAT.format(amount);
    }
    
    /**
     * Format date
     */
    public static String formatDate(Date date) {
        return date != null ? DATE_FORMAT.format(date) : "";
    }
    
    /**
     * Format datetime
     */
    public static String formatDateTime(Date date) {
        return date != null ? DATETIME_FORMAT.format(date) : "";
    }
    
    /**
     * Parse date from string
     */
    public static Date parseDate(String dateStr) {
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Show info message dialog
     */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Style a button with primary color
     */
    public static void stylePrimaryButton(JButton button) {
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width + 20, 42));
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = button.getBackground();
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
    }
    
    /**
     * Style a button with success color
     */
    public static void styleSuccessButton(JButton button) {
        button.setBackground(SUCCESS_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width + 20, 42));
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
    }
    
    /**
     * Style a button with danger color
     */
    public static void styleDangerButton(JButton button) {
        button.setBackground(DANGER_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width + 20, 42));
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
    }
    
    /**
     * Style a button with secondary color
     */
    public static void styleSecondaryButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY_COLOR);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width + 20, 42));
    }
    
    /**
     * Style table
     */
    public static void styleTable(JTable table) {
        table.setFont(TEXT_FONT);
        table.setRowHeight(35);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(SECONDARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.setIntercellSpacing(new Dimension(10, 5));
        table.setForeground(TEXT_COLOR); // Ensure text color is set
        table.setBackground(Color.WHITE);
        
        // Style header
        JTableHeader header = table.getTableHeader();
        header.setFont(LABEL_FONT);
        header.setBackground(new Color(245, 245, 245)); // Light gray background
        header.setForeground(TEXT_COLOR); // Dark text for visibility
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, SECONDARY_COLOR));
        
        // Left align cells for better readability
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }
    }
    
    /**
     * Create a non-editable table model
     */
    public static DefaultTableModel createTableModel(Object[] columns, int rows) {
        return new DefaultTableModel(columns, rows) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
    
    /**
     * Show success message
     */
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show error message
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show warning message
     */
    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Show confirmation dialog
     */
    public static boolean showConfirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Confirm", 
                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Create titled panel
     */
    public static JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1), 
            title,
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            PRIMARY_COLOR
        ));
        return panel;
    }
    
    /**
     * Create form label
     */
    public static JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        return label;
    }
    
    /**
     * Validate required field
     */
    public static boolean validateRequired(JTextField field, String fieldName, Component parent) {
        if (field.getText().trim().isEmpty()) {
            showError(parent, fieldName + " is required!");
            field.requestFocus();
            return false;
        }
        return true;
    }
    
    /**
     * Validate number field
     */
    public static boolean validateNumber(JTextField field, String fieldName, Component parent) {
        try {
            Double.parseDouble(field.getText().trim());
            return true;
        } catch (NumberFormatException e) {
            showError(parent, fieldName + " must be a valid number!");
            field.requestFocus();
            return false;
        }
    }
    
    /**
     * Center frame on screen
     */
    public static void centerFrame(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
    }
}
