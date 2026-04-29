package com.cloud.controller;

import com.cloud.entity.Customer;
import com.cloud.entity.ResourceQuota;
import com.cloud.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomer(@PathVariable String customerId) {
        Customer customer = customerService.getCustomer(customerId);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String email = (String) request.get("email");
        BigDecimal initialBalance = request.containsKey("initialBalance") 
                ? new BigDecimal(request.get("initialBalance").toString()) 
                : BigDecimal.ZERO;
        BigDecimal creditLimit = request.containsKey("creditLimit") 
                ? new BigDecimal(request.get("creditLimit").toString()) 
                : new BigDecimal("5000.00");

        Customer customer = customerService.createCustomer(name, email, initialBalance, creditLimit);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/{customerId}/quota")
    public ResponseEntity<ResourceQuota> getCustomerQuota(@PathVariable String customerId) {
        ResourceQuota quota = customerService.getCustomerQuota(customerId);
        if (quota == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(quota);
    }

    @PutMapping("/{customerId}/quota")
    public ResponseEntity<?> updateQuota(@PathVariable String customerId, @RequestBody ResourceQuota quota) {
        try {
            customerService.updateQuota(customerId, quota);
            Map<String, String> response = new HashMap<>();
            response.put("message", "配额更新成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{customerId}/recharge")
    public ResponseEntity<Map<String, Object>> recharge(@PathVariable String customerId, 
                                                          @RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        customerService.addBalance(customerId, amount);
        
        Customer customer = customerService.getCustomer(customerId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "充值成功");
        response.put("newBalance", customer.getAccountBalance());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable String customerId) {
        Customer customer = customerService.getCustomer(customerId);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("accountBalance", customer.getAccountBalance());
        response.put("creditLimit", customer.getCreditLimit());
        response.put("totalAvailable", customer.getAccountBalance().add(customer.getCreditLimit()));
        response.put("status", customer.getStatus());
        
        return ResponseEntity.ok(response);
    }
}
