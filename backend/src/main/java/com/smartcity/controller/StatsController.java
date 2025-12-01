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
        
        try {
            // 1. Thống kê Redis (HOT data)
            long redisCount = getRedisRecordCount();
            
            // 2. Thống kê MongoDB Warm
            long warmCount = warmMongoTemplate.count(
                    new org.springframework.data.mongodb.core.query.Query(), 
                    CityData.class);
            
            // 3. Thống kê MongoDB Cold
            long coldCount = coldMongoTemplate.count(
                    new org.springframework.data.mongodb.core.query.Query(), 
                    CityData.class);
            
            // Tổng hợp
            long totalCount = redisCount + warmCount + coldCount;
            
            // Build response - FLATTENED format để khớp với frontend
            Map<String, Object> stats = new HashMap<>();
            stats.put("hotCount", redisCount);
            stats.put("warmCount", warmCount);
            stats.put("coldCount", coldCount);
            stats.put("totalCount", totalCount);
            stats.put("incomingRate", metricsService.getIncomingRate());
            stats.put("processedRate", metricsService.getProcessedRate());
            
            log.info("System stats: Total={}, HOT={}, WARM={}, COLD={}", 
                    totalCount, redisCount, warmCount, coldCount);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error fetching system stats: {}", e.getMessage(), e);
            
            // Return default values on error
            Map<String, Object> stats = new HashMap<>();
            stats.put("hotCount", 0);
            stats.put("warmCount", 0);
            stats.put("coldCount", 0);
            stats.put("totalCount", 0);
            stats.put("incomingRate", 0);
            stats.put("processedRate", 0);
            
            return ResponseEntity.ok(stats);
        }
    }

    /**
     * Đếm số lượng keys trong Redis với pattern "hot:citydata:*"
     * 
     * @return Số lượng records trong Redis
     */
    private long getRedisRecordCount() {
        try {
            Set<String> keys = redisTemplate.keys("hot:citydata:*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            log.error("Error counting Redis keys: {}", e.getMessage(), e);
            return 0;
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
