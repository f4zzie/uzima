package com.uzima.models;

import java.time.LocalDateTime;

/**
 * Represents a client in the UZIMA Borehole Drilling System
 */
public class Client {
    private int clientId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String category; // Industrial, Commercial, Domestic
    private String boreholeLocation;
    private String password; // Hashed password
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor
    public Client(String fullName, String email, String phone, String address, String category, String boreholeLocation) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.category = category;
        this.boreholeLocation = boreholeLocation;
    }

    // Full constructor with ID and timestamps
    public Client(int clientId, String fullName, String email, String phone, String address, 
                  String category, String boreholeLocation, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.clientId = clientId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.category = category;
        this.boreholeLocation = boreholeLocation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Full constructor with ID, timestamps, and password (for DAO use)
    public Client(int clientId, String fullName, String email, String phone, String address, 
                  String category, String boreholeLocation, String password, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.clientId = clientId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.category = category;
        this.boreholeLocation = boreholeLocation;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBoreholeLocation() {
        return boreholeLocation;
    }

    public void setBoreholeLocation(String boreholeLocation) {
        this.boreholeLocation = boreholeLocation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Client{" +
                "clientId=" + clientId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
