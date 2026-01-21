package com.uzima.model;

import java.math.BigDecimal;

/**
 * Model class representing various service types (Drilling, Pump, Tank)
 */
public class ServiceType {
    private int id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal basePrice;
    private BigDecimal pricePerMeter;
    private String powerRating;
    private int maxDepth;
    private int capacity;
    private String material;
    private boolean active;
    
    // Constructors
    public ServiceType() {
        this.active = true;
    }
    
    public ServiceType(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = true;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    
    public BigDecimal getPricePerMeter() {
        return pricePerMeter;
    }
    
    public void setPricePerMeter(BigDecimal pricePerMeter) {
        this.pricePerMeter = pricePerMeter;
    }
    
    public String getPowerRating() {
        return powerRating;
    }
    
    public void setPowerRating(String powerRating) {
        this.powerRating = powerRating;
    }
    
    public int getMaxDepth() {
        return maxDepth;
    }
    
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    
    public String getMaterial() {
        return material;
    }
    
    public void setMaterial(String material) {
        this.material = material;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
