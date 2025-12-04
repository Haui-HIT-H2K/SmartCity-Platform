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

package com.smartcity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcity.config.EdgeNodeConfig;
import com.smartcity.model.CityData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RabbitMQ Ingestion Service
 * Service chủ động PULL dữ liệu từ RabbitMQ theo batch
 * 
 * Sử dụng EdgeNodeRegistry để quản lý danh sách Edge Storage động
 */
@Slf4j
@Service
public class RabbitMQIngestionService {

    private final EdgeNodeRegistry edgeNodeRegistry;
    private final DataRoutingService dataRoutingService;
    private final MLServiceClient mlServiceClient;
    private final MessageConverter messageConverter;
    private final ObjectMapper objectMapper;
    private final MetricsService metricsService;
    
    @Value("${ingestion.batch.size}")
    private int batchSize;
    
    @Value("${ingestion.batch.max-size}")
    private int maxBatchSize;

    public RabbitMQIngestionService(
            EdgeNodeRegistry edgeNodeRegistry,
            DataRoutingService dataRoutingService,
            MLServiceClient mlServiceClient,
            MessageConverter messageConverter,
            ObjectMapper objectMapper,
            MetricsService metricsService) {
        this.edgeNodeRegistry = edgeNodeRegistry;
        this.dataRoutingService = dataRoutingService;
        this.mlServiceClient = mlServiceClient;
        this.messageConverter = messageConverter;
        this.objectMapper = objectMapper;
        this.metricsService = metricsService;
    }

    /**
     * Scheduled Task - Chạy mỗi 10 giây
     * Kéo dữ liệu từ tất cả Edge Nodes có trong Registry
     */
    @Scheduled(fixedRateString = "${ingestion.schedule.fixed-rate}", 
               initialDelayString = "${ingestion.schedule.initial-delay}")
    public void pullDataFromAllEdges() {
        log.info("========================================");
        log.info("Starting scheduled batch pull from all edge nodes");
        log.info("========================================");
        
        try {
            // Lấy danh sách Edge Nodes từ Registry (DNS Resolution)
            List<EdgeNodeConfig.EdgeNode> availableNodes = edgeNodeRegistry.getAvailableNodes();
            
            if (availableNodes.isEmpty()) {
                log.warn("No available edge nodes found in registry");
                return;
            }
            
            log.info("DNS Resolved: Found {} Edge Storage(s)", availableNodes.size());
            
            // Collect tất cả dữ liệu từ các nodes
            List<CityData> allData = new ArrayList<>();
            
            // Loop qua từng Edge Node
            for (EdgeNodeConfig.EdgeNode node : availableNodes) {
                try {
                    log.info("Pulling batch from [{}] ({}:{})", 
                            node.getName(), node.getHost(), node.getPort());
                    
                    List<CityData> nodeData = pullBatchFromEdge(node);
                    allData.addAll(nodeData);
                    
                    log.info("  → Received {} messages from [{}]", 
                            nodeData.size(), node.getName());
                    
                } catch (Exception e) {
                    // Resilience: Nếu một node fail, tiếp tục với node khác
                    log.error("Failed to pull from [{}]: {}", node.getName(), e.getMessage());
                    log.debug("Continuing with other nodes...");
                }
            }
            
            // Tổng hợp
            log.info("Total messages received from all nodes: {}", allData.size());
            
            // Record metrics for incoming messages
            if (!allData.isEmpty()) {
                metricsService.recordIncoming(allData.size());
            }
            
            // Route và lưu trữ dữ liệu
            if (!allData.isEmpty()) {
                dataRoutingService.routeAndStore(allData);
                // Record as processed after storage
                metricsService.recordProcessed(allData.size());
            } else {
                log.info("No data to process in this cycle");
            }
            
        } catch (Exception e) {
            log.error("Error during batch pull: {}", e.getMessage(), e);
            // Hệ thống không crash - lần schedule tiếp theo sẽ retry
        }
        
        log.info("========================================");
        log.info("Batch pull completed");
        log.info("========================================");
    }

