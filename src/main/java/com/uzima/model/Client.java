package com.uzima.model;

import java.util.Date;

/**
 * Client model representing borehole clients
 */
public class Client {
    private String clientId;
    private String fullName;
    private String physicalAddress;
    private String phoneNumber;
    private String phoneNumber2;
    private String email;
    private String clientCategory; // INDUSTRIAL, COMMERCIAL, DOMESTIC
    private String boreholeLocation;
    private String county;
    private String gpsCoordinates;
    private String idNumber;
    private String password; // For customer login
    private Date registrationDate;
    private boolean isActive;
    private int createdBy;
    private Date createdAt;
    private Date updatedAt;
    
    // Constructors
    public Client() {}
    
    public Client(String clientId, String fullName, String phoneNumber, String clientCategory) {
        this.clientId = clientId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.clientCategory = clientCategory;
    }
    
    // Getters and Setters
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhysicalAddress() {
        return physicalAddress;
    }
    
    public void setPhysicalAddress(String physicalAddress) {
        this.physicalAddress = physicalAddress;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getPhoneNumber2() {
        return phoneNumber2;
    }
    
    public void setPhoneNumber2(String phoneNumber2) {
        this.phoneNumber2 = phoneNumber2;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getClientCategory() {
        return clientCategory;
    }
    
    public void setClientCategory(String clientCategory) {
        this.clientCategory = clientCategory;
    }
    
    public String getBoreholeLocation() {
        return boreholeLocation;
    }
    
    public void setBoreholeLocation(String boreholeLocation) {
        this.boreholeLocation = boreholeLocation;
    }
    
    public String getCounty() {
        return county;
    }
    
    public void setCounty(String county) {
        this.county = county;
    }
    
    public String getGpsCoordinates() {
        return gpsCoordinates;
    }
    
    public void setGpsCoordinates(String gpsCoordinates) {
        this.gpsCoordinates = gpsCoordinates;
    }
    
    public String getIdNumber() {
        return idNumber;
    }
    
    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
    
    public Date getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public int getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
