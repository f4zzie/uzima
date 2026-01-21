package com.uzima.util;

import com.uzima.model.User;

/**
 * Manages user session information throughout the application
 * Singleton pattern to maintain single session state
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private String sessionStartTime;
    
    private SessionManager() {
        // Private constructor for singleton
    }
    
    /**
     * Get singleton instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Set current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.sessionStartTime = java.time.LocalDateTime.now().toString();
    }
    
    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().equalsIgnoreCase(role);
    }
    
    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * Check if current user is manager or admin
     */
    public boolean isManagerOrAbove() {
        return hasRole("ADMIN") || hasRole("MANAGER");
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        currentUser = null;
        sessionStartTime = null;
    }
    
    /**
     * Get session start time
     */
    public String getSessionStartTime() {
        return sessionStartTime;
    }
}