    /**
     * Pull một batch dữ liệu từ một edge node
     * 
     * @param node Edge Node configuration
     * @return List các CityData đã nhận được
     */
    private List<CityData> pullBatchFromEdge(EdgeNodeConfig.EdgeNode node) {
        List<CityData> batchData = new ArrayList<>();
        
        RabbitTemplate rabbitTemplate = null;
        CachingConnectionFactory connectionFactory = null;
        
        try {
            // Tạo connection động dựa vào node config
            connectionFactory = createConnectionFactory(node);
            rabbitTemplate = new RabbitTemplate(connectionFactory);
            
            // Set MessageConverter để deserialize JSON
            rabbitTemplate.setMessageConverter(messageConverter);
            
            // Set default queue
            String queueName = node.getQueueName() != null ? 
                    node.getQueueName() : "city-data-queue";
            rabbitTemplate.setDefaultReceiveQueue(queueName);
            
            int receivedCount = 0;
            
            // Loop để lấy messages cho đến khi:
            // 1. Đạt max batch size
            // 2. Queue rỗng (receiveAndConvert trả về null)
            for (int i = 0; i < maxBatchSize; i++) {
                Object message = rabbitTemplate.receiveAndConvert();
                
                if (message == null) {
                    // Queue đã rỗng
                    log.debug("[{}] - Queue empty after {} messages", 
                            node.getName(), receivedCount);
                    break;
                }
                
                // Convert message to CityData
                CityData cityData = null;
                if (message instanceof CityData) {
                    cityData = (CityData) message;
                } else if (message instanceof Map) {
                    // LinkedHashMap from JSON deserialization
                    try {
                        cityData = objectMapper.convertValue(message, CityData.class);
                    } catch (Exception e) {
                        log.error("[{}] - Error converting Map to CityData: {}", 
                                node.getName(), e.getMessage());
                    }
                } else {
                    log.warn("[{}] - Received unknown message type: {}", 
                            node.getName(), message.getClass().getName());
                }
                
                if (cityData != null) {
                    // CRITICAL: Classify data using ML Service before storing
                    try {
                        com.smartcity.model.DataType dataType = mlServiceClient.classifyData(cityData);
                        cityData.setDataType(dataType);
                        
                        if (receivedCount % 100 == 0 && receivedCount > 0) {
                            log.debug("[{}] - Classified {} messages (last: {})", 
                                    node.getName(), receivedCount, dataType);
                        }
                    } catch (Exception e) {
                        log.error("[{}] - Error classifying data, defaulting to COLD: {}", 
                                node.getName(), e.getMessage());
                        cityData.setDataType(com.smartcity.model.DataType.COLD);
                    }
                    
                    batchData.add(cityData);
                    receivedCount++;
                    
                    // Log mỗi 100 messages
                    if (receivedCount % 100 == 0) {
                        log.debug("[{}] - Received {} messages...", 
                                node.getName(), receivedCount);
                    }
                }
                
                // Đạt batch size mong muốn
                if (receivedCount >= batchSize) {
                    log.debug("[{}] - Reached target batch size: {}", 
                            node.getName(), batchSize);
                    // Có thể break hoặc tiếp tục đến maxBatchSize
                }
            }
            
        } catch (Exception e) {
            log.error("[{}] - Error pulling batch: {}", node.getName(), e.getMessage());
            throw new RuntimeException("Failed to pull from " + node.getName(), e);
            
        } finally {
            // Đóng connection
            if (connectionFactory != null) {
                connectionFactory.destroy();
            }
        }
        
        return batchData;
    }

    /**
     * Tạo RabbitMQ ConnectionFactory động từ node config
     */
    private CachingConnectionFactory createConnectionFactory(EdgeNodeConfig.EdgeNode node) {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(node.getHost());
        factory.setPort(node.getPort());
        
        // Use credentials từ node, hoặc default
        if (node.getUsername() != null && node.getPassword() != null) {
            factory.setUsername(node.getUsername());
            factory.setPassword(node.getPassword());
        }
        
        return factory;
    }

    /**
     * Manual trigger - Để test/demo
     * Có thể gọi từ Controller
     */
    public void manualTriggerPull() {
        log.info("Manual trigger - Starting batch pull");
        pullDataFromAllEdges();
    }
}
