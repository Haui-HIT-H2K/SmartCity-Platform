package com.smartcity.service;

import com.smartcity.config.EdgeNodeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Edge Node Registry Service
 * Service quản lý danh sách Edge Storage (giống DNS Manager)
 * 
 * Chức năng:
 * - Load danh sách Edge Nodes từ config
 * - Cung cấp danh sách nodes "Active" để Ingestion Service sử dụng
 * - (Optional) Health check để kiểm tra nodes còn sống
 */
@Slf4j
@Service
public class EdgeNodeRegistry {

    private final EdgeNodeConfig edgeNodeConfig;

    public EdgeNodeRegistry(EdgeNodeConfig edgeNodeConfig) {
        this.edgeNodeConfig = edgeNodeConfig;
    }

    /**
     * Khởi tạo - Load và log danh sách Edge Nodes
     */
    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("Edge Node Registry - Initializing");
        log.info("========================================");
        
        List<EdgeNodeConfig.EdgeNode> nodes = getAvailableNodes();
        
        log.info("DNS Resolved: Found {} Edge Storage(s)", nodes.size());
        
        for (EdgeNodeConfig.EdgeNode node : nodes) {
            log.info("  - {} | {}:{} | Queue: {} | Status: {}", 
                    node.getName(), 
                    node.getHost(), 
                    node.getPort(),
                    node.getQueueName() != null ? node.getQueueName() : "auto-generate",
                    node.isEnabled() ? "ENABLED" : "DISABLED");
        }
        
        log.info("========================================");
    }

    /**
     * Lấy danh sách Edge Nodes đang available (enabled)
     * 
     * @return List các Edge Nodes active
     */
    public List<EdgeNodeConfig.EdgeNode> getAvailableNodes() {
        return edgeNodeConfig.getNodes().stream()
                .filter(EdgeNodeConfig.EdgeNode::isEnabled)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả Edge Nodes (bao gồm disabled)
     * 
     * @return List tất cả Edge Nodes
     */
    public List<EdgeNodeConfig.EdgeNode> getAllNodes() {
        return edgeNodeConfig.getNodes();
    }

    /**
     * Đếm số lượng Edge Nodes active
     * 
     * @return Số lượng nodes enabled
     */
    public int getActiveNodeCount() {
        return (int) edgeNodeConfig.getNodes().stream()
                .filter(EdgeNodeConfig.EdgeNode::isEnabled)
                .count();
    }

    /**
     * Kiểm tra có Edge Nodes nào available không
     * 
     * @return true nếu có ít nhất 1 node enabled
     */
    public boolean hasAvailableNodes() {
        return getActiveNodeCount() > 0;
    }

    /**
     * Get Edge Node by name
     * 
     * @param name Tên node
     * @return EdgeNode nếu tìm thấy, null nếu không
     */
    public EdgeNodeConfig.EdgeNode getNodeByName(String name) {
        return edgeNodeConfig.getNodes().stream()
                .filter(node -> node.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
