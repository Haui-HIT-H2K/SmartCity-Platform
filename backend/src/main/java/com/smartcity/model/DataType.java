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
