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

import com.smartcity.config.EdgeNodeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
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
    
    /**
     * Add new Edge Node
     * 
     * @param node New edge node to add
     * @return true if added successfully, false if node with same name already exists
     */
    public synchronized boolean addNode(EdgeNodeConfig.EdgeNode node) {
        // Check if node with same name already exists
        if (getNodeByName(node.getName()) != null) {
            log.warn("Cannot add node: Node with name '{}' already exists", node.getName());
            return false;
        }
        
        edgeNodeConfig.getNodes().add(node);
        log.info("Added new edge node: {} | {}:{}", node.getName(), node.getHost(), node.getPort());
        return true;
    }
    
    /**
     * Update node enabled status (Toggle enable/disable)
     * 
     * @param name Node name
     * @param enabled New enabled status
     * @return true if updated successfully, false if node not found
     */
    public synchronized boolean updateNodeStatus(String name, boolean enabled) {
        EdgeNodeConfig.EdgeNode node = getNodeByName(name);
        if (node == null) {
            log.warn("Cannot update status: Node '{}' not found", name);
            return false;
        }
        
        node.setEnabled(enabled);
        log.info("Updated node '{}' status to: {}", name, enabled ? "ENABLED" : "DISABLED");
        return true;
    }
    
    /**
     * Delete Edge Node
     * 
     * @param name Node name to delete
     * @return true if deleted successfully, false if node not found
     */
    public synchronized boolean deleteNode(String name) {
        EdgeNodeConfig.EdgeNode node = getNodeByName(name);
        if (node == null) {
            log.warn("Cannot delete: Node '{}' not found", name);
            return false;
        }
        
        boolean removed = edgeNodeConfig.getNodes().remove(node);
        if (removed) {
            log.info("Deleted edge node: {}", name);
        }
        return removed;
    }
}
