package com.uzima.models;

/**
 * Data container for the multi-step service request wizard
 */
public class ServiceRequestData {
    public String clientCategory;
    public String drillingType;
    public String pumpType;
    public int depthMeters;
    public int heightMeters;
    public String pipeType;
    public int pipeDiameterInches;
    public int pipeLengthMeters;
    public int numberOfOutlets;
    public int tankCapacityLitres;

    // Calculated amounts
    public double surveyFee;
    public double localAuthorityFee;
    public double drillingCost;
    public double pumpCost;
    public double plumbingCost;
    public double tankCost;
    public double depthHeightCharge;
    public double subtotal;
    public double taxAmount;
    public double totalAmount;
    public int invoiceId;
    public int requestId;
}
