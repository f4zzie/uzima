package com.uzima;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.uzima.ui.LoginForm;

/**
 * Main Application Entry Point
 * Shows login form immediately - database check happens on login attempt
 */
public class MainApplication {
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Show login form immediately - database check happens when user tries to login
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }
}
