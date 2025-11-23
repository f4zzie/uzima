package com.uzima.models;

/**
 * Represents a Plumbing Service in the UZIMA system
 */
public class PlumbingService {
    private int plumbingId;
    private String pipeType;
    private int pipeDiameterInches;
    private int pipeLengthMeters;
    private int numberOfOutlets;
    private double costPerMeter;
    private String description;

    // Constructor
    public PlumbingService(String pipeType, int pipeDiameterInches, int pipeLengthMeters, 
                           int numberOfOutlets, double costPerMeter, String description) {
        this.pipeType = pipeType;
        this.pipeDiameterInches = pipeDiameterInches;
        this.pipeLengthMeters = pipeLengthMeters;
        this.numberOfOutlets = numberOfOutlets;
        this.costPerMeter = costPerMeter;
        this.description = description;
    }

    // Full constructor with ID
    public PlumbingService(int plumbingId, String pipeType, int pipeDiameterInches, int pipeLengthMeters,
                           int numberOfOutlets, double costPerMeter, String description) {
        this.plumbingId = plumbingId;
        this.pipeType = pipeType;
        this.pipeDiameterInches = pipeDiameterInches;
        this.pipeLengthMeters = pipeLengthMeters;
        this.numberOfOutlets = numberOfOutlets;
        this.costPerMeter = costPerMeter;
        this.description = description;
    }

    // Getters and Setters
    public int getPlumbingId() {
        return plumbingId;
    }

    public void setPlumbingId(int plumbingId) {
        this.plumbingId = plumbingId;
    }

    public String getPipeType() {
        return pipeType;
    }

    public void setPipeType(String pipeType) {
        this.pipeType = pipeType;
    }

    public int getPipeDiameterInches() {
        return pipeDiameterInches;
    }

    public void setPipeDiameterInches(int pipeDiameterInches) {
        this.pipeDiameterInches = pipeDiameterInches;
    }

    public int getPipeLengthMeters() {
        return pipeLengthMeters;
    }

    public void setPipeLengthMeters(int pipeLengthMeters) {
        this.pipeLengthMeters = pipeLengthMeters;
    }

    public int getNumberOfOutlets() {
        return numberOfOutlets;
    }

    public void setNumberOfOutlets(int numberOfOutlets) {
        this.numberOfOutlets = numberOfOutlets;
    }

    public double getCostPerMeter() {
        return costPerMeter;
    }

    public void setCostPerMeter(double costPerMeter) {
        this.costPerMeter = costPerMeter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Calculate total plumbing cost
    public double calculateTotalCost() {
        return pipeLengthMeters * costPerMeter;
    }

    @Override
    public String toString() {
        return "PlumbingService{" +
                "plumbingId=" + plumbingId +
                ", pipeType='" + pipeType + '\'' +
                ", pipeLengthMeters=" + pipeLengthMeters +
                ", costPerMeter=" + costPerMeter +
                '}';
    }
}
