package com.smartcity.dto;

import com.smartcity.model.DataType;
import java.util.Map;

/**
 * DTO representing a normalized sensor record sent to the frontend.
 */
public record CityDataResponse(
        String id,
        String sensorId,
        DataType type,
        double value,
        String timestamp,
        Location location,
        Map<String, Object> metadata
) {

    public record Location(double lat, double lng) {}
}

