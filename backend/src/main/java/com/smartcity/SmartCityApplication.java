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

package com.smartcity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Smart City Platform - Core Backend Application
 * OLP 2025 - Hệ thống IoT thành phố thông minh
 */
@Slf4j
@EnableScheduling
@SpringBootApplication
public class SmartCityApplication {

    public static void main(String[] args) {
        log.info("========================================");
        log.info("Starting Smart City Platform - Core Backend");
        log.info("OLP 2025 - IoT Smart City Platform");
        log.info("========================================");
        
        SpringApplication.run(SmartCityApplication.class, args);
        
        log.info("========================================");
        log.info("Smart City Backend started successfully!");
        log.info("========================================");
    }
}
