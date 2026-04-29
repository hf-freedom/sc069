package com.cloud.service;

import com.cloud.entity.CloudResource;
import com.cloud.entity.MonitoringData;
import com.cloud.entity.ResourceStatus;
import com.cloud.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class MonitoringService {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private ResourceService resourceService;

    private final Random random = new Random();

    public MonitoringData reportMetrics(String resourceId, Double cpuUsage, Double memoryUsage, 
                                          Double diskUsage, Double networkIn, Double networkOut) {
        CloudResource resource = resourceService.getResource(resourceId);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }

        if (resource.getStatus() != ResourceStatus.RUNNING) {
            return null;
        }

        MonitoringData data = new MonitoringData();
        data.setId(dataStore.generateId());
        data.setResourceId(resourceId);
        data.setCustomerId(resource.getCustomerId());
        data.setCpuUsage(cpuUsage);
        data.setMemoryUsage(memoryUsage);
        data.setDiskUsage(diskUsage);
        data.setNetworkIn(networkIn);
        data.setNetworkOut(networkOut);
        data.setTimestamp(LocalDateTime.now());

        dataStore.getMonitoringData().put(data.getId(), data);
        dataStore.getMonitoringDataList().add(data);

        return data;
    }

    public MonitoringData generateAndReportMetrics(String resourceId) {
        CloudResource resource = resourceService.getResource(resourceId);
        if (resource == null || resource.getStatus() != ResourceStatus.RUNNING) {
            return null;
        }

        double cpuUsage = 20 + random.nextDouble() * 60;
        double memoryUsage = 30 + random.nextDouble() * 50;
        double diskUsage = 40 + random.nextDouble() * 40;
        double networkIn = random.nextDouble() * 100;
        double networkOut = random.nextDouble() * 80;

        return reportMetrics(resourceId, cpuUsage, memoryUsage, diskUsage, networkIn, networkOut);
    }

    public List<MonitoringData> getLatestMetrics(String resourceId, int limit) {
        return dataStore.getMonitoringDataList().stream()
                .filter(data -> data.getResourceId().equals(resourceId))
                .sorted((d1, d2) -> d2.getTimestamp().compareTo(d1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<MonitoringData> getMetricsByTimeRange(String resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        return dataStore.getMonitoringDataList().stream()
                .filter(data -> data.getResourceId().equals(resourceId)
                        && !data.getTimestamp().isBefore(startTime)
                        && !data.getTimestamp().isAfter(endTime))
                .collect(Collectors.toList());
    }

    public double getAverageCpuUsage(String resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        return dataStore.getMonitoringDataList().stream()
                .filter(data -> data.getResourceId().equals(resourceId)
                        && !data.getTimestamp().isBefore(startTime)
                        && !data.getTimestamp().isAfter(endTime))
                .mapToDouble(MonitoringData::getCpuUsage)
                .average()
                .orElse(0.0);
    }

    public double getAverageMemoryUsage(String resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        return dataStore.getMonitoringDataList().stream()
                .filter(data -> data.getResourceId().equals(resourceId)
                        && !data.getTimestamp().isBefore(startTime)
                        && !data.getTimestamp().isAfter(endTime))
                .mapToDouble(MonitoringData::getMemoryUsage)
                .average()
                .orElse(0.0);
    }
}
