package com.cloud.config;

import com.cloud.entity.CloudResource;
import com.cloud.entity.ResourceStatus;
import com.cloud.service.BillingService;
import com.cloud.service.MonitoringService;
import com.cloud.service.ResourceService;
import com.cloud.service.ScalingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduledTasks {

    @Autowired
    private MonitoringService monitoringService;

    @Autowired
    private ScalingService scalingService;

    @Autowired
    private BillingService billingService;

    @Autowired
    private ResourceService resourceService;

    @Scheduled(fixedRateString = "${monitoring.task.rate:30000}")
    public void reportMonitoringData() {
        List<CloudResource> runningResources = resourceService.getAllResources().stream()
                .filter(r -> r.getStatus() == ResourceStatus.RUNNING)
                .collect(Collectors.toList());

        for (CloudResource resource : runningResources) {
            monitoringService.generateAndReportMetrics(resource.getId());
        }

        System.out.println("监控数据定时上报完成，处理资源数: " + runningResources.size());
    }

    @Scheduled(fixedRateString = "${monitoring.task.rate:30000}")
    public void evaluateScalingRules() {
        scalingService.evaluateScalingRules();
    }

    @Scheduled(fixedRate = 3600000)
    public void generateHourlyUsage() {
        billingService.generateHourlyUsage();
        System.out.println("小时费用生成任务执行完成");
    }

    @Scheduled(cron = "${daily.billing.cron:0 0 2 * * ?}")
    public void generateDailyUsage() {
        billingService.generateDailyUsage();
        System.out.println("每日费用生成任务执行完成");
    }

    @Scheduled(fixedRateString = "${overdue.check.rate:60000}")
    public void checkOverdueAccounts() {
        billingService.checkOverdueAccounts();
    }
}
