package com.cloud.service;

import com.cloud.entity.*;
import com.cloud.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScalingService {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private MonitoringService monitoringService;

    public ScalingRule createScalingRule(String resourceId, String customerId, ScalingType type,
                                           MetricType metricType, Double threshold, Integer adjustmentAmount,
                                           Integer cooldownSeconds) {
        CloudResource resource = resourceService.getResource(resourceId);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }

        if (!resource.getCustomerId().equals(customerId)) {
            throw new RuntimeException("资源不属于该客户");
        }

        ScalingRule rule = new ScalingRule();
        rule.setId(dataStore.generateId());
        rule.setResourceId(resourceId);
        rule.setCustomerId(customerId);
        rule.setType(type);
        rule.setMetricType(metricType);
        rule.setThreshold(threshold);
        rule.setAdjustmentAmount(adjustmentAmount);
        rule.setCooldownSeconds(cooldownSeconds != null ? cooldownSeconds : 300);
        rule.setEnabled(true);

        dataStore.getScalingRules().put(rule.getId(), rule);

        return rule;
    }

    public void evaluateScalingRules() {
        LocalDateTime now = LocalDateTime.now();
        List<ScalingRule> activeRules = dataStore.getScalingRules().values().stream()
                .filter(ScalingRule::isEnabled)
                .collect(Collectors.toList());

        for (ScalingRule rule : activeRules) {
            if (rule.getLastTriggeredAt() != null) {
                Duration duration = Duration.between(rule.getLastTriggeredAt(), now);
                if (duration.getSeconds() < rule.getCooldownSeconds()) {
                    continue;
                }
            }

            CloudResource resource = resourceService.getResource(rule.getResourceId());
            if (resource == null || resource.getStatus() != ResourceStatus.RUNNING) {
                continue;
            }

            LocalDateTime startTime = now.minusMinutes(5);
            double averageValue = getAverageMetricValue(rule.getResourceId(), rule.getMetricType(), startTime, now);

            boolean shouldScale = false;
            if (rule.getType() == ScalingType.SCALE_OUT) {
                shouldScale = averageValue >= rule.getThreshold();
            } else {
                shouldScale = averageValue <= rule.getThreshold();
            }

            if (shouldScale) {
                try {
                    performScaling(rule, resource);
                    rule.setLastTriggeredAt(now);
                    System.out.println("触发" + (rule.getType() == ScalingType.SCALE_OUT ? "扩容" : "缩容") + 
                            "规则: " + rule.getId() + "，资源: " + resource.getId());
                } catch (Exception e) {
                    System.err.println("执行" + (rule.getType() == ScalingType.SCALE_OUT ? "扩容" : "缩容") + 
                            "失败: " + e.getMessage());
                }
            }
        }
    }

    private void performScaling(ScalingRule rule, CloudResource resource) {
        if (rule.getType() == ScalingType.SCALE_OUT) {
            performScaleOut(rule, resource);
        } else {
            performScaleIn(rule, resource);
        }
    }

    private void performScaleOut(ScalingRule rule, CloudResource resource) {
        int newCapacity = resource.getCurrentCapacity() + rule.getAdjustmentAmount();
        
        if (newCapacity > resource.getMaxCapacity()) {
            throw new RuntimeException("扩容后容量超过最大限制");
        }

        ResourceQuota quota = customerService.getCustomerQuota(resource.getCustomerId());
        if (quota != null) {
            boolean canScale = false;
            switch (resource.getType()) {
                case VM:
                    canScale = quota.canAllocateVm(rule.getAdjustmentAmount());
                    break;
                case STORAGE:
                    canScale = quota.canAllocateStorage(rule.getAdjustmentAmount());
                    break;
                case BANDWIDTH:
                    canScale = quota.canAllocateBandwidth(rule.getAdjustmentAmount());
                    break;
                case DATABASE:
                    canScale = quota.canAllocateDatabase(rule.getAdjustmentAmount());
                    break;
            }
            if (!canScale) {
                throw new RuntimeException("配额不足，无法扩容");
            }
        }

        BigDecimal additionalCost = resource.getHourlyRate().multiply(new BigDecimal(rule.getAdjustmentAmount()));
        if (!customerService.canAfford(resource.getCustomerId(), additionalCost.multiply(new BigDecimal("24")))) {
            throw new RuntimeException("账户余额不足，无法扩容");
        }

        resource.setCurrentCapacity(newCapacity);
        resource.setUpdatedAt(LocalDateTime.now());

        updateQuotaUsed(resource.getCustomerId(), resource.getType(), rule.getAdjustmentAmount(), true);
    }

    private void performScaleIn(ScalingRule rule, CloudResource resource) {
        int newCapacity = resource.getCurrentCapacity() - rule.getAdjustmentAmount();
        
        if (newCapacity < resource.getMinCapacity()) {
            throw new RuntimeException("缩容后容量低于最小限制");
        }

        resource.setCurrentCapacity(newCapacity);
        resource.setUpdatedAt(LocalDateTime.now());

        updateQuotaUsed(resource.getCustomerId(), resource.getType(), rule.getAdjustmentAmount(), false);
    }

    private void updateQuotaUsed(String customerId, ResourceType type, int amount, boolean isIncrease) {
        ResourceQuota quota = customerService.getCustomerQuota(customerId);
        if (quota == null) return;

        int delta = isIncrease ? amount : -amount;

        switch (type) {
            case VM:
                quota.setVmUsed(quota.getVmUsed() + delta);
                break;
            case STORAGE:
                quota.setStorageUsed(quota.getStorageUsed() + delta);
                break;
            case BANDWIDTH:
                quota.setBandwidthUsed(quota.getBandwidthUsed() + delta);
                break;
            case DATABASE:
                quota.setDatabaseUsed(quota.getDatabaseUsed() + delta);
                break;
        }
    }

    private double getAverageMetricValue(String resourceId, MetricType metricType, 
                                           LocalDateTime startTime, LocalDateTime endTime) {
        List<MonitoringData> dataList = monitoringService.getMetricsByTimeRange(resourceId, startTime, endTime);
        
        if (dataList.isEmpty()) {
            return 0.0;
        }

        return dataList.stream()
                .mapToDouble(data -> {
                    switch (metricType) {
                        case CPU:
                            return data.getCpuUsage();
                        case MEMORY:
                            return data.getMemoryUsage();
                        case DISK:
                            return data.getDiskUsage();
                        case NETWORK:
                            return (data.getNetworkIn() + data.getNetworkOut()) / 2;
                        default:
                            return 0.0;
                    }
                })
                .average()
                .orElse(0.0);
    }

    public List<ScalingRule> getScalingRulesByResource(String resourceId) {
        return dataStore.getScalingRules().values().stream()
                .filter(rule -> rule.getResourceId().equals(resourceId))
                .collect(Collectors.toList());
    }

    public void disableScalingRule(String ruleId) {
        ScalingRule rule = dataStore.getScalingRules().get(ruleId);
        if (rule != null) {
            rule.setEnabled(false);
        }
    }
}
