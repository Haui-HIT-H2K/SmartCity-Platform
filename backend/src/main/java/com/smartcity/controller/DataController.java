package com.smartcity.controller;

import com.smartcity.dto.DataPageResponse;
import com.smartcity.model.DataType;
import com.smartcity.service.CityDataQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.ok(response);
    }
}

