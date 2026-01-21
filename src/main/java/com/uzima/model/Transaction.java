package com.uzima.model;

import java.util.Date;

/**
 * Transaction model representing client transactions/orders
 */
public class Transaction {
    private int transactionId;
    private String transactionNo;
    private String clientId;
    private Date transactionDate;
    
    // Drilling Details
    private int drillingTypeId;
    private double boreholeDepth;
    private double drillingCost;
    
    // Pump Details
    private int pumpTypeId;
    private double tankHeight;
    private double pumpCost;
    
    // Tank Details
    private int tankTypeId;
    private double tankCost;
    
    // Plumbing Details
    private boolean hasPlumbing;
    private double plumbingCost;
    
    // Fees
    private double surveyFee;
    private double authorityFee;
    
    // Totals
    private double subtotal;
    private double taxAmount;
    private double grandTotal;
    
    // Payment
    private String paymentStatus; // NOT_PAID, PARTIALLY_PAID, PAID
    private double amountPaid;
    private double balance;
    
    // Job Status
    private String jobStatus; // PENDING_SURVEY, APPROVED, IN_PROGRESS, COMPLETED
    
    private String notes;
    private int createdBy;
    private Date createdAt;
    private Date updatedAt;
    
    // Constructors
    public Transaction() {}
    
    // Getters and Setters
    public int getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getTransactionNo() {
        return transactionNo;
    }
    
    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public Date getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public int getDrillingTypeId() {
        return drillingTypeId;
    }
    
    public void setDrillingTypeId(int drillingTypeId) {
        this.drillingTypeId = drillingTypeId;
    }
    
    public double getBoreholeDepth() {
        return boreholeDepth;
    }
    
    public void setBoreholeDepth(double boreholeDepth) {
        this.boreholeDepth = boreholeDepth;
    }
    
    public double getDrillingCost() {
        return drillingCost;
    }
    
    public void setDrillingCost(double drillingCost) {
        this.drillingCost = drillingCost;
    }
    
    public int getPumpTypeId() {
        return pumpTypeId;
    }
    
    public void setPumpTypeId(int pumpTypeId) {
        this.pumpTypeId = pumpTypeId;
    }
    
    public double getTankHeight() {
        return tankHeight;
    }
    
    public void setTankHeight(double tankHeight) {
        this.tankHeight = tankHeight;
    }
    
    public double getPumpCost() {
        return pumpCost;
    }
    
    public void setPumpCost(double pumpCost) {
        this.pumpCost = pumpCost;
    }
    
    public int getTankTypeId() {
        return tankTypeId;
    }
    
    public void setTankTypeId(int tankTypeId) {
        this.tankTypeId = tankTypeId;
    }
    
    public double getTankCost() {
        return tankCost;
    }
    
    public void setTankCost(double tankCost) {
        this.tankCost = tankCost;
    }
    
    public boolean isHasPlumbing() {
        return hasPlumbing;
    }
    
    public void setHasPlumbing(boolean hasPlumbing) {
        this.hasPlumbing = hasPlumbing;
    }
    
    public double getPlumbingCost() {
        return plumbingCost;
    }
    
    public void setPlumbingCost(double plumbingCost) {
        this.plumbingCost = plumbingCost;
    }
    
    public double getSurveyFee() {
        return surveyFee;
    }
    
    public void setSurveyFee(double surveyFee) {
        this.surveyFee = surveyFee;
    }
    
    public double getAuthorityFee() {
        return authorityFee;
    }
    
    public void setAuthorityFee(double authorityFee) {
        this.authorityFee = authorityFee;
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
    
    public double getGrandTotal() {
        return grandTotal;
    }
    
    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public double getAmountPaid() {
        return amountPaid;
    }
    
    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public String getJobStatus() {
        return jobStatus;
    }
    
    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
}
