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

import com.smartcity.dto.DataPageResponse;
import com.smartcity.model.DataType;
import com.smartcity.service.CityDataQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * REST controller that exposes paginated CityData for the frontend.
 */
@Slf4j
@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@RequiredArgsConstructor
public class DataController {

    private final CityDataQueryService cityDataQueryService;

    @GetMapping
    public ResponseEntity<DataPageResponse> getData(
            @RequestParam(value = "type", required = false) DataType type,
            @RequestParam(value = "sensorId", required = false) String sensorId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        DataPageResponse response = cityDataQueryService.fetchData(type, sensorId, page, size);
        
        // Add cache headers for performance
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
                .eTag(generateETag(response))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        log.info("Fetching record with ID: {}", id);
        
        try {
            Object record = cityDataQueryService.getById(id);
            
            if (record == null) {
                log.warn("Record not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            // Cache individual records for 5 minutes
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                    .body(record);
        } catch (Exception e) {
            log.error("Error fetching record with ID {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body("Error fetching record");
        }
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadById(@PathVariable String id) {
        log.info("Downloading record with ID: {}", id);
        
        try {
            Object record = cityDataQueryService.getById(id);
            
            if (record == null) {
                log.warn("Record not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "record-" + id + ".json");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(record);
        } catch (Exception e) {
            log.error("Error downloading record with ID {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body("Error downloading record");
        }
    }
    
    /**
     * Generate ETag from response hash for cache validation
     */
    private String generateETag(DataPageResponse response) {
        return "\"" + Integer.toHexString(response.hashCode()) + "\"";
    }
}
