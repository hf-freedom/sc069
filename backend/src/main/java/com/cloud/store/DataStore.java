package com.cloud.store;

import com.cloud.entity.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataStore {
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();
    private final Map<String, ResourceQuota> quotas = new ConcurrentHashMap<>();
    private final Map<String, CloudResource> resources = new ConcurrentHashMap<>();
    private final Map<String, MonitoringData> monitoringData = new ConcurrentHashMap<>();
    private final Map<String, ScalingRule> scalingRules = new ConcurrentHashMap<>();
    private final Map<String, Bill> bills = new ConcurrentHashMap<>();
    private final Map<String, DailyUsage> dailyUsages = new ConcurrentHashMap<>();

    private final List<MonitoringData> monitoringDataList = Collections.synchronizedList(new ArrayList<>());
    private final List<DailyUsage> dailyUsageList = Collections.synchronizedList(new ArrayList<>());

    @PostConstruct
    public void init() {
        initializeBaseData();
    }

    private void initializeBaseData() {
        // 创建测试客户
        Customer customer1 = new Customer();
        customer1.setId("C001");
        customer1.setName("张三");
        customer1.setEmail("zhangsan@example.com");
        customer1.setAccountBalance(new BigDecimal("5000.00"));
        customer1.setCreditLimit(new BigDecimal("10000.00"));

        ResourceQuota quota1 = new ResourceQuota();
        quota1.setCustomerId("C001");
        quota1.setVmQuota(10);
        quota1.setStorageQuota(2000);
        quota1.setBandwidthQuota(2000);
        quota1.setDatabaseQuota(5);

        customer1.setQuota(quota1);

        customers.put(customer1.getId(), customer1);
        quotas.put(quota1.getCustomerId(), quota1);

        // 创建第二个测试客户
        Customer customer2 = new Customer();
        customer2.setId("C002");
        customer2.setName("李四");
        customer2.setEmail("lisi@example.com");
        customer2.setAccountBalance(new BigDecimal("1000.00"));
        customer2.setCreditLimit(new BigDecimal("5000.00"));

        ResourceQuota quota2 = new ResourceQuota();
        quota2.setCustomerId("C002");
        quota2.setVmQuota(5);
        quota2.setStorageQuota(500);
        quota2.setBandwidthQuota(500);
        quota2.setDatabaseQuota(2);

        customer2.setQuota(quota2);

        customers.put(customer2.getId(), customer2);
        quotas.put(quota2.getCustomerId(), quota2);

        System.out.println("基础数据初始化完成");
        System.out.println("客户数量: " + customers.size());
    }

    public Map<String, Customer> getCustomers() {
        return customers;
    }

    public Map<String, ResourceQuota> getQuotas() {
        return quotas;
    }

    public Map<String, CloudResource> getResources() {
        return resources;
    }

    public Map<String, MonitoringData> getMonitoringData() {
        return monitoringData;
    }

    public Map<String, ScalingRule> getScalingRules() {
        return scalingRules;
    }

    public Map<String, Bill> getBills() {
        return bills;
    }

    public Map<String, DailyUsage> getDailyUsages() {
        return dailyUsages;
    }

    public List<MonitoringData> getMonitoringDataList() {
        return monitoringDataList;
    }

    public List<DailyUsage> getDailyUsageList() {
        return dailyUsageList;
    }

    public String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
