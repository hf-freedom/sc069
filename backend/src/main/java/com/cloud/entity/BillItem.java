package com.cloud.entity;

import java.math.BigDecimal;

public class BillItem {
    private String resourceId;
    private ResourceType resourceType;
    private String resourceName;
    private BigDecimal usageHours;
    private BigDecimal hourlyRate;
    private BigDecimal amount;

    public BigDecimal getAmount() {
        if (usageHours != null && hourlyRate != null) {
            return usageHours.multiply(hourlyRate);
        }
        return amount;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public BigDecimal getUsageHours() {
        return usageHours;
    }

    public void setUsageHours(BigDecimal usageHours) {
        this.usageHours = usageHours;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
