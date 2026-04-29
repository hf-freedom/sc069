package com.cloud.controller;

import com.cloud.entity.CloudResource;
import com.cloud.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @GetMapping
    public ResponseEntity<List<CloudResource>> getAllResources(
            @RequestParam(required = false) String customerId) {
        if (customerId != null) {
            return ResponseEntity.ok(resourceService.getResourcesByCustomer(customerId));
        }
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<CloudResource> getResource(@PathVariable String resourceId) {
        CloudResource resource = resourceService.getResource(resourceId);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resource);
    }

    @PostMapping("/vm")
    public ResponseEntity<CloudResource> createVm(@RequestBody Map<String, Object> request) {
        String customerId = (String) request.get("customerId");
        String name = (String) request.get("name");
        Integer cpuCores = (Integer) request.get("cpuCores");
        Integer memoryGB = (Integer) request.get("memoryGB");

        try {
            CloudResource resource = resourceService.createVm(customerId, name, cpuCores, memoryGB);
            return ResponseEntity.ok(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/storage")
    public ResponseEntity<CloudResource> createStorage(@RequestBody Map<String, Object> request) {
        String customerId = (String) request.get("customerId");
        String name = (String) request.get("name");
        Integer storageGB = (Integer) request.get("storageGB");

        try {
            CloudResource resource = resourceService.createStorage(customerId, name, storageGB);
            return ResponseEntity.ok(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/bandwidth")
    public ResponseEntity<CloudResource> createBandwidth(@RequestBody Map<String, Object> request) {
        String customerId = (String) request.get("customerId");
        String name = (String) request.get("name");
        Integer bandwidthMbps = (Integer) request.get("bandwidthMbps");

        try {
            CloudResource resource = resourceService.createBandwidth(customerId, name, bandwidthMbps);
            return ResponseEntity.ok(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/database")
    public ResponseEntity<CloudResource> createDatabase(@RequestBody Map<String, Object> request) {
        String customerId = (String) request.get("customerId");
        String name = (String) request.get("name");
        String dbType = (String) request.get("dbType");
        Integer dbVersion = (Integer) request.get("dbVersion");
        Integer storageGB = (Integer) request.get("storageGB");

        try {
            CloudResource resource = resourceService.createDatabase(customerId, name, dbType, dbVersion, storageGB);
            return ResponseEntity.ok(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{resourceId}/start")
    public ResponseEntity<Map<String, String>> startResource(@PathVariable String resourceId) {
        try {
            resourceService.startResource(resourceId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "资源启动成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{resourceId}/stop")
    public ResponseEntity<Map<String, String>> stopResource(@PathVariable String resourceId) {
        try {
            resourceService.stopResource(resourceId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "资源停止成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Map<String, String>> releaseResource(@PathVariable String resourceId) {
        try {
            resourceService.releaseResource(resourceId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "资源释放成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{resourceId}")
    public ResponseEntity<?> updateResource(@PathVariable String resourceId, 
                                             @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            Integer cpuCores = request.containsKey("cpuCores") ? (Integer) request.get("cpuCores") : null;
            Integer memoryGB = request.containsKey("memoryGB") ? (Integer) request.get("memoryGB") : null;
            Integer storageGB = request.containsKey("storageGB") ? (Integer) request.get("storageGB") : null;
            Integer bandwidthMbps = request.containsKey("bandwidthMbps") ? (Integer) request.get("bandwidthMbps") : null;
            Integer minCapacity = request.containsKey("minCapacity") ? (Integer) request.get("minCapacity") : null;
            Integer maxCapacity = request.containsKey("maxCapacity") ? (Integer) request.get("maxCapacity") : null;

            CloudResource updatedResource = resourceService.updateResource(
                    resourceId, name, cpuCores, memoryGB, storageGB, 
                    bandwidthMbps, minCapacity, maxCapacity);
            
            return ResponseEntity.ok(updatedResource);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
