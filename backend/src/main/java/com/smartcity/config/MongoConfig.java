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

package com.smartcity.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * MongoDB Multi-Datasource Configuration
 * Cấu hình 2 MongoDB instances với authentication:
 * - Warm Storage: Lưu dữ liệu tuần
 * - Cold Storage: Lưu dữ liệu tháng
 */
@Slf4j
@Configuration
public class MongoConfig {

    @Value("${mongodb.warm.host}")
    private String warmHost;

    @Value("${mongodb.warm.port}")
    private int warmPort;

    @Value("${mongodb.warm.database}")
    private String warmDatabase;

    @Value("${mongodb.warm.username}")
    private String warmUsername;

    @Value("${mongodb.warm.password}")
    private String warmPassword;

    @Value("${mongodb.warm.authentication-database}")
    private String warmAuthDatabase;

    @Value("${mongodb.cold.host}")
    private String coldHost;

    @Value("${mongodb.cold.port}")
    private int coldPort;

    @Value("${mongodb.cold.database}")
    private String coldDatabase;

    @Value("${mongodb.cold.username}")
    private String coldUsername;

    @Value("${mongodb.cold.password}")
    private String coldPassword;

    @Value("${mongodb.cold.authentication-database}")
    private String coldAuthDatabase;

    /**
     * MongoClient cho Warm Storage với authentication
     */
    @Bean(name = "warmMongoClient")
    @Primary
    public MongoClient warmMongoClient() {
        String connectionString = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s", 
            warmUsername, warmPassword, warmHost, warmPort, warmDatabase, warmAuthDatabase);
        
        log.info("Initializing Warm MongoDB Client: mongodb://{}:***@{}:{}/{}?authSource={}", 
            warmUsername, warmHost, warmPort, warmDatabase, warmAuthDatabase);
        
        return MongoClients.create(connectionString);
    }

    /**
     * MongoTemplate cho Warm Storage
     * Sử dụng để lưu dữ liệu WARM (dữ liệu tuần)
     */
    @Bean(name = "warmMongoTemplate")
    @Primary
    public MongoTemplate warmMongoTemplate() {
        log.info("Creating Warm MongoTemplate for database: {}", warmDatabase);
        
        return new MongoTemplate(warmMongoClient(), warmDatabase);
    }

    /**
     * MongoClient cho Cold Storage với authentication
     */
    @Bean(name = "coldMongoClient")
    public MongoClient coldMongoClient() {
        String connectionString = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s", 
            coldUsername, coldPassword, coldHost, coldPort, coldDatabase, coldAuthDatabase);
        
        log.info("Initializing Cold MongoDB Client: mongodb://{}:***@{}:{}/{}?authSource={}", 
            coldUsername, coldHost, coldPort, coldDatabase, coldAuthDatabase);
        
        return MongoClients.create(connectionString);
    }

    /**
     * MongoTemplate cho Cold Storage
     * Sử dụng để lưu dữ liệu COLD (dữ liệu tháng)
     */
    @Bean(name = "coldMongoTemplate")
    public MongoTemplate coldMongoTemplate() {
        log.info("Creating Cold MongoTemplate for database: {}", coldDatabase);
        
        return new MongoTemplate(coldMongoClient(), coldDatabase);
    }
}
