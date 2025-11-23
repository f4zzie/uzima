package com.uzima.models;

/**
 * Represents a Drilling Service in the UZIMA system
 */
public class DrillingService {
    private int drillingId;
    private String serviceType; // Symmetric, Core, Geo-Technical
    private double downPaymentFee;
    private String description;

    // Constructor
    public DrillingService(String serviceType, double downPaymentFee, String description) {
        this.serviceType = serviceType;
        this.downPaymentFee = downPaymentFee;
        this.description = description;
    }

    // Full constructor with ID
    public DrillingService(int drillingId, String serviceType, double downPaymentFee, String description) {
        this.drillingId = drillingId;
        this.serviceType = serviceType;
        this.downPaymentFee = downPaymentFee;
        this.description = description;
    }

    // Getters and Setters
    public int getDrillingId() {
        return drillingId;
    }

    public void setDrillingId(int drillingId) {
        this.drillingId = drillingId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public double getDownPaymentFee() {
        return downPaymentFee;
    }

    public void setDownPaymentFee(double downPaymentFee) {
        this.downPaymentFee = downPaymentFee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "DrillingService{" +
                "drillingId=" + drillingId +
                ", serviceType='" + serviceType + '\'' +
                ", downPaymentFee=" + downPaymentFee +
                '}';
    }
}
