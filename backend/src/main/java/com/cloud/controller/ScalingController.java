package com.cloud.controller;

import com.cloud.entity.MetricType;
import com.cloud.entity.ScalingRule;
import com.cloud.entity.ScalingType;
import com.cloud.service.ScalingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scaling")
@CrossOrigin(origins = "*")
public class ScalingController {

    @Autowired
    private ScalingService scalingService;

    @PostMapping("/rules")
    public ResponseEntity<ScalingRule> createScalingRule(@RequestBody Map<String, Object> request) {
        String resourceId = (String) request.get("resourceId");
        String customerId = (String) request.get("customerId");
        ScalingType type = ScalingType.valueOf((String) request.get("type"));
        MetricType metricType = MetricType.valueOf((String) request.get("metricType"));
        Double threshold = ((Number) request.get("threshold")).doubleValue();
        Integer adjustmentAmount = (Integer) request.get("adjustmentAmount");
        Integer cooldownSeconds = request.containsKey("cooldownSeconds") 
                ? (Integer) request.get("cooldownSeconds") 
                : 300;

        try {
            ScalingRule rule = scalingService.createScalingRule(resourceId, customerId, type, metricType,
                    threshold, adjustmentAmount, cooldownSeconds);
            return ResponseEntity.ok(rule);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/resources/{resourceId}/rules")
    public ResponseEntity<List<ScalingRule>> getScalingRulesByResource(@PathVariable String resourceId) {
        List<ScalingRule> rules = scalingService.getScalingRulesByResource(resourceId);
        return ResponseEntity.ok(rules);
    }

    @PutMapping("/rules/{ruleId}/disable")
    public ResponseEntity<Map<String, String>> disableScalingRule(@PathVariable String ruleId) {
        scalingService.disableScalingRule(ruleId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "扩缩容规则已禁用");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/evaluate")
    public ResponseEntity<Map<String, String>> evaluateScalingRules() {
        scalingService.evaluateScalingRules();
        Map<String, String> response = new HashMap<>();
        response.put("message", "扩缩容规则评估完成");
        return ResponseEntity.ok(response);
    }
}
