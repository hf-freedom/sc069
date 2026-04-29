package com.cloud.service;

import com.cloud.entity.*;
import com.cloud.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private BillingService billingService;

    public CloudResource createVm(String customerId, String name, Integer cpuCores, Integer memoryGB) {
        Customer customer = customerService.getCustomer(customerId);
        if (customer == null) {
            throw new RuntimeException("客户不存在");
        }

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new RuntimeException("客户账户状态异常，无法创建资源");
        }

        ResourceQuota quota = customerService.getCustomerQuota(customerId);
        System.out.println("创建虚拟机 - 客户ID: " + customerId + 
                ", 已使用: " + quota.getVmUsed() + 
                ", 配额: " + quota.getVmQuota());
        if (!quota.canAllocateVm(1)) {
            throw new RuntimeException("虚拟机配额不足: 已使用 " + quota.getVmUsed() + 
                    ", 配额 " + quota.getVmQuota() + 
                    ", 需要 1 台");
        }

        BigDecimal hourlyRate = calculateVmHourlyRate(cpuCores, memoryGB);
        if (!customerService.canAfford(customerId, hourlyRate.multiply(new BigDecimal("24")))) {
            throw new RuntimeException("账户余额不足，至少需要24小时的费用");
        }

        CloudResource resource = new CloudResource();
        resource.setId(dataStore.generateId());
        resource.setCustomerId(customerId);
        resource.setType(ResourceType.VM);
        resource.setName(name);
        resource.setCpuCores(cpuCores);
        resource.setMemoryGB(memoryGB);
        resource.setHourlyRate(hourlyRate);
        resource.setMinCapacity(1);
        resource.setMaxCapacity(10);
        resource.setCurrentCapacity(1);

        quota.setVmUsed(quota.getVmUsed() + 1);

        resource.setStatus(ResourceStatus.RUNNING);
        resource.setBillingStartTime(LocalDateTime.now());

        dataStore.getResources().put(resource.getId(), resource);

        return resource;
    }

    public CloudResource createStorage(String customerId, String name, Integer storageGB) {
        Customer customer = customerService.getCustomer(customerId);
        if (customer == null) {
            throw new RuntimeException("客户不存在");
        }

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new RuntimeException("客户账户状态异常，无法创建资源");
        }

        ResourceQuota quota = customerService.getCustomerQuota(customerId);
        System.out.println("创建存储 - 客户ID: " + customerId + 
                ", 已使用: " + quota.getStorageUsed() + " GB" + 
                ", 配额: " + quota.getStorageQuota() + " GB" +
                ", 申请: " + storageGB + " GB");
        if (!quota.canAllocateStorage(storageGB)) {
            throw new RuntimeException("存储配额不足: 已使用 " + quota.getStorageUsed() + " GB" + 
                    ", 配额 " + quota.getStorageQuota() + " GB" + 
                    ", 申请 " + storageGB + " GB");
        }

        BigDecimal hourlyRate = calculateStorageHourlyRate(storageGB);
        if (!customerService.canAfford(customerId, hourlyRate.multiply(new BigDecimal("24")))) {
            throw new RuntimeException("账户余额不足，至少需要24小时的费用");
        }

        CloudResource resource = new CloudResource();
        resource.setId(dataStore.generateId());
        resource.setCustomerId(customerId);
        resource.setType(ResourceType.STORAGE);
        resource.setName(name);
        resource.setStorageGB(storageGB);
        resource.setHourlyRate(hourlyRate);
        resource.setMinCapacity(storageGB);
        resource.setMaxCapacity(storageGB * 10);
        resource.setCurrentCapacity(storageGB);

        quota.setStorageUsed(quota.getStorageUsed() + storageGB);

        resource.setStatus(ResourceStatus.RUNNING);
        resource.setBillingStartTime(LocalDateTime.now());

        dataStore.getResources().put(resource.getId(), resource);

        return resource;
    }

    public CloudResource createBandwidth(String customerId, String name, Integer bandwidthMbps) {
        Customer customer = customerService.getCustomer(customerId);
        if (customer == null) {
            throw new RuntimeException("客户不存在");
        }

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new RuntimeException("客户账户状态异常，无法创建资源");
        }

        ResourceQuota quota = customerService.getCustomerQuota(customerId);
        System.out.println("创建带宽 - 客户ID: " + customerId + 
                ", 已使用: " + quota.getBandwidthUsed() + " Mbps" + 
                ", 配额: " + quota.getBandwidthQuota() + " Mbps" +
                ", 申请: " + bandwidthMbps + " Mbps");
        if (!quota.canAllocateBandwidth(bandwidthMbps)) {
            throw new RuntimeException("带宽配额不足: 已使用 " + quota.getBandwidthUsed() + " Mbps" + 
                    ", 配额 " + quota.getBandwidthQuota() + " Mbps" + 
                    ", 申请 " + bandwidthMbps + " Mbps");
        }

        BigDecimal hourlyRate = calculateBandwidthHourlyRate(bandwidthMbps);
        if (!customerService.canAfford(customerId, hourlyRate.multiply(new BigDecimal("24")))) {
            throw new RuntimeException("账户余额不足，至少需要24小时的费用");
        }

        CloudResource resource = new CloudResource();
        resource.setId(dataStore.generateId());
        resource.setCustomerId(customerId);
        resource.setType(ResourceType.BANDWIDTH);
        resource.setName(name);
        resource.setBandwidthMbps(bandwidthMbps);
        resource.setHourlyRate(hourlyRate);
        resource.setMinCapacity(bandwidthMbps);
        resource.setMaxCapacity(bandwidthMbps * 10);
        resource.setCurrentCapacity(bandwidthMbps);

        quota.setBandwidthUsed(quota.getBandwidthUsed() + bandwidthMbps);

        resource.setStatus(ResourceStatus.RUNNING);
        resource.setBillingStartTime(LocalDateTime.now());

        dataStore.getResources().put(resource.getId(), resource);

        return resource;
    }

    public CloudResource createDatabase(String customerId, String name, String dbType, Integer dbVersion, Integer storageGB) {
        Customer customer = customerService.getCustomer(customerId);
        if (customer == null) {
            throw new RuntimeException("客户不存在");
        }

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new RuntimeException("客户账户状态异常，无法创建资源");
        }

        ResourceQuota quota = customerService.getCustomerQuota(customerId);
        System.out.println("创建数据库 - 客户ID: " + customerId + 
                ", 已使用: " + quota.getDatabaseUsed() + 
                ", 配额: " + quota.getDatabaseQuota());
        if (!quota.canAllocateDatabase(1)) {
            throw new RuntimeException("数据库配额不足: 已使用 " + quota.getDatabaseUsed() + 
                    ", 配额 " + quota.getDatabaseQuota() + 
                    ", 需要 1 个");
        }

        BigDecimal hourlyRate = calculateDatabaseHourlyRate(dbType, storageGB);
        if (!customerService.canAfford(customerId, hourlyRate.multiply(new BigDecimal("24")))) {
            throw new RuntimeException("账户余额不足，至少需要24小时的费用");
        }

        CloudResource resource = new CloudResource();
        resource.setId(dataStore.generateId());
        resource.setCustomerId(customerId);
        resource.setType(ResourceType.DATABASE);
        resource.setName(name);
        resource.setDbType(dbType);
        resource.setDbVersion(dbVersion);
        resource.setStorageGB(storageGB);
        resource.setHourlyRate(hourlyRate);
        resource.setMinCapacity(storageGB);
        resource.setMaxCapacity(storageGB * 5);
        resource.setCurrentCapacity(storageGB);

        quota.setDatabaseUsed(quota.getDatabaseUsed() + 1);

        resource.setStatus(ResourceStatus.RUNNING);
        resource.setBillingStartTime(LocalDateTime.now());

        dataStore.getResources().put(resource.getId(), resource);

        return resource;
    }

    public CloudResource getResource(String resourceId) {
        return dataStore.getResources().get(resourceId);
    }

    public List<CloudResource> getResourcesByCustomer(String customerId) {
        return dataStore.getResources().values().stream()
                .filter(r -> r.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    public List<CloudResource> getAllResources() {
        return dataStore.getResources().values().stream().collect(Collectors.toList());
    }

    public void stopResource(String resourceId) {
        CloudResource resource = dataStore.getResources().get(resourceId);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }

        if (resource.getStatus() == ResourceStatus.RUNNING) {
            resource.setStatus(ResourceStatus.STOPPED);
            resource.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void startResource(String resourceId) {
        CloudResource resource = dataStore.getResources().get(resourceId);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }

        if (resource.getStatus() == ResourceStatus.STOPPED) {
            resource.setStatus(ResourceStatus.RUNNING);
            resource.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void releaseResource(String resourceId) {
        CloudResource resource = dataStore.getResources().get(resourceId);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }

        if (resource.getStatus() == ResourceStatus.TERMINATED) {
            return;
        }

        resource.setStatus(ResourceStatus.TERMINATED);
        resource.setBillingEndTime(LocalDateTime.now());
        resource.setUpdatedAt(LocalDateTime.now());

        ResourceQuota quota = customerService.getCustomerQuota(resource.getCustomerId());
        if (quota != null) {
            switch (resource.getType()) {
                case VM:
                    quota.setVmUsed(quota.getVmUsed() - resource.getCurrentCapacity());
                    break;
                case STORAGE:
                    quota.setStorageUsed(quota.getStorageUsed() - resource.getCurrentCapacity());
                    break;
                case BANDWIDTH:
                    quota.setBandwidthUsed(quota.getBandwidthUsed() - resource.getCurrentCapacity());
                    break;
                case DATABASE:
                    quota.setDatabaseUsed(quota.getDatabaseUsed() - 1);
                    break;
            }
        }

        billingService.generateFinalBill(resourceId);
    }

    private BigDecimal calculateVmHourlyRate(Integer cpuCores, Integer memoryGB) {
        return new BigDecimal(cpuCores * 0.5 + memoryGB * 0.2);
    }

    private BigDecimal calculateStorageHourlyRate(Integer storageGB) {
        return new BigDecimal(storageGB * 0.01);
    }

    private BigDecimal calculateBandwidthHourlyRate(Integer bandwidthMbps) {
        return new BigDecimal(bandwidthMbps * 0.1);
    }

    private BigDecimal calculateDatabaseHourlyRate(String dbType, Integer storageGB) {
        BigDecimal baseRate = "mysql".equalsIgnoreCase(dbType) ? new BigDecimal("0.3") : new BigDecimal("0.5");
        return baseRate.add(new BigDecimal(storageGB * 0.02));
    }

    public CloudResource updateResource(String resourceId, String name, Integer cpuCores, Integer memoryGB,
                                         Integer storageGB, Integer bandwidthMbps, Integer minCapacity,
                                         Integer maxCapacity) {
        CloudResource resource = dataStore.getResources().get(resourceId);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }

        if (resource.getStatus() == ResourceStatus.TERMINATED) {
            throw new RuntimeException("已释放的资源无法更新");
        }

        if (name != null && !name.trim().isEmpty()) {
            resource.setName(name);
        }

        boolean specsChanged = false;
        BigDecimal oldHourlyRate = resource.getHourlyRate();

        if (resource.getType() == ResourceType.VM) {
            if (cpuCores != null && !cpuCores.equals(resource.getCpuCores())) {
                resource.setCpuCores(cpuCores);
                specsChanged = true;
            }
            if (memoryGB != null && !memoryGB.equals(resource.getMemoryGB())) {
                resource.setMemoryGB(memoryGB);
                specsChanged = true;
            }
            if (specsChanged) {
                resource.setHourlyRate(calculateVmHourlyRate(resource.getCpuCores(), resource.getMemoryGB()));
            }
        } else if (resource.getType() == ResourceType.STORAGE) {
            if (storageGB != null && !storageGB.equals(resource.getStorageGB())) {
                if (storageGB < resource.getMinCapacity()) {
                    throw new RuntimeException("存储容量不能低于最小容量: " + resource.getMinCapacity() + "GB");
                }
                
                int delta = storageGB - resource.getStorageGB();
                ResourceQuota quota = customerService.getCustomerQuota(resource.getCustomerId());
                
                if (delta > 0 && !quota.canAllocateStorage(delta)) {
                    throw new RuntimeException("存储配额不足");
                }
                
                quota.setStorageUsed(quota.getStorageUsed() + delta);
                resource.setStorageGB(storageGB);
                resource.setCurrentCapacity(storageGB);
                resource.setHourlyRate(calculateStorageHourlyRate(storageGB));
                specsChanged = true;
            }
        } else if (resource.getType() == ResourceType.BANDWIDTH) {
            if (bandwidthMbps != null && !bandwidthMbps.equals(resource.getBandwidthMbps())) {
                if (bandwidthMbps < resource.getMinCapacity()) {
                    throw new RuntimeException("带宽不能低于最小容量: " + resource.getMinCapacity() + "Mbps");
                }
                
                int delta = bandwidthMbps - resource.getBandwidthMbps();
                ResourceQuota quota = customerService.getCustomerQuota(resource.getCustomerId());
                
                if (delta > 0 && !quota.canAllocateBandwidth(delta)) {
                    throw new RuntimeException("带宽配额不足");
                }
                
                quota.setBandwidthUsed(quota.getBandwidthUsed() + delta);
                resource.setBandwidthMbps(bandwidthMbps);
                resource.setCurrentCapacity(bandwidthMbps);
                resource.setHourlyRate(calculateBandwidthHourlyRate(bandwidthMbps));
                specsChanged = true;
            }
        } else if (resource.getType() == ResourceType.DATABASE) {
            if (storageGB != null && !storageGB.equals(resource.getStorageGB())) {
                if (storageGB < resource.getMinCapacity()) {
                    throw new RuntimeException("存储容量不能低于最小容量: " + resource.getMinCapacity() + "GB");
                }
                
                resource.setStorageGB(storageGB);
                resource.setCurrentCapacity(storageGB);
                resource.setHourlyRate(calculateDatabaseHourlyRate(resource.getDbType(), storageGB));
                specsChanged = true;
            }
        }

        if (minCapacity != null) {
            resource.setMinCapacity(minCapacity);
        }
        if (maxCapacity != null) {
            resource.setMaxCapacity(maxCapacity);
        }

        resource.setUpdatedAt(LocalDateTime.now());

        return resource;
    }
}
