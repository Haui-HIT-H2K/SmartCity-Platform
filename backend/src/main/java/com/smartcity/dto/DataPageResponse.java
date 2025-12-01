package com.smartcity.dto;

import com.smartcity.model.DataType;
import java.util.List;

/**
 * Pagination envelope returned by the /api/data endpoint.
 */
public record DataPageResponse(
        List<CityDataResponse> data,
        long total,
        int page,
        int pageSize,
        int totalPages,
        DataType appliedType
) {}

