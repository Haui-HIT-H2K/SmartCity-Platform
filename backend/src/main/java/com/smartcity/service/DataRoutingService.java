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

import com.smartcity.model.CityData;
import com.smartcity.model.DataType;
import com.smartcity.service.SystemHealthService.HealthStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Data Routing Service
 * Service phân loại và lưu trữ dữ liệu vào các storage tiers:
 * - HOT: Redis (TTL = 1 giờ)
 * - WARM: MongoDB Warm (bulk insert)
 * - COLD: MongoDB Cold (bulk insert)
 */
@Slf4j
@Service
public class DataRoutingService {

    private final MongoTemplate warmMongoTemplate;
    private final MongoTemplate coldMongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SystemHealthService healthService;
    
    // Collection name for deferred data (persistent queue)
    private static final String DEFERRED_COLLECTION = "deferred_data";
    
    @Value("${redis.hot-data.ttl}")
    private long hotDataTtl;

    public DataRoutingService(
            @Qualifier("warmMongoTemplate") MongoTemplate warmMongoTemplate,
            @Qualifier("coldMongoTemplate") MongoTemplate coldMongoTemplate,
            RedisTemplate<String, Object> redisTemplate,
            SystemHealthService healthService) {
        this.warmMongoTemplate = warmMongoTemplate;
        this.coldMongoTemplate = coldMongoTemplate;
        this.redisTemplate = redisTemplate;
        this.healthService = healthService;
    }

    /**
     * Route và lưu trữ một batch dữ liệu
     * Health-aware: Defers COLD/WARM data based on system health status
     * 
     * @param batchData List các CityData cần lưu trữ
     */
    public void routeAndStore(List<CityData> batchData) {
        if (batchData == null || batchData.isEmpty()) {
            log.debug("No data to route and store");
            return;
        }
        
        // Get current health status and allowed data types
        HealthStatus health = healthService.getCurrentHealth();
        Set<DataType> allowedTypes = healthService.getAllowedDataTypes();
        
        log.info("========================================");
        log.info("Starting data routing for {} records", batchData.size());
        log.info("System Health: {} | Allowed types: {}", health, allowedTypes);
        log.info("========================================");
        
        try {
            // Bước 1: Phân loại dữ liệu thành 3 sub-lists
            List<CityData> hotList = new ArrayList<>();
            List<CityData> warmList = new ArrayList<>();
            List<CityData> coldList = new ArrayList<>();
            
            for (CityData data : batchData) {
                // Tạo ID nếu chưa có
                data.generateId();
                
                // Auto-classify nếu dataType null
                if (data.getDataType() == null) {
                    DataType classifiedType = classifyData(data);
                    data.setDataType(classifiedType);
                    log.debug("Auto-classified data {} as {}", data.getId(), classifiedType);
                }
                
                // Phân loại theo dataType
                switch (data.getDataType()) {
                    case HOT:
                        hotList.add(data);
                        break;
                    case WARM:
                        warmList.add(data);
                        break;
                    case COLD:
                        coldList.add(data);
                        break;
                    default:
                        log.warn("Unknown data type: {} for record {}", 
                                data.getDataType(), data.getId());
                }
            }
            
            log.info("Data classification completed: HOT={}, WARM={}, COLD={}", 
                    hotList.size(), warmList.size(), coldList.size());
            
            // Bước 2: Lưu trữ dựa trên health status
            // HOT data luôn được lưu (highest priority)
            storeHotData(hotList);
            
            // WARM data: lưu nếu allowed, nếu không thì defer
            if (allowedTypes.contains(DataType.WARM)) {
                storeWarmData(warmList);
            } else if (!warmList.isEmpty()) {
                deferData(warmList, DataType.WARM);
                log.warn("Health {}: Deferred {} WARM records to persistent queue", 
                        health, warmList.size());
            }
            
            // COLD data: lưu nếu allowed, nếu không thì defer
            if (allowedTypes.contains(DataType.COLD)) {
                storeColdData(coldList);
            } else if (!coldList.isEmpty()) {
                deferData(coldList, DataType.COLD);
                log.warn("Health {}: Deferred {} COLD records to persistent queue", 
                        health, coldList.size());
            }
            
            log.info("========================================");
            log.info("Data routing completed successfully");
            log.info("========================================");
            
        } catch (Exception e) {
            log.error("Error during data routing: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to route and store data", e);
        }
    }

