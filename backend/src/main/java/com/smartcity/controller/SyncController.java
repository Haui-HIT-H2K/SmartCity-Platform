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

import com.smartcity.service.RabbitMQIngestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Sync Controller
 * Controller cho việc kích hoạt thủ công việc đồng bộ dữ liệu
 */
@Slf4j
@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class SyncController {

    private final RabbitMQIngestionService ingestionService;

    public SyncController(RabbitMQIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * API: POST /api/sync/trigger
     * Kích hoạt thủ công việc kéo dữ liệu từ RabbitMQ
     * 
     * Mục đích: Demo, testing, hoặc manual sync khi cần
     * 
     * @return Response với status và timestamp
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerManualSync() {
        log.info("========================================");
        log.info("Received manual trigger request");
        log.info("========================================");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Kích hoạt pull dữ liệu
            ingestionService.manualTriggerPull();
            
            response.put("status", "success");
            response.put("message", "Manual data pull triggered successfully");
            response.put("timestamp", LocalDateTime.now().toString());
            
            log.info("Manual trigger completed successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during manual trigger: {}", e.getMessage(), e);
            
            response.put("status", "error");
            response.put("message", "Failed to trigger manual pull: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
