package com.dist.metrics_service.controller;

import com.dist.metrics_service.service.MetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricService metricService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(metricService.getSummary());
    }

    @GetMapping("/requests")
    public ResponseEntity<Map<String, Long>> getRequestBreakdown() {
        return ResponseEntity.ok(metricService.getRequestBreakdown());
    }

    @GetMapping("/requests/by-node")
    public ResponseEntity<List<Object[]>> getRequestsByNode() {
        return ResponseEntity.ok(metricService.getRequestsByNode());
    }

    @GetMapping("/cluster")
    public ResponseEntity<Map<String, Object>> getClusterStats() {
        return ResponseEntity.ok(metricService.getClusterStats());
    }

    @GetMapping("/replication")
    public ResponseEntity<Map<String, Object>> getReplicationStats() {
        return ResponseEntity.ok(metricService.getReplicationStats());
    }
}