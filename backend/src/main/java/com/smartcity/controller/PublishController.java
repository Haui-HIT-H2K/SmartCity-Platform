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

import com.smartcity.dto.MessageResponse;
import com.smartcity.dto.PublishDataRequest;
import com.smartcity.model.CityData;
import com.smartcity.model.DataType;
import com.smartcity.service.DataRoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for publishing data to the Smart City Platform
 * Allows users to push single or batch data records
 */
@Slf4j
@RestController
@RequestMapping("/api/publish")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@RequiredArgsConstructor
public class PublishController {

    private final DataRoutingService dataRoutingService;

    private static final int MAX_BATCH_SIZE = 100;

    /**
     * Publish single data record
     * POST /api/publish
     */
    @PostMapping
    public ResponseEntity<?> publishSingle(@RequestBody PublishDataRequest request) {
        log.info("Publishing single data from source: {}", request.getSourceId());

        // Validate request
        if (request.getSourceId() == null || request.getSourceId().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Source ID is required"));
        }

        if (request.getPayload() == null || request.getPayload().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Payload is required"));
        }

        try {
            // Create CityData from request
            CityData cityData = CityData.builder()
                    .sourceId(request.getSourceId())
                    .payload(request.getPayload())
                    .dataType(request.getDataType()) // Can be null, will be auto-classified
                    .timestamp(System.currentTimeMillis())
                    .build();

            // Route and store the data
            List<CityData> dataList = new ArrayList<>();
            dataList.add(cityData);
            dataRoutingService.routeAndStore(dataList);

            log.info("Successfully published data with ID: {}", cityData.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data published successfully");
            response.put("id", cityData.getId());
            response.put("dataType", cityData.getDataType());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error publishing data: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to publish data: " + e.getMessage()));
        }
    }

    /**
     * Publish batch data records
     * POST /api/publish/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<?> publishBatch(@RequestBody List<PublishDataRequest> requests) {
        log.info("Publishing batch of {} records", requests.size());

        // Validate batch size
        if (requests.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Batch cannot be empty"));
        }

        if (requests.size() > MAX_BATCH_SIZE) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Batch size exceeds maximum limit of " + MAX_BATCH_SIZE));
        }

        try {
            List<CityData> dataList = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (int i = 0; i < requests.size(); i++) {
                PublishDataRequest request = requests.get(i);

                // Validate each record
                if (request.getSourceId() == null || request.getSourceId().isEmpty()) {
                    errors.add("Record " + i + ": Source ID is required");
                    continue;
                }

                if (request.getPayload() == null || request.getPayload().isEmpty()) {
                    errors.add("Record " + i + ": Payload is required");
                    continue;
                }

                // Create CityData from request
                CityData cityData = CityData.builder()
                        .sourceId(request.getSourceId())
                        .payload(request.getPayload())
                        .dataType(request.getDataType())
                        .timestamp(System.currentTimeMillis())
                        .build();

                dataList.add(cityData);
            }

            // Route and store all valid data
            if (!dataList.isEmpty()) {
                dataRoutingService.routeAndStore(dataList);
            }

            log.info("Successfully published {} out of {} records", dataList.size(), requests.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalReceived", requests.size());
            response.put("totalPublished", dataList.size());
            response.put("message", dataList.size() + " records published successfully");

            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error publishing batch data: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to publish batch data: " + e.getMessage()));
        }
    }
}
