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

package com.smartcity.service;

import com.smartcity.model.DataType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

/**
 * System Health Service
 * Centralized service for checking system health and determining allowed data types
 * based on current health status.
 */
@Slf4j
@Service
public class SystemHealthService {

    /**
     * Health status enum
     */
    public enum HealthStatus {
        UP,       // All systems operational
        DEGRADED, // Some systems down (e.g., MongoDB Cold unavailable)
        DOWN      // Critical systems down (e.g., Redis unavailable)
    }

    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate warmMongoTemplate;
    private final MongoTemplate coldMongoTemplate;

    public SystemHealthService(
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier("warmMongoTemplate") MongoTemplate warmMongoTemplate,
            @Qualifier("coldMongoTemplate") MongoTemplate coldMongoTemplate) {
        this.redisTemplate = redisTemplate;
        this.warmMongoTemplate = warmMongoTemplate;
        this.coldMongoTemplate = coldMongoTemplate;
    }

    /**
     * Get current system health status
     * 
     * Logic:
     * - UP: All connections OK (Redis + MongoDB Warm + MongoDB Cold)
     * - DEGRADED: Redis and MongoDB Warm OK, but MongoDB Cold failed
     * - DOWN: Redis or MongoDB Warm failed (critical services)
     * 
     * @return Current HealthStatus
     */
    public HealthStatus getCurrentHealth() {
        try {
            boolean redisOk = checkRedisConnection();
            boolean mongoWarmOk = checkMongoConnection(warmMongoTemplate);
            boolean mongoColdOk = checkMongoConnection(coldMongoTemplate);

            if (redisOk && mongoWarmOk && mongoColdOk) {
                return HealthStatus.UP;
            } else if (redisOk && mongoWarmOk) {
                // Only MongoDB Cold is down - system is degraded but operational
                log.warn("System DEGRADED: MongoDB Cold unavailable");
                return HealthStatus.DEGRADED;
            } else {
                // Critical services down
                log.error("System DOWN: Redis={}, MongoDB Warm={}", redisOk, mongoWarmOk);
                return HealthStatus.DOWN;
            }
        } catch (Exception e) {
            log.error("Error checking system health: {}", e.getMessage());
            return HealthStatus.DOWN;
        }
    }

    /**
     * Get allowed data types based on current health status
     * 
     * Rules:
     * - UP: All types allowed (HOT, WARM, COLD)
     * - DEGRADED: Only HOT and WARM allowed (COLD deferred)
     * - DOWN: Only HOT allowed (WARM and COLD deferred)
     * 
     * @return Set of allowed DataType values
     */
    public Set<DataType> getAllowedDataTypes() {
        HealthStatus status = getCurrentHealth();
        
        switch (status) {
            case UP:
                return EnumSet.of(DataType.HOT, DataType.WARM, DataType.COLD);
            case DEGRADED:
                log.info("Health DEGRADED: Only HOT and WARM data types allowed");
                return EnumSet.of(DataType.HOT, DataType.WARM);
            case DOWN:
                log.warn("Health DOWN: Only HOT data type allowed");
                return EnumSet.of(DataType.HOT);
            default:
                return EnumSet.of(DataType.HOT); // Safest fallback
        }
    }

    /**
     * Check if a specific data type is currently allowed
     * 
     * @param dataType The data type to check
     * @return true if allowed, false if should be deferred
     */
    public boolean isDataTypeAllowed(DataType dataType) {
        return getAllowedDataTypes().contains(dataType);
    }

    /**
     * Check Redis connection
     */
    private boolean checkRedisConnection() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            log.error("Redis connection check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check MongoDB connection
     */
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
