package com.cloud.entity;

public class ResourceQuota {
    private String customerId;
    private Integer vmQuota;
    private Integer vmUsed;
    private Integer storageQuota;
    private Integer storageUsed;
    private Integer bandwidthQuota;
    private Integer bandwidthUsed;
    private Integer databaseQuota;
    private Integer databaseUsed;

    public ResourceQuota() {
        this.vmQuota = 10;
        this.vmUsed = 0;
        this.storageQuota = 1000;
        this.storageUsed = 0;
        this.bandwidthQuota = 1000;
        this.bandwidthUsed = 0;
        this.databaseQuota = 5;
        this.databaseUsed = 0;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Integer getVmQuota() {
        return vmQuota != null ? vmQuota : 10;
    }

    public void setVmQuota(Integer vmQuota) {
        this.vmQuota = vmQuota;
    }

    public Integer getVmUsed() {
        return vmUsed != null ? vmUsed : 0;
    }

    public void setVmUsed(Integer vmUsed) {
        this.vmUsed = vmUsed;
    }

    public Integer getStorageQuota() {
        return storageQuota != null ? storageQuota : 1000;
    }

    public void setStorageQuota(Integer storageQuota) {
        this.storageQuota = storageQuota;
    }

    public Integer getStorageUsed() {
        return storageUsed != null ? storageUsed : 0;
    }

    public void setStorageUsed(Integer storageUsed) {
        this.storageUsed = storageUsed;
    }

    public Integer getBandwidthQuota() {
        return bandwidthQuota != null ? bandwidthQuota : 1000;
    }

    public void setBandwidthQuota(Integer bandwidthQuota) {
        this.bandwidthQuota = bandwidthQuota;
    }

    public Integer getBandwidthUsed() {
        return bandwidthUsed != null ? bandwidthUsed : 0;
    }

    public void setBandwidthUsed(Integer bandwidthUsed) {
        this.bandwidthUsed = bandwidthUsed;
    }

    public Integer getDatabaseQuota() {
        return databaseQuota != null ? databaseQuota : 5;
    }

    public void setDatabaseQuota(Integer databaseQuota) {
        this.databaseQuota = databaseQuota;
    }

    public Integer getDatabaseUsed() {
        return databaseUsed != null ? databaseUsed : 0;
    }

    public void setDatabaseUsed(Integer databaseUsed) {
        this.databaseUsed = databaseUsed;
    }

    public boolean canAllocateVm(int amount) {
        return getVmUsed() + amount <= getVmQuota();
    }

    public boolean canAllocateStorage(int amount) {
        return getStorageUsed() + amount <= getStorageQuota();
    }

    public boolean canAllocateBandwidth(int amount) {
        return getBandwidthUsed() + amount <= getBandwidthQuota();
    }

    public boolean canAllocateDatabase(int amount) {
        return getDatabaseUsed() + amount <= getDatabaseQuota();
    }
}
