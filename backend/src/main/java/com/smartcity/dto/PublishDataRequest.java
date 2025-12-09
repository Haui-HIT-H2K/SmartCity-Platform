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
