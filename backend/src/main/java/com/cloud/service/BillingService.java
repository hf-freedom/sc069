package com.cloud.service;

import com.cloud.entity.*;
import com.cloud.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillingService {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ResourceService resourceService;

    public void generateHourlyUsage() {
        LocalDateTime now = LocalDateTime.now();
        List<CloudResource> runningResources = resourceService.getAllResources().stream()
                .filter(r -> r.getStatus() == ResourceStatus.RUNNING || r.getStatus() == ResourceStatus.RESTRICTED)
                .collect(Collectors.toList());

        for (CloudResource resource : runningResources) {
            LocalDateTime lastBillingTime = resource.getLastBillingTime();
            if (lastBillingTime == null) {
                lastBillingTime = resource.getBillingStartTime();
                if (lastBillingTime == null) {
                    lastBillingTime = now;
                }
            }

            Duration duration = Duration.between(lastBillingTime, now);
            long hours = duration.toHours();

            if (hours >= 1) {
                BigDecimal usageHours = new BigDecimal(hours);
                BigDecimal hourlyCost = usageHours.multiply(resource.getHourlyRate());

                try {
                    if (customerService.canAfford(resource.getCustomerId(), hourlyCost)) {
                        customerService.deductBalance(resource.getCustomerId(), hourlyCost);

                        DailyUsage dailyUsage = new DailyUsage();
                        dailyUsage.setId(dataStore.generateId());
                        dailyUsage.setResourceId(resource.getId());
                        dailyUsage.setCustomerId(resource.getCustomerId());
                        dailyUsage.setResourceType(resource.getType());
                        dailyUsage.setUsageDate(LocalDate.now());
                        dailyUsage.setUsageHours(usageHours);
                        dailyUsage.setHourlyRate(resource.getHourlyRate());
                        dailyUsage.setDailyCost(hourlyCost);

                        dataStore.getDailyUsages().put(dailyUsage.getId(), dailyUsage);
                        dataStore.getDailyUsageList().add(dailyUsage);

                        resource.setLastBillingTime(now);
                        resource.setUpdatedAt(now);

                        System.out.println("生成小时使用记录: 资源=" + resource.getId() + 
                                ", 客户=" + resource.getCustomerId() + 
                                ", 小时数=" + hours + 
                                ", 费用=" + hourlyCost);
                    } else {
                        System.out.println("余额不足，无法扣费: 资源=" + resource.getId() + 
                                ", 费用=" + hourlyCost);
                    }
                } catch (RuntimeException e) {
                    System.err.println("扣费失败: 资源=" + resource.getId() + ", 错误=" + e.getMessage());
                }
            }
        }

        checkOverdueAccounts();
    }

    public void generateDailyUsage() {
        generateHourlyUsage();
    }

    public void checkOverdueAccounts() {
        List<Customer> customers = customerService.getAllCustomers();

        for (Customer customer : customers) {
            if (customer.getStatus() == CustomerStatus.TERMINATED) {
                continue;
            }

            BigDecimal totalAvailable = customer.getTotalAvailable();
            
            if (totalAvailable.compareTo(BigDecimal.ZERO) < 0) {
                if (customer.getStatus() == CustomerStatus.ACTIVE) {
                    customerService.updateCustomerStatus(customer.getId(), CustomerStatus.RESTRICTED);
                    
                    restrictCustomerResources(customer.getId());
                    
                    System.out.println("客户账户欠费，进入限制状态: " + customer.getId() + " - " + customer.getName());
                } else if (customer.getStatus() == CustomerStatus.RESTRICTED) {
                    if (totalAvailable.compareTo(new BigDecimal("-1000")) < 0) {
                        customerService.updateCustomerStatus(customer.getId(), CustomerStatus.SUSPENDED);
                        
                        suspendCustomerResources(customer.getId());
                        
                        System.out.println("客户长期欠费，账户停机: " + customer.getId() + " - " + customer.getName());
                    }
                }
            } else if (customer.getStatus() == CustomerStatus.RESTRICTED) {
                if (totalAvailable.compareTo(BigDecimal.ZERO) >= 0) {
                    customerService.updateCustomerStatus(customer.getId(), CustomerStatus.ACTIVE);
                    
                    restoreCustomerResources(customer.getId());
                    
                    System.out.println("客户账户已充值，恢复正常状态: " + customer.getId() + " - " + customer.getName());
                }
            }
        }
    }

    private void restrictCustomerResources(String customerId) {
        List<CloudResource> resources = resourceService.getResourcesByCustomer(customerId);
        for (CloudResource resource : resources) {
            if (resource.getStatus() == ResourceStatus.RUNNING) {
                resource.setStatus(ResourceStatus.RESTRICTED);
                resource.setUpdatedAt(LocalDateTime.now());
            }
        }
    }

    private void suspendCustomerResources(String customerId) {
        List<CloudResource> resources = resourceService.getResourcesByCustomer(customerId);
        for (CloudResource resource : resources) {
            if (resource.getStatus() == ResourceStatus.RUNNING || resource.getStatus() == ResourceStatus.RESTRICTED) {
                resource.setStatus(ResourceStatus.STOPPED);
                resource.setUpdatedAt(LocalDateTime.now());
            }
        }
    }

    private void restoreCustomerResources(String customerId) {
        List<CloudResource> resources = resourceService.getResourcesByCustomer(customerId);
        for (CloudResource resource : resources) {
            if (resource.getStatus() == ResourceStatus.RESTRICTED) {
                resource.setStatus(ResourceStatus.RUNNING);
                resource.setUpdatedAt(LocalDateTime.now());
            }
        }
    }

    public Bill generateDailyBill(String customerId, LocalDate date) {
        List<DailyUsage> usages = dataStore.getDailyUsageList().stream()
                .filter(u -> u.getCustomerId().equals(customerId) && u.getUsageDate().equals(date))
                .collect(Collectors.toList());

        if (usages.isEmpty()) {
            return null;
        }

        Bill bill = new Bill();
        bill.setId(dataStore.generateId());
        bill.setCustomerId(customerId);
        bill.setBillingDate(date);

        for (DailyUsage usage : usages) {
            BillItem item = new BillItem();
            item.setResourceId(usage.getResourceId());
            item.setResourceType(usage.getResourceType());
            item.setResourceName(getResourceName(usage.getResourceId()));
            item.setUsageHours(usage.getUsageHours());
            item.setHourlyRate(usage.getHourlyRate());
            item.setAmount(usage.getDailyCost());

            bill.addItem(item);
        }

        dataStore.getBills().put(bill.getId(), bill);

        return bill;
    }

    public void generateFinalBill(String resourceId) {
        CloudResource resource = resourceService.getResource(resourceId);
        if (resource == null) {
            return;
        }

        LocalDateTime startTime = resource.getLastBillingTime() != null 
                ? resource.getLastBillingTime() 
                : resource.getBillingStartTime();
        if (startTime == null) {
            startTime = resource.getCreatedAt();
        }
        
        LocalDateTime endTime = resource.getBillingEndTime();
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();
        
        if (hours >= 1) {
            BigDecimal usageHours = new BigDecimal(hours);
            BigDecimal totalAmount = usageHours.multiply(resource.getHourlyRate());

            try {
                if (customerService.canAfford(resource.getCustomerId(), totalAmount)) {
                    customerService.deductBalance(resource.getCustomerId(), totalAmount);

                    BillItem item = new BillItem();
                    item.setResourceId(resource.getId());
                    item.setResourceType(resource.getType());
                    item.setResourceName(resource.getName());
                    item.setUsageHours(usageHours);
                    item.setHourlyRate(resource.getHourlyRate());
                    item.setAmount(totalAmount);

                    Bill bill = new Bill();
                    bill.setId(dataStore.generateId());
                    bill.setCustomerId(resource.getCustomerId());
                    bill.setBillingDate(LocalDate.now());
                    bill.addItem(item);
                    bill.setStatus(BillStatus.PAID);

                    dataStore.getBills().put(bill.getId(), bill);

                    System.out.println("生成最终账单: 资源=" + resourceId + ", 总费用=" + totalAmount);
                }
            } catch (RuntimeException e) {
                System.err.println("生成最终账单失败: " + e.getMessage());
            }
        }
    }

    private String getResourceName(String resourceId) {
        CloudResource resource = resourceService.getResource(resourceId);
        return resource != null ? resource.getName() : "Unknown";
    }

    public List<Bill> getBillsByCustomer(String customerId) {
        return dataStore.getBills().values().stream()
                .filter(b -> b.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    public List<DailyUsage> getDailyUsagesByCustomer(String customerId, LocalDate startDate, LocalDate endDate) {
        return dataStore.getDailyUsageList().stream()
                .filter(u -> u.getCustomerId().equals(customerId)
                        && !u.getUsageDate().isBefore(startDate)
                        && !u.getUsageDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    public BigDecimal calculateTotalCost(String customerId, LocalDate startDate, LocalDate endDate) {
        return getDailyUsagesByCustomer(customerId, startDate, endDate).stream()
                .map(DailyUsage::getDailyCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
