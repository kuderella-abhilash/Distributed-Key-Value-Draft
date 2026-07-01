package com.dist.api_gateway.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardStatsController {

    private final WebClient kvClient = WebClient.builder()
            .baseUrl("http://localhost:8081")
            .build();

    private final AtomicInteger requestCount = new AtomicInteger(0);
    private long windowStart = System.currentTimeMillis();

    // ── /api/v1/dashboard/cache-stats ─────────────────────────────
    // kv-service has no cache stats endpoint yet → always returns 0
    @GetMapping("/cache-stats")
    public Mono<Map<String, Object>> cacheStats() {
        Map<String, Object> body = new HashMap<>();
        body.put("hitRate", 0.0);
        body.put("hits", 0);
        body.put("misses", 0);
        return Mono.just(body);
    }

    // ── /api/v1/dashboard/store-meta ──────────────────────────────
    // Calls GET /api/v1/keys?page=0&pageSize=1000 on kv-service
    // and counts the returned array to get real totalKeys
    @GetMapping("/store-meta")
    public Mono<Map<String, Object>> storeMeta() {
        return kvClient.get()
                .uri("/api/v1/keys?page=0&pageSize=1000")
                .retrieve()
                .bodyToMono(List.class)
                .map(list -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("totalKeys", list != null ? list.size() : 0);
                    body.put("replicationLagMs", 0);
                    return (Map<String, Object>) body;
                })
                .onErrorResume(e -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("totalKeys", 0);
                    body.put("replicationLagMs", 0);
                    return Mono.just(body);
                });
    }

    // ── /api/v1/dashboard/kafka-stats ─────────────────────────────
    // Try replication-service at 8082 for kafka topic data
    @GetMapping("/kafka-stats")
    public Mono<Map<String, Object>> kafkaStats() {
        return WebClient.builder()
                .baseUrl("http://localhost:8082")
                .build()
                .get()
                .uri("/api/v1/replication/kafka/stats")
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("topics",        res.getOrDefault("topics", new ArrayList<>()));
                    body.put("totalMessages", res.getOrDefault("totalMessages", 0));
                    body.put("totalRate",     res.getOrDefault("totalRate", 0));
                    return (Map<String, Object>) body;
                })
                .onErrorResume(e -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("topics", new ArrayList<>());
                    body.put("totalMessages", 0);
                    body.put("totalRate", 0);
                    return Mono.just(body);
                });
    }

    // ── /api/v1/dashboard/rps ─────────────────────────────────────
    @GetMapping("/rps")
    public Mono<Map<String, Object>> rps() {
        long now = System.currentTimeMillis();
        double elapsedSeconds = Math.max((now - windowStart) / 1000.0, 1.0);
        int count = requestCount.getAndSet(0);
        windowStart = now;

        Map<String, Object> body = new HashMap<>();
        body.put("value", Math.round(count / elapsedSeconds));
        return Mono.just(body);
    }
}