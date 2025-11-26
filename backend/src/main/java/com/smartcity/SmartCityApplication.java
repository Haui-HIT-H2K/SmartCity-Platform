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