    /**
     * Rule-based classification cho data không có dataType
     * Logic: Dựa vào sensor type và giá trị để phân loại
     */
    private DataType classifyData(CityData data) {
        // Default là WARM
        DataType result = DataType.WARM;
        
        try {
            // Lấy sensor type từ sourceId hoặc payload
            String sourceId = data.getSourceId();
            if (sourceId == null) {
                return DataType.WARM;
            }
            
            // Rule 1: Emergency sensors → HOT
            if (sourceId.toLowerCase().contains("emergency") || 
                sourceId.toLowerCase().contains("alert") ||
                sourceId.toLowerCase().contains("fire") ||
                sourceId.toLowerCase().contains("police")) {
                return DataType.HOT;
            }
            
            // Rule 2: Temperature sensors với ngưỡng cao → HOT
            if (data.getPayload() != null && data.getPayload().containsKey("temperature")) {
                Object tempObj = data.getPayload().get("temperature");
                if (tempObj instanceof Number) {
                    double temp = ((Number) tempObj).doubleValue();
                    if (temp > 35 || temp < 0) {  // Nhiệt độ cực đoan
                        return DataType.HOT;
                    }
                }
            }
            
            // Rule 3: Air quality sensors với chất lượng kém → HOT  
            if (data.getPayload() != null && data.getPayload().containsKey("co2_level")) {
                Object co2Obj = data.getPayload().get("co2_level");
                if (co2Obj instanceof Number) {
                    int co2 = ((Number) co2Obj).intValue();
                    if (co2 > 1000) {  // CO2 cao
                        return DataType.HOT;
                    }
                }
            }
            
            // Rule 4: Traffic sensors → WARM (thường xuyên truy cập)
            if (sourceId.toLowerCase().contains("traffic") ||
                sourceId.toLowerCase().contains("camera") ||
                sourceId.toLowerCase().contains("parking")) {
                return DataType.WARM;
            }
            
            // Rule 5: Historical/Archive sensors → COLD
            if (sourceId.toLowerCase().contains("archive") ||
                sourceId.toLowerCase().contains("history") ||
                sourceId.toLowerCase().contains("logger")) {
                return DataType.COLD;
            }
            
            // Default: WARM
            return DataType.WARM;
            
        } catch (Exception e) {
            log.warn("Error classifying data {}: {}. Using default WARM", 
                    data.getId(), e.getMessage());
            return DataType.WARM;
        }
    }

