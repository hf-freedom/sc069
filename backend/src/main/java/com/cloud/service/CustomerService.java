package com.cloud.service;

import com.cloud.entity.Customer;
import com.cloud.entity.CustomerStatus;
import com.cloud.entity.ResourceQuota;
import com.cloud.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private DataStore dataStore;

    public Customer createCustomer(String name, String email, BigDecimal initialBalance, BigDecimal creditLimit) {
        Customer customer = new Customer();
        customer.setId(dataStore.generateId());
        customer.setName(name);
        customer.setEmail(email);
        customer.setAccountBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);
        customer.setCreditLimit(creditLimit != null ? creditLimit : new BigDecimal("5000.00"));
        customer.setCreditUsed(BigDecimal.ZERO);

        ResourceQuota quota = new ResourceQuota();
        quota.setCustomerId(customer.getId());
        customer.setQuota(quota);

        dataStore.getCustomers().put(customer.getId(), customer);
        dataStore.getQuotas().put(quota.getCustomerId(), quota);

        return customer;
    }

    public Customer getCustomer(String customerId) {
        return dataStore.getCustomers().get(customerId);
    }

    public List<Customer> getAllCustomers() {
        return dataStore.getCustomers().values().stream().collect(Collectors.toList());
    }

    public void updateCustomerStatus(String customerId, CustomerStatus status) {
        Customer customer = dataStore.getCustomers().get(customerId);
        if (customer != null) {
            customer.setStatus(status);
            customer.setUpdatedAt(LocalDateTime.now());
        }
    }

    public boolean canAfford(String customerId, BigDecimal amount) {
        Customer customer = dataStore.getCustomers().get(customerId);
        if (customer == null) {
            return false;
        }
        return customer.getTotalAvailable().compareTo(amount) >= 0;
    }

    public void deductBalance(String customerId, BigDecimal amount) {
        Customer customer = dataStore.getCustomers().get(customerId);
        if (customer == null) {
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        if (!canAfford(customerId, amount)) {
            throw new RuntimeException("余额不足，无法扣除");
        }

        BigDecimal accountBalance = customer.getAccountBalance() != null 
                ? customer.getAccountBalance() 
                : BigDecimal.ZERO;

        if (accountBalance.compareTo(amount) >= 0) {
            customer.setAccountBalance(accountBalance.subtract(amount));
        } else {
            BigDecimal remainingAmount = amount.subtract(accountBalance);
            customer.setAccountBalance(BigDecimal.ZERO);
            customer.setCreditUsed(customer.getCreditUsed().add(remainingAmount));
        }

        customer.setUpdatedAt(LocalDateTime.now());
    }

    public void addBalance(String customerId, BigDecimal amount) {
        Customer customer = dataStore.getCustomers().get(customerId);
        if (customer == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal creditUsed = customer.getCreditUsed() != null 
                ? customer.getCreditUsed() 
                : BigDecimal.ZERO;

        if (creditUsed.compareTo(BigDecimal.ZERO) > 0) {
            if (amount.compareTo(creditUsed) >= 0) {
                customer.setCreditUsed(BigDecimal.ZERO);
                customer.setAccountBalance(customer.getAccountBalance().add(amount.subtract(creditUsed)));
            } else {
                customer.setCreditUsed(creditUsed.subtract(amount));
            }
        } else {
            customer.setAccountBalance(customer.getAccountBalance().add(amount));
        }

        customer.setUpdatedAt(LocalDateTime.now());
    }

    public ResourceQuota getCustomerQuota(String customerId) {
        return dataStore.getQuotas().get(customerId);
    }

    public void updateQuota(String customerId, ResourceQuota newQuota) {
        ResourceQuota existingQuota = dataStore.getQuotas().get(customerId);
        if (existingQuota == null) {
            throw new RuntimeException("客户配额不存在");
        }

        System.out.println("更新配额 - 客户ID: " + customerId);
        System.out.println("  原始配额: vm=" + existingQuota.getVmQuota() + 
                ", storage=" + existingQuota.getStorageQuota() + 
                ", bandwidth=" + existingQuota.getBandwidthQuota() + 
                ", database=" + existingQuota.getDatabaseQuota());

        if (newQuota.getVmQuota() != null) {
            if (newQuota.getVmQuota() < existingQuota.getVmUsed()) {
                throw new RuntimeException("虚拟机配额不能低于当前已使用量: " + existingQuota.getVmUsed());
            }
            existingQuota.setVmQuota(newQuota.getVmQuota());
        }
        
        if (newQuota.getStorageQuota() != null) {
            if (newQuota.getStorageQuota() < existingQuota.getStorageUsed()) {
                throw new RuntimeException("存储配额不能低于当前已使用量: " + existingQuota.getStorageUsed());
            }
            existingQuota.setStorageQuota(newQuota.getStorageQuota());
        }
        
        if (newQuota.getBandwidthQuota() != null) {
            if (newQuota.getBandwidthQuota() < existingQuota.getBandwidthUsed()) {
                throw new RuntimeException("带宽配额不能低于当前已使用量: " + existingQuota.getBandwidthUsed());
            }
            existingQuota.setBandwidthQuota(newQuota.getBandwidthQuota());
        }
        
        if (newQuota.getDatabaseQuota() != null) {
            if (newQuota.getDatabaseQuota() < existingQuota.getDatabaseUsed()) {
                throw new RuntimeException("数据库配额不能低于当前已使用量: " + existingQuota.getDatabaseUsed());
            }
            existingQuota.setDatabaseQuota(newQuota.getDatabaseQuota());
        }

        System.out.println("  新配额: vm=" + existingQuota.getVmQuota() + 
                ", storage=" + existingQuota.getStorageQuota() + 
                ", bandwidth=" + existingQuota.getBandwidthQuota() + 
                ", database=" + existingQuota.getDatabaseQuota());
    }
}
