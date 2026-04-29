package com.cloud.entity;

import java.time.LocalDateTime;

public class MonitoringData {
    private String id;
    private String resourceId;
    private String customerId;
    private Double cpuUsage;
    private Double memoryUsage;
    private Double diskUsage;
    private Double networkIn;
    private Double networkOut;
    private LocalDateTime timestamp;

    public MonitoringData() {
        this.timestamp = LocalDateTime.now();
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

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public Double getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(Double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public Double getNetworkIn() {
        return networkIn;
    }

    public void setNetworkIn(Double networkIn) {
        this.networkIn = networkIn;
    }

    public Double getNetworkOut() {
        return networkOut;
    }

    public void setNetworkOut(Double networkOut) {
        this.networkOut = networkOut;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