    /**
     * Lưu HOT data vào Redis với TTL
     * ĐỒNG THỜI backup vào MongoDB Warm để persist sau khi TTL expire
     * 
     * @param hotList List các CityData HOT
     */
    private void storeHotData(List<CityData> hotList) {
        if (hotList.isEmpty()) {
            log.debug("No HOT data to store");
            return;
        }
        
        log.info("Storing {} HOT records to Redis with TTL={}s", hotList.size(), hotDataTtl);
        
        try {
            int storedCount = 0;
            
            // Sử dụng pipeline để tăng hiệu suất
            redisTemplate.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                for (CityData data : hotList) {
                    try {
                        String key = "hot:citydata:" + data.getId();
                        redisTemplate.opsForValue().set(key, data, hotDataTtl, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.error("Error storing HOT data {}: {}", data.getId(), e.getMessage());
                    }
                }
                return null;
            });
            
            storedCount = hotList.size();
            log.info("Successfully stored {} HOT records to Redis", storedCount);
            
            // BACKUP: Lưu HOT data vào MongoDB Warm để persist sau khi Redis expire
            storeHotAsWarmBackup(hotList);
            
        } catch (Exception e) {
            log.error("Error storing HOT data to Redis: {}", e.getMessage(), e);
            // Không throw exception - cho phép WARM/COLD tiếp tục
        }
    }

    /**
     * Backup HOT data vào MongoDB Warm
     * Đảm bảo data không mất khi Redis TTL expire
     */
    private void storeHotAsWarmBackup(List<CityData> hotList) {
        if (hotList.isEmpty()) {
            return;
        }
        
        log.info("Backing up {} HOT records to MongoDB Warm for persistence", hotList.size());
        
        try {
            // Clone và đổi dataType sang WARM
            List<CityData> warmBackup = new ArrayList<>();
            for (CityData data : hotList) {
                CityData warm = new CityData();
                warm.setId(data.getId()); // Giữ nguyên ID
                warm.setSourceId(data.getSourceId());
                warm.setTimestamp(data.getTimestamp());
                warm.setPayload(data.getPayload());
                warm.setDataType(DataType.WARM); // ← Change to WARM
                warmBackup.add(warm);
            }
            
            // Bulk insert vào MongoDB Warm
            BulkOperations bulkOps = warmMongoTemplate.bulkOps(
                    BulkOperations.BulkMode.UNORDERED,
                    CityData.class);
            bulkOps.insert(warmBackup);
            
            com.mongodb.bulk.BulkWriteResult result = bulkOps.execute();
            
            log.info("Successfully backed up {} HOT→WARM records to MongoDB Warm (inserted={})", 
                    warmBackup.size(), result.getInsertedCount());
            
        } catch (Exception e) {
            log.error("Error backing up HOT data to MongoDB Warm: {}", e.getMessage(), e);
            // Log error nhưng không throw - Redis insert đã thành công
        }
    }

    /**
     * Lưu WARM data vào MongoDB Warm sử dụng Bulk Insert
     * 
     * @param warmList List các CityData WARM
     */
    private void storeWarmData(List<CityData> warmList) {
        if (warmList.isEmpty()) {
            log.debug("No WARM data to store");
            return;
        }
        
        log.info("Storing {} WARM records to MongoDB Warm using Bulk Insert", warmList.size());
        
        try {
            // Sử dụng BulkOperations để insert cả batch một lúc
            BulkOperations bulkOps = warmMongoTemplate.bulkOps(
                    BulkOperations.BulkMode.UNORDERED, 
                    CityData.class);
            
            bulkOps.insert(warmList);
            
            com.mongodb.bulk.BulkWriteResult result = bulkOps.execute();
            
            log.info("Successfully bulk inserted {} WARM records to MongoDB Warm (inserted={})", 
                    warmList.size(), result.getInsertedCount());
            
        } catch (Exception e) {
            log.error("Error bulk inserting WARM data to MongoDB: {}", e.getMessage(), e);
            // Không throw exception - cho phép COLD tiếp tục
        }
    }

    /**
     * Lưu COLD data vào MongoDB Cold sử dụng Bulk Insert
     * 
     * @param coldList List các CityData COLD
     */
    private void storeColdData(List<CityData> coldList) {
        if (coldList.isEmpty()) {
            log.debug("No COLD data to store");
            return;
        }
        
        log.info("Storing {} COLD records to MongoDB Cold using Bulk Insert", coldList.size());
        
        try {
            // Sử dụng BulkOperations để insert cả batch một lúc
            BulkOperations bulkOps = coldMongoTemplate.bulkOps(
                    BulkOperations.BulkMode.UNORDERED, 
                    CityData.class);
            
            bulkOps.insert(coldList);
            
            com.mongodb.bulk.BulkWriteResult result = bulkOps.execute();
            
            log.info("Successfully bulk inserted {} COLD records to MongoDB Cold (inserted={})", 
                    coldList.size(), result.getInsertedCount());
            
        } catch (Exception e) {
            log.error("Error bulk inserting COLD data to MongoDB: {}", e.getMessage(), e);
            // Log error nhưng không throw
        }
    }

    /**
     * Defer data to persistent MongoDB queue when health is degraded/down
     * Data will be processed when health recovers
     * 
     * @param dataList List of CityData to defer
     * @param originalType Original DataType for later routing
     */
    private void deferData(List<CityData> dataList, DataType originalType) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        
        try {
            for (CityData data : dataList) {
                // Keep original type for later processing
                data.setDataType(originalType);
            }
            
            // Use bulk insert for performance
            BulkOperations bulkOps = warmMongoTemplate.bulkOps(
                    BulkOperations.BulkMode.UNORDERED,
                    CityData.class,
                    DEFERRED_COLLECTION);
            bulkOps.insert(dataList);
            bulkOps.execute();
            
            log.info("Deferred {} {} records to persistent queue '{}'", 
                    dataList.size(), originalType, DEFERRED_COLLECTION);
                    
        } catch (Exception e) {
            log.error("Error deferring {} data: {}", originalType, e.getMessage(), e);
            // Critical: Try to save individually as fallback
            for (CityData data : dataList) {
                try {
                    warmMongoTemplate.save(data, DEFERRED_COLLECTION);
                } catch (Exception ex) {
                    log.error("Failed to defer data {}: {}", data.getId(), ex.getMessage());
                }
            }
        }
    }

    /**
     * Scheduled task to process deferred data when health recovers
     * Runs every 30 seconds
     */
    @Scheduled(fixedRate = 30000)
    public void processDeferredData() {
        try {
            HealthStatus health = healthService.getCurrentHealth();
            
            // Count deferred documents
            long deferredCount = warmMongoTemplate.count(new Query(), DEFERRED_COLLECTION);
            
            if (deferredCount == 0) {
                // No deferred data, nothing to do
                return;
            }
            
            log.info("Checking deferred data: {} records pending, Health: {}", deferredCount, health);
            
            if (health == HealthStatus.UP) {
                // Process ALL deferred data (COLD + WARM)
                List<CityData> deferred = warmMongoTemplate.findAll(CityData.class, DEFERRED_COLLECTION);
                
                if (!deferred.isEmpty()) {
                    log.info("Health UP - Processing {} deferred records", deferred.size());
                    
                    // Separate by type
                    List<CityData> warmDeferred = deferred.stream()
                            .filter(d -> d.getDataType() == DataType.WARM)
                            .collect(Collectors.toList());
                    List<CityData> coldDeferred = deferred.stream()
                            .filter(d -> d.getDataType() == DataType.COLD)
                            .collect(Collectors.toList());
                    
                    // Store to appropriate tiers
                    if (!warmDeferred.isEmpty()) {
                        storeWarmData(warmDeferred);
                    }
                    if (!coldDeferred.isEmpty()) {
                        storeColdData(coldDeferred);
                    }
                    
                    // Clear deferred collection
                    warmMongoTemplate.dropCollection(DEFERRED_COLLECTION);
                    log.info("Successfully processed deferred data: {} WARM, {} COLD", 
                            warmDeferred.size(), coldDeferred.size());
                }
                
            } else if (health == HealthStatus.DEGRADED) {
                // Only process deferred WARM data (COLD storage still unavailable)
                Query warmQuery = Query.query(Criteria.where("dataType").is(DataType.WARM.name()));
                List<CityData> deferredWarm = warmMongoTemplate.find(warmQuery, CityData.class, DEFERRED_COLLECTION);
                
                if (!deferredWarm.isEmpty()) {
                    log.info("Health DEGRADED - Processing {} deferred WARM records", deferredWarm.size());
                    storeWarmData(deferredWarm);
                    warmMongoTemplate.remove(warmQuery, DEFERRED_COLLECTION);
                    log.info("Processed {} deferred WARM records", deferredWarm.size());
                }
            }
            // If DOWN: Do nothing, keep data in persistent queue
            
        } catch (Exception e) {
            log.error("Error processing deferred data: {}", e.getMessage(), e);
            // Will retry on next scheduled run
        }
    }
}
