package com.uzima.models;

import java.time.LocalDateTime;

/**
 * Represents an Invoice in the UZIMA system
 */
public class Invoice {
    private int invoiceId;
    private int requestId;
    private int clientId;
    private double surveyFee;
    private double localAuthorityFee;
    private double drillingCost;
    private double pumpCost;
    private double plumbingCost;
    private double tankCost;
    private double depthHeightCharge;
    private double subtotal;
    private double taxAmount;
    private double totalAmount;
    private LocalDateTime invoiceDate;
    private String paymentStatus; // Pending, Partial, Paid, Overdue

    // Constructor
    public Invoice(int requestId, int clientId, double surveyFee, double localAuthorityFee,
                   double drillingCost, double pumpCost, double plumbingCost, double tankCost,
                   double depthHeightCharge) {
        this.requestId = requestId;
        this.clientId = clientId;
        this.surveyFee = surveyFee;
        this.localAuthorityFee = localAuthorityFee;
        this.drillingCost = drillingCost;
        this.pumpCost = pumpCost;
        this.plumbingCost = plumbingCost;
        this.tankCost = tankCost;
        this.depthHeightCharge = depthHeightCharge;
        this.paymentStatus = "Pending";
        calculateTotals();
    }

    // Full constructor with ID and timestamps
    public Invoice(int invoiceId, int requestId, int clientId, double surveyFee, double localAuthorityFee,
                   double drillingCost, double pumpCost, double plumbingCost, double tankCost,
                   double depthHeightCharge, double subtotal, double taxAmount, double totalAmount,
                   LocalDateTime invoiceDate, String paymentStatus) {
        this.invoiceId = invoiceId;
        this.requestId = requestId;
        this.clientId = clientId;
        this.surveyFee = surveyFee;
        this.localAuthorityFee = localAuthorityFee;
        this.drillingCost = drillingCost;
        this.pumpCost = pumpCost;
        this.plumbingCost = plumbingCost;
        this.tankCost = tankCost;
        this.depthHeightCharge = depthHeightCharge;
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.invoiceDate = invoiceDate;
        this.paymentStatus = paymentStatus;
    }

    /**
     * Calculate subtotal, tax, and total amounts
     */
    private void calculateTotals() {
        this.subtotal = surveyFee + localAuthorityFee + drillingCost + pumpCost + plumbingCost + tankCost + depthHeightCharge;
        this.taxAmount = TaxCalculation.calculateTax(subtotal);
        this.totalAmount = TaxCalculation.calculateTotal(subtotal);
        this.invoiceDate = LocalDateTime.now();
    }

    // Getters and Setters
    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public double getSurveyFee() {
        return surveyFee;
    }

    public void setSurveyFee(double surveyFee) {
        this.surveyFee = surveyFee;
    }

    public double getLocalAuthorityFee() {
        return localAuthorityFee;
    }

    public void setLocalAuthorityFee(double localAuthorityFee) {
        this.localAuthorityFee = localAuthorityFee;
    }

    public double getDrillingCost() {
        return drillingCost;
    }

    public void setDrillingCost(double drillingCost) {
        this.drillingCost = drillingCost;
    }

    public double getPumpCost() {
        return pumpCost;
    }

    public void setPumpCost(double pumpCost) {
        this.pumpCost = pumpCost;
    }

    public double getPlumbingCost() {
        return plumbingCost;
    }

    public void setPlumbingCost(double plumbingCost) {
        this.plumbingCost = plumbingCost;
    }

    public double getTankCost() {
        return tankCost;
    }

    public void setTankCost(double tankCost) {
        this.tankCost = tankCost;
    }

    public double getDepthHeightCharge() {
        return depthHeightCharge;
    }

    public void setDepthHeightCharge(double depthHeightCharge) {
        this.depthHeightCharge = depthHeightCharge;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    /**
     * Recalculate totals after any cost changes
     */
    public void recalculateTotals() {
        calculateTotals();
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceId=" + invoiceId +
                ", clientId=" + clientId +
                ", subtotal=" + subtotal +
                ", taxAmount=" + taxAmount +
                ", totalAmount=" + totalAmount +
                ", paymentStatus='" + paymentStatus + '\'' +
                '}';
    }
}
