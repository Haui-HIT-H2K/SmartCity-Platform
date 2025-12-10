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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcity.model.CityData;
import com.smartcity.model.DataType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * ML Service Client
 * Service gọi ML Service để xác định dataType (HOT/WARM/COLD)
 */
@Slf4j
@Service
public class MLServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${ml.service.url:http://smart-city-ml:8000}")
    private String mlServiceUrl;
    
    public MLServiceClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Classify CityData bằng ML Service
     * 
     * @param cityData CityData cần classify
     * @return DataType (HOT/WARM/COLD)
     */
    public DataType classifyData(CityData cityData) {
        try {
            // Extract sensor values từ payload
            Map<String, Object> payload = cityData.getPayload();
            if (payload == null || payload.isEmpty()) {
                log.warn("Empty payload for data {}, defaulting to COLD", cityData.getId());
                return DataType.COLD;
            }

            // Determine metric type và value
            String metricType = null;
            Double value = null;

            if (payload.containsKey("temperature")) {
                metricType = "temperature";
                value = getDoubleValue(payload.get("temperature"));
            } else if (payload.containsKey("humidity")) {
                metricType = "humidity";
                value = getDoubleValue(payload.get("humidity"));
            } else if (payload.containsKey("co2_level")) {
                metricType = "co2";
                value = getDoubleValue(payload.get("co2_level"));
            } else if (payload.containsKey("co2")) {
                metricType = "co2";
                value = getDoubleValue(payload.get("co2"));
            }

            if (metricType == null || value == null) {
                log.warn("No valid sensor metric found in payload for {}, defaulting to COLD", cityData.getId());
                return DataType.COLD;
            }

            // Call ML Service
            return callMLService(metricType, value);
            
        } catch (Exception e) {
            log.error("Error classifying data {}: {}", cityData.getId(), e.getMessage());
            // Fallback to COLD on error
            return DataType.COLD;
        }
    }

    /**
     * Call ML Service /predict endpoint
     */
    private DataType callMLService(String metricType, Double value) {
        try {
            String endpoint = mlServiceUrl + "/predict";
            
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("source", "sensor");
            requestBody.put("metric_type", metricType);
            requestBody.put("value", value);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Call ML Service
            ResponseEntity<Map> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String label = (String) responseBody.get("label");
                
                // Map label to DataType
                if ("HOT".equals(label)) {
                    return DataType.HOT;
                } else if ("WARM".equals(label)) {
                    return DataType.WARM;
                } else {
                    return DataType.COLD; // "COLD" or default
                }
            } else {
                log.warn("ML Service returned non-OK status: {}", response.getStatusCode());
                return DataType.COLD;
            }
            
        } catch (Exception e) {
            log.error("Error calling ML Service for {} = {}: {}", metricType, value, e.getMessage());
            return DataType.COLD; // Fallback
        }
    }

    /**
     * Helper method để extract Double từ Object
     */
    private Double getDoubleValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Batch classify multiple CityData using ML Service batch endpoint.
     * Optimized for high-throughput ingestion.
     * 
     * @param dataList List of CityData to classify
     * @return List of DataType (same order as input)
     */
    public java.util.List<DataType> classifyDataBatch(java.util.List<CityData> dataList) {
        java.util.List<DataType> results = new java.util.ArrayList<>();
        
        if (dataList == null || dataList.isEmpty()) {
            return results;
        }
        
        try {
            // Build batch request items
            java.util.List<Map<String, Object>> items = new java.util.ArrayList<>();
            
            for (CityData cityData : dataList) {
                Map<String, Object> payload = cityData.getPayload();
                if (payload == null || payload.isEmpty()) {
                    items.add(null); // Mark as null for later
                    continue;
                }
                
                // Extract metric type and value
                String metricType = null;
                Double value = null;
                
                if (payload.containsKey("temperature")) {
                    metricType = "temperature";
                    value = getDoubleValue(payload.get("temperature"));
                } else if (payload.containsKey("humidity")) {
                    metricType = "humidity";
                    value = getDoubleValue(payload.get("humidity"));
                } else if (payload.containsKey("co2_level")) {
                    metricType = "co2";
                    value = getDoubleValue(payload.get("co2_level"));
                } else if (payload.containsKey("co2")) {
                    metricType = "co2";
                    value = getDoubleValue(payload.get("co2"));
                }
                
                if (metricType != null && value != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("metric_type", metricType);
                    item.put("value", value);
                    items.add(item);
                } else {
                    items.add(null);
                }
            }
            
            // Filter out nulls for batch request, track indices
            java.util.List<Map<String, Object>> validItems = new java.util.ArrayList<>();
            java.util.List<Integer> validIndices = new java.util.ArrayList<>();
            
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) != null) {
                    validItems.add(items.get(i));
                    validIndices.add(i);
                }
            }
            
            // Initialize results with COLD defaults
            for (int i = 0; i < dataList.size(); i++) {
                results.add(DataType.COLD);
            }
            
            if (validItems.isEmpty()) {
                log.warn("No valid items to classify in batch");
                return results;
            }
            
            // Call ML batch endpoint
            String endpoint = mlServiceUrl + "/predict/batch";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("items", validItems);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                java.util.List<Map<String, Object>> batchResults = 
                        (java.util.List<Map<String, Object>>) responseBody.get("results");
                
                if (batchResults != null && batchResults.size() == validItems.size()) {
                    for (int i = 0; i < batchResults.size(); i++) {
                        Map<String, Object> result = batchResults.get(i);
                        String label = (String) result.get("label");
                        int originalIndex = validIndices.get(i);
                        
                        // Map label to DataType
                        if ("HOT".equals(label)) {
                            results.set(originalIndex, DataType.HOT);
                        } else if ("WARM".equals(label)) {
                            results.set(originalIndex, DataType.WARM);
                        }
                        // COLD is already default
                    }
                }
                
                log.debug("Batch classified {} items successfully", validItems.size());
            } else {
                log.warn("ML batch service returned non-OK status: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error in batch classification: {}", e.getMessage());
            // Results already initialized with COLD defaults
        }
        
        return results;
    }
}
