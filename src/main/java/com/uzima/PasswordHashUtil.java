package com.uzima;

import java.security.MessageDigest;
import java.util.Base64;

public class PasswordHashUtil {
    
    /**
     * Generate SHA-256 Base64 hash of a password
     * This is the same method used in ClientService
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Generate hashes for common test passwords
     */
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("UZIMA PASSWORD HASH GENERATOR");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println();
        
        String[] testPasswords = {
            "password123",  // Admin password
            "test123",      // Test accounts
            "secure456"     // Alternative
        };
        
        for (String password : testPasswords) {
            String hash = hashPassword(password);
            System.out.println("Password: " + password);
            System.out.println("Hash:     " + hash);
            System.out.println();
        }
        
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("SQL UPDATE COMMANDS:");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println();
        
        String adminHash = hashPassword("password123");
        System.out.println("UPDATE clients SET password='" + adminHash + "' WHERE email='admin@uzima.com';");
        System.out.println("UPDATE clients SET password='" + adminHash + "' WHERE email='john@example.com';");
        System.out.println("UPDATE clients SET password='" + adminHash + "' WHERE email='sarah@example.com';");
        System.out.println("UPDATE clients SET password='" + adminHash + "' WHERE email='mike@example.com';");
        System.out.println();
    }
}
