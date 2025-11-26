package com.smartcity.model;

/**
 * Enum DataType
 * Phân loại dữ liệu IoT theo mức độ "nóng" - tần suất truy vấn
 * 
 * - HOT: Dữ liệu nóng - Lưu trong Redis với TTL = 1 giờ
 * - WARM: Dữ liệu ấm - Lưu trong MongoDB Warm (dữ liệu tuần)
 * - COLD: Dữ liệu lạnh - Lưu trong MongoDB Cold (dữ liệu tháng)
 */
public enum DataType {
    /**
     * Dữ liệu HOT - Cần truy vấn nhanh, lưu trong Redis
     * TTL = 1 giờ
     */
    HOT,
    
    /**
     * Dữ liệu WARM - Lưu trữ dữ liệu tuần trong MongoDB Warm
     */
    WARM,
    
    /**
     * Dữ liệu COLD - Lưu trữ dữ liệu tháng trong MongoDB Cold
     */
    COLD
}
