package com.cloud.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Customer {
    private String id;
    private String name;
    private String email;
    private BigDecimal accountBalance;
    private BigDecimal creditLimit;
    private BigDecimal creditUsed;
    private ResourceQuota quota;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Customer() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = CustomerStatus.ACTIVE;
        this.creditUsed = BigDecimal.ZERO;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getCreditUsed() {
        return creditUsed;
    }

    public void setCreditUsed(BigDecimal creditUsed) {
        this.creditUsed = creditUsed;
    }

    public BigDecimal getAvailableCredit() {
        if (creditLimit == null) {
            return BigDecimal.ZERO;
        }
        return creditLimit.subtract(creditUsed != null ? creditUsed : BigDecimal.ZERO);
    }

    public BigDecimal getTotalAvailable() {
        BigDecimal balance = accountBalance != null ? accountBalance : BigDecimal.ZERO;
        return balance.add(getAvailableCredit());
    }

    public ResourceQuota getQuota() {
        return quota;
    }

    public void setQuota(ResourceQuota quota) {
        this.quota = quota;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
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
