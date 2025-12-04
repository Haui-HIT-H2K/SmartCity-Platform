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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
}
