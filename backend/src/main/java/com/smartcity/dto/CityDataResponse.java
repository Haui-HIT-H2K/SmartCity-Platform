SPDX-License-Identifier: Apache-2.0
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

