package com.smartcity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Edge Node Configuration Properties
 * Map danh sách Edge Storage từ application.yml
 * 
 * Đóng vai trò như DNS Configuration cho Edge Nodes
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.edge")
public class EdgeNodeConfig {
    
    /**
     * Danh sách các Edge Storage Nodes
     */
    private List<EdgeNode> nodes = new ArrayList<>();
    
    /**
     * Inner class đại diện cho một Edge Storage Node
     */
    @Data
    public static class EdgeNode {
        /**
         * Tên khu vực / Subnet (VD: "Subnet-CauGiay")
         */
        private String name;
        
        /**
         * Host của RabbitMQ (VD: "rabbit-edge-1")
         */
        private String host;
        
        /**
         * Port của RabbitMQ (VD: 5672)
         */
        private int port;
        
        /**
         * Queue name (optional, mặc định sẽ generate từ name)
         */
        private String queueName;
        
        /**
         * Username (optional, mặc định từ spring.rabbitmq)
         */
        private String username;
        
        /**
         * Password (optional, mặc định từ spring.rabbitmq)
         */
        private String password;
        
        /**
         * Enabled status (mặc định true)
         */
        private boolean enabled = true;
    }
}
