package com.cloud.controller;

import com.cloud.entity.Bill;
import com.cloud.entity.DailyUsage;
import com.cloud.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "*")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @GetMapping("/customers/{customerId}/bills")
    public ResponseEntity<List<Bill>> getBillsByCustomer(@PathVariable String customerId) {
        List<Bill> bills = billingService.getBillsByCustomer(customerId);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/customers/{customerId}/usage")
    public ResponseEntity<List<DailyUsage>> getDailyUsages(
            @PathVariable String customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailyUsage> usages = billingService.getDailyUsagesByCustomer(customerId, startDate, endDate);
        return ResponseEntity.ok(usages);
    }

    @GetMapping("/customers/{customerId}/total-cost")
    public ResponseEntity<Map<String, Object>> getTotalCost(
            @PathVariable String customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal totalCost = billingService.calculateTotalCost(customerId, startDate, endDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("customerId", customerId);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("totalCost", totalCost);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-daily")
    public ResponseEntity<Map<String, String>> generateDailyUsage() {
        billingService.generateDailyUsage();
        Map<String, String> response = new HashMap<>();
        response.put("message", "每日使用记录生成完成");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-overdue")
    public ResponseEntity<Map<String, String>> checkOverdueAccounts() {
        billingService.checkOverdueAccounts();
        Map<String, String> response = new HashMap<>();
        response.put("message", "欠费账户检查完成");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-bill/{customerId}")
    public ResponseEntity<Bill> generateDailyBill(
            @PathVariable String customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Bill bill = billingService.generateDailyBill(customerId, date);
        if (bill == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bill);
    }
}
