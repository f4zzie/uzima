package com.uzima.models;

/**
 * Represents a Pump Service in the UZIMA system
 */
public class PumpService {
    private int pumpId;
    private String pumpType; // Submersible Electric, Solar, Hand
    private double cost;
    private String description;

    // Constructor
    public PumpService(String pumpType, double cost, String description) {
        this.pumpType = pumpType;
        this.cost = cost;
        this.description = description;
    }

    // Full constructor with ID
    public PumpService(int pumpId, String pumpType, double cost, String description) {
        this.pumpId = pumpId;
        this.pumpType = pumpType;
        this.cost = cost;
        this.description = description;
    }

    // Getters and Setters
    public int getPumpId() {
        return pumpId;
    }

    public void setPumpId(int pumpId) {
        this.pumpId = pumpId;
    }

    public String getPumpType() {
        return pumpType;
    }

    public void setPumpType(String pumpType) {
        this.pumpType = pumpType;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "PumpService{" +
                "pumpId=" + pumpId +
                ", pumpType='" + pumpType + '\'' +
                ", cost=" + cost +
                '}';
    }
}
