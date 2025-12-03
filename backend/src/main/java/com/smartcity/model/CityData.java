package com.smartcity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * Entity: CityData
 * Model dữ liệu IoT từ các cảm biến trong thành phố thông minh
 * 
 * Được lưu trữ trong:
 * - Redis (nếu dataType = HOT)
 * - MongoDB Warm (nếu dataType = WARM)
 * - MongoDB Cold (nếu dataType = COLD)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "city_data")
public class CityData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * ID duy nhất của bản ghi (UUID)
     */
    @Id
    private String id;
    
    /**
     * ID thiết bị IoT nguồn (Device ID)
     * Ví dụ: "sensor-001", "camera-hoan-kiem-01"
     */
    private String sourceId;
    
    /**
     * Dữ liệu cảm biến (Sensor Data)
     * Là một Map linh hoạt chứa các giá trị từ cảm biến
     * 
     * Ví dụ:
     * {
     *   "temperature": 25.5,
     *   "humidity": 60,
     *   "location": {
     *     "lat": 21.0285,
     *     "lon": 105.8542
     *   }
     * }
     */
    private Map<String, Object> payload;
    
    /**
     * Loại dữ liệu: HOT, WARM, hoặc COLD
     * Quyết định nơi lưu trữ của dữ liệu
     */
    private DataType dataType;
    
    /**
     * Thời gian ghi nhận dữ liệu (epoch milliseconds)
     * Sử dụng System.currentTimeMillis()
     */
    private Long timestamp;
    
    /**
     * Tạo ID mới nếu chưa có (Pre-persist)
     */
    public void generateId() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
    }
    
    /**
     * Builder pattern hỗ trợ tạo đối tượng với ID tự động
     */
    public static class CityDataBuilder {
        public CityData build() {
            CityData cityData = new CityData(id, sourceId, payload, dataType, timestamp);
            cityData.generateId();
            return cityData;
        }
    }
}
