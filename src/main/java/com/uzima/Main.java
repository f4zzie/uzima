package com.uzima;

import com.uzima.ui.LoginForm;

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
        
        // Show login form immediately - database check happens when user tries to login
        SwingUtilities.invokeLater(() -> {
            System.out.println("==============================================");
            System.out.println("UZIMA BOREHOLE DRILLING SYSTEM");
            System.out.println("Version 1.0.0");
            System.out.println("==============================================");
            
            // Launch login form - database check happens on login attempt
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }
}
