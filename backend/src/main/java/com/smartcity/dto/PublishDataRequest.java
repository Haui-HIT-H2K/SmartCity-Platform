package com.smartcity.dto;

import com.smartcity.model.DataType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for publishing data to the system
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublishDataRequest {
    
    /**
     * Source ID / Sensor ID (required)
     */
    private String sourceId;
    
    /**
     * Sensor data payload (required)
     * Example: {"temperature": 25.5, "humidity": 60}
     */
    private Map<String, Object> payload;
    
    /**
     * Data type (optional)
     * If not provided, will be auto-classified by the system
     */
    private DataType dataType;
}
