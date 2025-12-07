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

package com.smartcity.controller;

import com.smartcity.config.EdgeNodeConfig;
import com.smartcity.service.EdgeNodeRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

/**
 * Nodes Controller
 * API endpoint để frontend lấy thông tin Edge Nodes
 */
@Slf4j
@RestController
@RequestMapping("/api/nodes")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class NodesController {

    private final EdgeNodeRegistry edgeNodeRegistry;

    public NodesController(EdgeNodeRegistry edgeNodeRegistry) {
        this.edgeNodeRegistry = edgeNodeRegistry;
    }

    /**
     * API: GET /api/nodes
     * Trả về danh sách Edge Nodes để frontend hiển thị
     * 
     * Response format phù hợp với frontend EdgeNode interface:
     * {
     *   id: string
     *   name: string
     *   host: string
     *   port: number
     *   status: 'online' | 'offline'
     *   lastPing?: string
     * }
     * 
     * @return List of edge nodes
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getEdgeNodes() {
        log.debug("Fetching edge nodes list for frontend");
        
        try {
            List<EdgeNodeConfig.EdgeNode> nodes = edgeNodeRegistry.getAllNodes();
            
            // Transform to frontend format
            List<Map<String, Object>> response = nodes.stream()
                .map(node -> {
                    Map<String, Object> nodeData = new HashMap<>();
                    nodeData.put("id", node.getName().toLowerCase().replace(" ", "-"));
                    nodeData.put("name", node.getName());
                    nodeData.put("host", node.getHost());
                    nodeData.put("port", node.getPort());
                    nodeData.put("enabled", node.isEnabled());  // Add enabled field
                    // Giả định node online nếu enabled
                    nodeData.put("status", node.isEnabled() ? "online" : "offline");
                    nodeData.put("lastPing", java.time.LocalDateTime.now().toString());
                    return nodeData;
                })
                .collect(Collectors.toList());
            
            log.info("Returning {} edge nodes", response.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching edge nodes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(List.of());
        }
    }
    
    /**
     * API: POST /api/nodes
     * Create new Edge Node
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNode(@RequestBody CreateNodeRequest request) {
        log.info("Creating new edge node: {}", request.getName());
        
        try {
            // Create new node from request
            EdgeNodeConfig.EdgeNode newNode = new EdgeNodeConfig.EdgeNode();
            newNode.setName(request.getName());
            newNode.setHost(request.getHost());
            newNode.setPort(request.getPort());
            newNode.setQueueName(request.getQueueName());
            newNode.setUsername(request.getUsername());
            newNode.setPassword(request.getPassword());
            newNode.setEnabled(true); // Default to enabled
            
            boolean added = edgeNodeRegistry.addNode(newNode);
            
            if (!added) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Node with name '" + request.getName() + "' already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
            
            // Return created node
            Map<String, Object> response = new HashMap<>();
            response.put("id", newNode.getName().toLowerCase().replace(" ", "-"));
            response.put("name", newNode.getName());
            response.put("host", newNode.getHost());
            response.put("port", newNode.getPort());
            response.put("status", "online");
            response.put("enabled", newNode.isEnabled());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error creating edge node: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to create node");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * API: PUT /api/nodes/{name}/toggle
     * Toggle Edge Node enabled status
     */
    @PutMapping("/{name}/toggle")
    public ResponseEntity<Map<String, Object>> toggleNode(@PathVariable String name) {
        log.info("Toggling edge node: {}", name);
        
        try {
            EdgeNodeConfig.EdgeNode node = edgeNodeRegistry.getNodeByName(name);
            
            if (node == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Node '" + name + "' not found");
                return ResponseEntity.notFound().build();
            }
            
            // Toggle the status
            boolean newStatus = !node.isEnabled();
            boolean updated = edgeNodeRegistry.updateNodeStatus(name, newStatus);
            
            if (!updated) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Failed to update node status");
                return ResponseEntity.internalServerError().body(error);
            }
            
            // Return updated node
            Map<String, Object> response = new HashMap<>();
            response.put("name", node.getName());
            response.put("enabled", newStatus);
            response.put("status", newStatus ? "online" : "offline");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error toggling edge node: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to toggle node");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * API: DELETE /api/nodes/{name}
     * Delete Edge Node
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, Object>> deleteNode(@PathVariable String name) {
        log.info("Deleting edge node: {}", name);
        
        try {
            boolean deleted = edgeNodeRegistry.deleteNode(name);
            
            if (!deleted) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Node '" + name + "' not found");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Node deleted successfully");
            response.put("name", name);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting edge node: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to delete node");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * DTO for creating new node
     */
    public static class CreateNodeRequest {
        private String name;
        private String host;
        private int port;
        private String queueName;
        private String username;
        private String password;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public String getQueueName() { return queueName; }
        public void setQueueName(String queueName) { this.queueName = queueName; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
