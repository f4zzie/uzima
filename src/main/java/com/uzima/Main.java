package com.uzima;

import com.uzima.ui.LoginForm;
import com.uzima.util.DatabaseConnection;
import com.uzima.util.UIHelper;

import javax.swing.*;

/**
 * Main Application Entry Point for Uzima Borehole Drilling System
 * 
 * @author Your Name
 * @version 1.0.0
 * @date 2026-01-21
 */
public class Main {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Test database connection on startup
        SwingUtilities.invokeLater(() -> {
            System.out.println("==============================================");
            System.out.println("UZIMA BOREHOLE DRILLING SYSTEM");
            System.out.println("Version 1.0.0");
            System.out.println("==============================================");
            
            // Test database connection
            if (DatabaseConnection.testConnection()) {
                System.out.println("✓ Database connection successful");
                
                // Launch login form
                LoginForm loginForm = new LoginForm();
                loginForm.setVisible(true);
                
            } else {
                System.err.println("✗ Database connection failed!");
                
                JOptionPane.showMessageDialog(null, 
                    "Cannot connect to database!\n\n" +
                    "Please ensure:\n" +
                    "1. MySQL server is running\n" +
                    "2. Database 'uzima_borehole_db' exists\n" +
                    "3. Run the SQL setup script in database/uzima_db_setup.sql\n" +
                    "4. Update database credentials in DatabaseConnection.java\n\n" +
                    "Application will now exit.",
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
                
                System.exit(1);
            }
        });
    }
}
