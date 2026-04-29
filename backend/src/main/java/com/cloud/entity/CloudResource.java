package com.cloud.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CloudResource {
    private String id;
    private String customerId;
    private ResourceType type;
    private String name;
    private ResourceStatus status;
    
    // 虚拟机属性
    private Integer cpuCores;
    private Integer memoryGB;
    
    // 存储属性
    private Integer storageGB;
    
    // 带宽属性
    private Integer bandwidthMbps;
    
    // 数据库属性
    private String dbType;
    private Integer dbVersion;
    
    // 计费属性
    private BigDecimal hourlyRate;
    private LocalDateTime billingStartTime;
    private LocalDateTime billingEndTime;
    private LocalDateTime lastBillingTime;
    
    // 扩缩容相关
    private Integer minCapacity;
    private Integer maxCapacity;
    private Integer currentCapacity;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CloudResource() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ResourceStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public Integer getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(Integer cpuCores) {
        this.cpuCores = cpuCores;
    }

    public Integer getMemoryGB() {
        return memoryGB;
    }

    public void setMemoryGB(Integer memoryGB) {
        this.memoryGB = memoryGB;
    }

    public Integer getStorageGB() {
        return storageGB;
    }

    public void setStorageGB(Integer storageGB) {
        this.storageGB = storageGB;
    }

    public Integer getBandwidthMbps() {
        return bandwidthMbps;
    }

    public void setBandwidthMbps(Integer bandwidthMbps) {
        this.bandwidthMbps = bandwidthMbps;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public Integer getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(Integer dbVersion) {
        this.dbVersion = dbVersion;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public LocalDateTime getBillingStartTime() {
        return billingStartTime;
    }

    public void setBillingStartTime(LocalDateTime billingStartTime) {
        this.billingStartTime = billingStartTime;
    }

    public LocalDateTime getBillingEndTime() {
        return billingEndTime;
    }

    public void setBillingEndTime(LocalDateTime billingEndTime) {
        this.billingEndTime = billingEndTime;
    }

    public LocalDateTime getLastBillingTime() {
        return lastBillingTime;
    }

    public void setLastBillingTime(LocalDateTime lastBillingTime) {
        this.lastBillingTime = lastBillingTime;
    }

    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer minCapacity) {
        this.minCapacity = minCapacity;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Integer getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(Integer currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
