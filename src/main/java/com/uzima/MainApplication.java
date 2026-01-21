package com.uzima;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.uzima.ui.LoginForm;
import com.uzima.util.DatabaseConnection;

/**
 * Alternative Main Application Entry Point
 */
public class MainApplication {
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Test database connection
        if (DatabaseConnection.testConnection()) {
            SwingUtilities.invokeLater(() -> {
                LoginForm loginForm = new LoginForm();
                loginForm.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(null, 
                "Cannot connect to database!\n\n" +
                "Please ensure MySQL is running and database is set up.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
