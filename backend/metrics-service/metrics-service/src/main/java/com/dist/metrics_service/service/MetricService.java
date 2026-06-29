package com.dist.metrics_service.service;

import com.dist.metrics_service.entity.Metric;
import com.dist.metrics_service.entity.RequestStat;
import com.dist.metrics_service.event.ClusterEvent;
import com.dist.metrics_service.event.KvEvent;
import com.dist.metrics_service.repository.MetricRepo;
import com.dist.metrics_service.repository.RequestStatRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricService {

    private final RequestStatRepo requestStatRepository;
    private final MetricRepo metricRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.cluster-service.url:http://localhost:8083}")
    private String clusterServiceUrl;

    @Value("${app.replication-service.url:http://localhost:8082}")
    private String replicationServiceUrl;

    // Java 11+ built-in HTTP client — no extra dependency needed
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    // ─── Summary ──────────────────────────────────────────────────────────────

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("requests", getRequestBreakdown());
        summary.put("cluster", getClusterStats());
        summary.put("replication", getReplicationStats());
        return summary;
    }

    // ─── Request Breakdown ────────────────────────────────────────────────────

    public Map<String, Long> getRequestBreakdown() {
        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (String op : List.of("CREATE", "UPDATE", "DELETE")) {
            breakdown.put(op, requestStatRepository.countByOperation(op));
        }
        return breakdown;
    }

    // ─── Cluster Stats ────────────────────────────────────────────────────────

    public Map<String, Object> getClusterStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try {
            String body = get(clusterServiceUrl + "/api/v1/cluster/status");
            Map<String, String> nodeStatus = objectMapper.readValue(
                    body,
                    new TypeReference<Map<String, String>>() {}
            );
            long nodesUp   = nodeStatus.values().stream().filter("UP"::equals).count();
            long nodesDown = nodeStatus.values().stream().filter("DOWN"::equals).count();
            stats.put("nodesUp", nodesUp);
            stats.put("nodesDown", nodesDown);
            stats.put("nodeDetails", nodeStatus);
        } catch (Exception e) {
            log.warn("Cluster service unreachable: {}", e.getMessage());
            stats.put("nodesUp", 0);
            stats.put("nodesDown", 0);
            stats.put("error", "Cluster service unreachable");
        }
        return stats;
    }

    // ─── Replication Stats ────────────────────────────────────────────────────

    public Map<String, Object> getReplicationStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        try {
            String body = get(replicationServiceUrl + "/api/v1/replication/stats");
            Map<String, Object> result = objectMapper.readValue(
                    body,
                    new TypeReference<Map<String, Object>>() {}
            );
            stats.putAll(result);
        } catch (Exception e) {
            log.warn("Replication service unreachable: {}", e.getMessage());
            stats.put("replicationCompleted", 0);
            stats.put("replicationFailed", 0);
            stats.put("error", "Replication service unreachable");
        }
        return stats;
    }

    // ─── Event Recorders ──────────────────────────────────────────────────────

    public void recordKvEvent(KvEvent event) {
        requestStatRepository.save(
                RequestStat.builder()
                        .operation(event.operation().name())
                        .nodeId(event.nodeId())
                        .key(event.key())
                        .build()
        );
    }

    public void recordClusterEvent(ClusterEvent event) {
        metricRepository.save(
                Metric.builder()
                        .metricName("node.status.changed")
                        .metricValue(event.status().name())
                        .nodeId(event.nodeId())
                        .build()
        );
    }

    public List<Object[]> getRequestsByNode() {
        return requestStatRepository.countGroupedByNodeAndOperation();
    }

    // ─── Internal HTTP helper ─────────────────────────────────────────────────

    private String get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(3))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + " from " + url);
        }
        return response.body();
    }
}