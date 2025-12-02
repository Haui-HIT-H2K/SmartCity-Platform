package com.smartcity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcity.dto.CityDataResponse;
import com.smartcity.dto.DataPageResponse;
import com.smartcity.model.CityData;
import com.smartcity.model.DataType;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Service that aggregates data from the different storage tiers for the UI.
 */
@Slf4j
@Service
public class CityDataQueryService {

    private static final String HOT_KEY_PATTERN = "hot:citydata:*";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final MongoTemplate warmMongoTemplate;
    private final MongoTemplate coldMongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public CityDataQueryService(
            @Qualifier("warmMongoTemplate") MongoTemplate warmMongoTemplate,
            @Qualifier("coldMongoTemplate") MongoTemplate coldMongoTemplate,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.warmMongoTemplate = warmMongoTemplate;
        this.coldMongoTemplate = coldMongoTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches data based on the requested filters.
     * If requestedType is null, fetches from ALL storage tiers.
     */
    public DataPageResponse fetchData(DataType requestedType, String sensorId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        log.debug("Fetching data | type={}, sensorId={}, page={}, size={}",
                requestedType, sensorId, safePage, safeSize);

        DataSlice slice;
        
        // If no type specified, fetch from all storage tiers
        if (requestedType == null) {
            slice = fetchAllTypes(sensorId, safePage, safeSize);
        } else {
            // Fetch from specific storage tier
            switch (requestedType) {
                case HOT:
                    slice = fetchHotSlice(sensorId, safePage, safeSize);
                    break;
                case COLD:
                    slice = fetchMongoSlice(coldMongoTemplate, DataType.COLD, sensorId, safePage, safeSize);
                    break;
                case WARM:
                    slice = fetchMongoSlice(warmMongoTemplate, DataType.WARM, sensorId, safePage, safeSize);
                    break;
                default:
                    slice = DataSlice.empty();
                    break;
            }
        }

        List<CityDataResponse> payload = slice.records().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        int totalPages = safeSize > 0
                ? (int) Math.ceil((double) slice.total() / safeSize)
                : 0;

        return new DataPageResponse(
                payload,
                slice.total(),
                safePage + 1,
                safeSize,
                totalPages,
                requestedType
        );
    }
    
    /**
     * Fetch data from all storage tiers (HOT + WARM + COLD) when no specific type is requested
     */
    private DataSlice fetchAllTypes(String sensorId, int page, int size) {
        log.debug("Fetching from all storage tiers");
        
        List<CityData> allData = new java.util.ArrayList<>();
        
        // Fetch from HOT (Redis)
        try {
            DataSlice hotSlice = fetchHotSlice(sensorId, 0, Integer.MAX_VALUE);
            allData.addAll(hotSlice.records());
            log.debug("Fetched {} HOT records", hotSlice.records().size());
        } catch (Exception e) {
            log.warn("Error fetching HOT data: {}", e.getMessage());
        }
        
        // Fetch from WARM (MongoDB)
        try {
            DataSlice warmSlice = fetchMongoSlice(warmMongoTemplate, DataType.WARM, sensorId, 0, Integer.MAX_VALUE);
            allData.addAll(warmSlice.records());
            log.debug("Fetched {} WARM records", warmSlice.records().size());
        } catch (Exception e) {
            log.warn("Error fetching WARM data: {}", e.getMessage());
        }
        
        // Fetch from COLD (MongoDB)
        try {
            DataSlice coldSlice = fetchMongoSlice(coldMongoTemplate, DataType.COLD, sensorId, 0, Integer.MAX_VALUE);
            allData.addAll(coldSlice.records());
            log.debug("Fetched {} COLD records", coldSlice.records().size());
        } catch (Exception e) {
            log.warn("Error fetching COLD data: {}", e.getMessage());
        }
        
        // Sort all data by timestamp (most recent first)
        List<CityData> sortedData = allData.stream()
                .sorted(Comparator.comparingLong(
                        (CityData data) -> data.getTimestamp() != null ? data.getTimestamp() : 0L)
                        .reversed())
                .collect(Collectors.toList());
        
        long total = sortedData.size();
        
        // Apply pagination
        int fromIndex = Math.min((int) ((long) page * size), sortedData.size());
        int toIndex = Math.min(fromIndex + size, sortedData.size());
        List<CityData> pageRecords = fromIndex < toIndex
                ? sortedData.subList(fromIndex, toIndex)
                : Collections.emptyList();
        
        log.debug("All types slice | total={} returning={}", total, pageRecords.size());
        return new DataSlice(pageRecords, total);
    }

    private DataSlice fetchMongoSlice(
            MongoTemplate template,
            DataType type,
            String sensorId,
            int page,
            int size
    ) {
        Query baseQuery = new Query().addCriteria(Criteria.where("dataType").is(type));

        if (StringUtils.hasText(sensorId)) {
            baseQuery.addCriteria(Criteria.where("sourceId").is(sensorId));
        }

        long total = template.count(baseQuery, CityData.class);

        Query pagedQuery = baseQuery
                .with(Sort.by(Sort.Direction.DESC, "timestamp"))
                .skip((long) page * size)
                .limit(size);

        List<CityData> records = template.find(pagedQuery, CityData.class);
        log.debug("Mongo slice | type={} records={} total={}", type, records.size(), total);
        return new DataSlice(records, total);
    }

    private DataSlice fetchHotSlice(String sensorId, int page, int size) {
        Set<String> keys = redisTemplate.keys(HOT_KEY_PATTERN);
        if (keys == null || keys.isEmpty()) {
            return DataSlice.empty();
        }

        List<CityData> filtered = keys.stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .map(this::convertRedisValue)
                .filter(Objects::nonNull)
                .filter(data -> {
                    if (!StringUtils.hasText(sensorId)) {
                        return true;
                    }
                    return sensorId.equalsIgnoreCase(data.getSourceId());
                })
                .sorted(Comparator.comparingLong(
                        (CityData data) -> data.getTimestamp() != null ? data.getTimestamp() : 0L)
                        .reversed())
                .collect(Collectors.toList());

        long total = filtered.size();
        int fromIndex = Math.min((int) ((long) page * size), filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<CityData> pageRecords = fromIndex < toIndex
                ? filtered.subList(fromIndex, toIndex)
                : Collections.emptyList();

        log.debug("Redis slice | keys={} filtered={} returning={}", keys.size(), total, pageRecords.size());
        return new DataSlice(pageRecords, total);
    }

    private CityData convertRedisValue(Object value) {
        if (value instanceof CityData cityData) {
            return cityData;
        }

        if (value instanceof Map<?, ?> mapValue) {
            return objectMapper.convertValue(mapValue, CityData.class);
        }

        log.warn("Unsupported Redis payload type: {}", value != null ? value.getClass() : "null");
        return null;
    }

    private CityDataResponse toResponse(CityData data) {
        double primaryValue = extractPrimaryValue(data.getPayload());
        CityDataResponse.Location location = extractLocation(data.getPayload());
        Map<String, Object> metadata = data.getPayload() != null
                ? new LinkedHashMap<>(data.getPayload())
                : Collections.emptyMap();

        return new CityDataResponse(
                data.getId(),
                data.getSourceId(),
                data.getDataType(),
                primaryValue,
                formatTimestamp(data.getTimestamp()),
                location,
                metadata
        );
    }

    private double extractPrimaryValue(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return 0d;
        }

        List<String> preferredOrder = List.of("temperature", "value", "humidity", "co2_level");
        for (String key : preferredOrder) {
            Double candidate = toDouble(payload.get(key));
            if (candidate != null) {
                return candidate;
            }
        }

        for (Object value : payload.values()) {
            Double candidate = toDouble(value);
            if (candidate != null) {
                return candidate;
            }
        }

        return 0d;
    }

    private Double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private CityDataResponse.Location extractLocation(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }

        Object locationObj = payload.get("location");
        if (!(locationObj instanceof Map<?, ?> locationMap)) {
            return null;
        }

        Double lat = toDouble(locationMap.get("lat"));
        // Get lng or fallback to lon
        Object lngObj = locationMap.containsKey("lng") ? locationMap.get("lng") : locationMap.get("lon");
        Double lng = toDouble(lngObj);

        if (lat == null || lng == null) {
            return null;
        }

        return new CityDataResponse.Location(lat, lng);
    }

    private String formatTimestamp(Long timestamp) {
        long safeTs = timestamp != null ? timestamp : System.currentTimeMillis();
        return ISO_FORMATTER.format(Instant.ofEpochMilli(safeTs).atOffset(ZoneOffset.UTC));
    }

    private record DataSlice(List<CityData> records, long total) {
        static DataSlice empty() {
            return new DataSlice(Collections.emptyList(), 0);
        }
    }
}

