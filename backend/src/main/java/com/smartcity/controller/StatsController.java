SPDX-License-Identifier: Apache-2.0
/*

 * Copyright 2025 Haui.HIT - H2K

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */

package com.smartcity.controller;

import com.smartcity.model.CityData;
import com.smartcity.service.MetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stats Controller
 * Controller hiển thị thống kê số liệu trong hệ thống
 */
@Slf4j
@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class StatsController {

    private final MongoTemplate warmMongoTemplate;
    private final MongoTemplate coldMongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MetricsService metricsService;

    public StatsController(
            @Qualifier("warmMongoTemplate") MongoTemplate warmMongoTemplate,
            @Qualifier("coldMongoTemplate") MongoTemplate coldMongoTemplate,
            RedisTemplate<String, Object> redisTemplate,
            MetricsService metricsService) {
        this.warmMongoTemplate = warmMongoTemplate;
        this.coldMongoTemplate = coldMongoTemplate;
        this.redisTemplate = redisTemplate;
        this.metricsService = metricsService;
    }

    /**
     * API: GET /api/stats
     * Trả về thống kê số lượng bản ghi trong các storage tiers
     * Response format khớp với frontend SystemStats interface
     * 
     * @return Response với số liệu thống kê
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        log.info("Fetching system statistics");
        
        long redisCount = 0;
        long warmCount = 0;
        long coldCount = 0;
        
        // 1. Thống kê Redis (HOT data) - try separately
        try {
            redisCount = getRedisRecordCount();
            log.info("Redis count retrieved: {}", redisCount);
        } catch (Exception e) {
            log.error("Error counting Redis keys: {}", e.getMessage(), e);
            // Continue with redisCount = 0
        }
        
        // 2. Thống kê MongoDB Warm - try separately
        try {
            warmCount = warmMongoTemplate.count(
                    new org.springframework.data.mongodb.core.query.Query(), 
                    CityData.class);
            log.info("Warm MongoDB count retrieved: {}", warmCount);
        } catch (Exception e) {
            log.warn("MongoDB Warm not available: {}", e.getMessage());
            // Continue with warmCount = 0
        }
        
        // 3. Thống kê MongoDB Cold - try separately
        try {
            coldCount = coldMongoTemplate.count(
                    new org.springframework.data.mongodb.core.query.Query(), 
                    CityData.class);
            log.info("Cold MongoDB count retrieved: {}", coldCount);
        } catch (Exception e) {
            log.warn("MongoDB Cold not available: {}", e.getMessage());
            // Continue with coldCount = 0
        }
        
        // Tổng hợp
        long totalCount = redisCount + warmCount + coldCount;
        
        // Build response - FLATTENED format để khớp với frontend
        Map<String, Object> stats = new HashMap<>();
        stats.put("hotCount", redisCount);
        stats.put("warmCount", warmCount);
        stats.put("coldCount", coldCount);
        stats.put("totalCount", totalCount);
        
        // Get metrics rates safely
        try {
            stats.put("incomingRate", metricsService.getIncomingRate());
            stats.put("processedRate", metricsService.getProcessedRate());
            
            // Add rate history for chart visualization
            List<Map<String, Object>> rateHistory = metricsService.getRateHistory().stream()
                    .map(snapshot -> {
                        Map<String, Object> point = new HashMap<>();
                        point.put("timestamp", snapshot.getTimestamp());
                        point.put("incomingRate", snapshot.getIncomingRate());
                        point.put("processedRate", snapshot.getProcessedRate());
                        return point;
                    })
                    .collect(java.util.stream.Collectors.toList());
            stats.put("rateHistory", rateHistory);
            
        } catch (Exception e) {
            log.warn("Error getting metrics: {}", e.getMessage());
            stats.put("incomingRate", 0);
            stats.put("processedRate", 0);
            stats.put("rateHistory", java.util.Collections.emptyList());
        }
        
        log.info("System stats: Total={}, HOT={}, WARM={}, COLD={}, History size={}", 
                totalCount, redisCount, warmCount, coldCount, 
                stats.containsKey("rateHistory") ? ((List<?>)stats.get("rateHistory")).size() : 0);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Đếm số lượng keys trong Redis với pattern "hot:citydata:*"
     * Sử dụng SCAN thay vì KEYS để tránh block Redis
     * 
     * @return Số lượng records trong Redis
     */
    private long getRedisRecordCount() {
        try {
            log.debug("Attempting to count Redis keys with pattern: hot:citydata:*");
            
            // Use execute with RedisCallback for better control
            Long count = redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Long>) connection -> {
                long keyCount = 0;
                
                // Use SCAN instead of KEYS for better performance
                org.springframework.data.redis.core.ScanOptions options = 
                    org.springframework.data.redis.core.ScanOptions.scanOptions()
                        .match("hot:citydata:*")
                        .count(1000)
                        .build();
                
                org.springframework.data.redis.core.Cursor<byte[]> cursor = 
                    connection.scan(options);
                
                while (cursor.hasNext()) {
                    cursor.next();
                    keyCount++;
                }
                
                cursor.close();
                return keyCount;
            });
            
            log.info("Redis key count for pattern 'hot:citydata:*': {}", count);
            return count != null ? count : 0;
            
        } catch (Exception e) {
            log.error("Error counting Redis keys: {}", e.getMessage(), e);
            
            // Fallback: Try simple keys() method
            try {
                log.warn("Attempting fallback method using keys()");
                Set<String> keys = redisTemplate.keys("hot:citydata:*");
                long count = keys != null ? keys.size() : 0;
                log.info("Fallback key count: {}", count);
                return count;
            } catch (Exception e2) {
                log.error("Fallback method also failed: {}", e2.getMessage(), e2);
                return 0;
            }
        }
    }

    /**
     * API: GET /api/stats/health
     * Health check endpoint
     * 
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Kiểm tra kết nối các storage
            boolean redisOk = checkRedisConnection();
            boolean mongoWarmOk = checkMongoConnection(warmMongoTemplate);
            boolean mongoColdOk = checkMongoConnection(coldMongoTemplate);
            
            Map<String, String> connections = new HashMap<>();
            connections.put("redis", redisOk ? "OK" : "ERROR");
            connections.put("mongodb_warm", mongoWarmOk ? "OK" : "ERROR");
            connections.put("mongodb_cold", mongoColdOk ? "OK" : "ERROR");
            
            boolean allOk = redisOk && mongoWarmOk && mongoColdOk;
            
            health.put("status", allOk ? "UP" : "DEGRADED");
            health.put("timestamp", LocalDateTime.now().toString());
            health.put("connections", connections);
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(health);
        }
    }

    private boolean checkRedisConnection() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            log.error("Redis connection check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkMongoConnection(MongoTemplate mongoTemplate) {
        try {
            mongoTemplate.getDb().listCollectionNames();
            return true;
        } catch (Exception e) {
            log.error("MongoDB connection check failed: {}", e.getMessage());
            return false;
        }
    }
}
