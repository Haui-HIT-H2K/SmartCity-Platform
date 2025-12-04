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
}
