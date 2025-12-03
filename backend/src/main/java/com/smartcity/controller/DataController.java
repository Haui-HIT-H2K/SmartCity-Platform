package com.smartcity.controller;

import com.smartcity.dto.DataPageResponse;
import com.smartcity.model.DataType;
import com.smartcity.service.CityDataQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
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
    
    /**
     * Generate ETag from response hash for cache validation
     */
    private String generateETag(DataPageResponse response) {
        return "\"" + Integer.toHexString(response.hashCode()) + "\"";
    }
}

