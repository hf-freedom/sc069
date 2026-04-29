package com.cloud.entity;

import java.time.LocalDateTime;

public class ScalingRule {
    private String id;
    private String resourceId;
    private String customerId;
    private ScalingType type;
    private MetricType metricType;
    private Double threshold;
    private Integer adjustmentAmount;
    private Integer cooldownSeconds;
    private LocalDateTime lastTriggeredAt;
    private boolean enabled;

    public ScalingRule() {
        this.enabled = true;
        this.cooldownSeconds = 300;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public ScalingType getType() {
        return type;
    }

    public void setType(ScalingType type) {
        this.type = type;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Integer getAdjustmentAmount() {
        return adjustmentAmount;
    }

    public void setAdjustmentAmount(Integer adjustmentAmount) {
        this.adjustmentAmount = adjustmentAmount;
    }

    public Integer getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(Integer cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public LocalDateTime getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
