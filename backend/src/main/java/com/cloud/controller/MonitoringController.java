package com.cloud.controller;

import com.cloud.entity.MonitoringData;
import com.cloud.service.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringController {

    @Autowired
    private MonitoringService monitoringService;

    @PostMapping("/report")
    public ResponseEntity<MonitoringData> reportMetrics(@RequestBody Map<String, Object> request) {
        String resourceId = (String) request.get("resourceId");
        Double cpuUsage = ((Number) request.get("cpuUsage")).doubleValue();
        Double memoryUsage = ((Number) request.get("memoryUsage")).doubleValue();
        Double diskUsage = ((Number) request.get("diskUsage")).doubleValue();
        Double networkIn = ((Number) request.get("networkIn")).doubleValue();
        Double networkOut = ((Number) request.get("networkOut")).doubleValue();

        try {
            MonitoringData data = monitoringService.reportMetrics(resourceId, cpuUsage, memoryUsage, 
                    diskUsage, networkIn, networkOut);
            if (data == null) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(data);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/resources/{resourceId}/latest")
    public ResponseEntity<List<MonitoringData>> getLatestMetrics(
            @PathVariable String resourceId,
            @RequestParam(defaultValue = "10") int limit) {
        List<MonitoringData> data = monitoringService.getLatestMetrics(resourceId, limit);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/resources/{resourceId}/range")
    public ResponseEntity<List<MonitoringData>> getMetricsByTimeRange(
            @PathVariable String resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<MonitoringData> data = monitoringService.getMetricsByTimeRange(resourceId, startTime, endTime);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/resources/{resourceId}/average")
    public ResponseEntity<Map<String, Object>> getAverageMetrics(
            @PathVariable String resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        double avgCpu = monitoringService.getAverageCpuUsage(resourceId, startTime, endTime);
        double avgMemory = monitoringService.getAverageMemoryUsage(resourceId, startTime, endTime);

        Map<String, Object> response = new HashMap<>();
        response.put("resourceId", resourceId);
        response.put("startTime", startTime);
        response.put("endTime", endTime);
        response.put("averageCpuUsage", avgCpu);
        response.put("averageMemoryUsage", avgMemory);

        return ResponseEntity.ok(response);
    }
}
