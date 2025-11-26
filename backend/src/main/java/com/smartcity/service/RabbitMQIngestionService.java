package com.smartcity.service;

import com.smartcity.model.CityData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * RabbitMQ Ingestion Service
 * Service chủ động PULL dữ liệu từ RabbitMQ theo batch
 * 
 * Cơ chế:
 * - Mỗi 10 giây, kéo dữ liệu từ cả 2 edge nodes
 * - Lấy 1 batch lớn (1000-5000 messages)
 * - Không dùng Listener thụ động
 */
@Slf4j
@Service
public class RabbitMQIngestionService {

    private final RabbitTemplate edge1RabbitTemplate;
    private final RabbitTemplate edge2RabbitTemplate;
    private final DataRoutingService dataRoutingService;
    
    @Value("${ingestion.batch.size}")
    private int batchSize;
    
    @Value("${ingestion.batch.max-size}")
    private int maxBatchSize;

    public RabbitMQIngestionService(
            @Qualifier("edge1RabbitTemplate") RabbitTemplate edge1RabbitTemplate,
            @Qualifier("edge2RabbitTemplate") RabbitTemplate edge2RabbitTemplate,
            DataRoutingService dataRoutingService) {
        this.edge1RabbitTemplate = edge1RabbitTemplate;
        this.edge2RabbitTemplate = edge2RabbitTemplate;
        this.dataRoutingService = dataRoutingService;
    }

    /**
     * Scheduled Task - Chạy mỗi 10 giây
     * Kéo dữ liệu từ cả 2 edge nodes
     */
    @Scheduled(fixedRateString = "${ingestion.schedule.fixed-rate}", 
               initialDelayString = "${ingestion.schedule.initial-delay}")
    public void pullDataFromAllEdges() {
        log.info("========================================");
        log.info("Starting scheduled batch pull from all edge nodes");
        log.info("========================================");
        
        try {
            // Pull từ Edge Node 1
            List<CityData> edge1Data = pullBatchFromEdge(edge1RabbitTemplate, "Edge-1");
            
            // Pull từ Edge Node 2
            List<CityData> edge2Data = pullBatchFromEdge(edge2RabbitTemplate, "Edge-2");
            
            // Tổng hợp dữ liệu
            List<CityData> allData = new ArrayList<>();
            allData.addAll(edge1Data);
            allData.addAll(edge2Data);
            
            int totalReceived = allData.size();
            log.info("Total messages received: {} (Edge-1: {}, Edge-2: {})", 
                    totalReceived, edge1Data.size(), edge2Data.size());
            
            // Route và lưu trữ dữ liệu
            if (!allData.isEmpty()) {
                dataRoutingService.routeAndStore(allData);
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
     * @param rabbitTemplate RabbitTemplate của edge node
     * @param edgeName Tên edge node (để log)
     * @return List các CityData đã nhận được
     */
    private List<CityData> pullBatchFromEdge(RabbitTemplate rabbitTemplate, String edgeName) {
        List<CityData> batchData = new ArrayList<>();
        
        log.info("Pulling batch from {}, target size: {}, max: {}", 
                edgeName, batchSize, maxBatchSize);
        
        try {
            int receivedCount = 0;
            
            // Loop để lấy messages cho đến khi:
            // 1. Đạt max batch size
            // 2. Queue rỗng (receiveAndConvert trả về null)
            for (int i = 0; i < maxBatchSize; i++) {
                Object message = rabbitTemplate.receiveAndConvert();
                
                if (message == null) {
                    // Queue đã rỗng
                    log.debug("{} - Queue empty after {} messages", edgeName, receivedCount);
                    break;
                }
                
                // Convert message to CityData
                if (message instanceof CityData) {
                    batchData.add((CityData) message);
                    receivedCount++;
                    
                    // Log mỗi 100 messages
                    if (receivedCount % 100 == 0) {
                        log.debug("{} - Received {} messages...", edgeName, receivedCount);
                    }
                } else {
                    log.warn("{} - Received unknown message type: {}", 
                            edgeName, message.getClass().getName());
                }
                
                // Đạt batch size mong muốn? Có thể dừng (hoặc tiếp tục đến max)
                if (receivedCount >= batchSize) {
                    log.debug("{} - Reached target batch size: {}", edgeName, batchSize);
                    // Có thể break ở đây, hoặc tiếp tục đến maxBatchSize
                }
            }
            
            log.info("{} - Successfully pulled {} messages", edgeName, receivedCount);
            
        } catch (Exception e) {
            log.error("{} - Error pulling batch: {}", edgeName, e.getMessage(), e);
            // Không throw exception - cho phép edge khác tiếp tục
        }
        
        return batchData;
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
