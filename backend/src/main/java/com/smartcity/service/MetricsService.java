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

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service để track ingestion metrics với historical data cho biểu đồ
 */
@Slf4j
@Service
public class MetricsService {
    
    // Counters
    private final AtomicInteger incomingCount = new AtomicInteger(0);
    private final AtomicInteger processedCount = new AtomicInteger(0);
    
    // Timestamps for rate calculation
    private final AtomicLong lastResetTime = new AtomicLong(Instant.now().toEpochMilli());
    
    // Calculated rates (messages per second)
    private volatile int incomingRate = 0;
    private volatile int processedRate = 0;
    
    // Rate history for chart visualization (last 10 minutes)
    private final ConcurrentLinkedQueue<RateSnapshot> rateHistory = new ConcurrentLinkedQueue<>();
    private static final int MAX_HISTORY_SIZE = 60; // 60 snapshots = 10 minutes at 10s intervals
    
    /**
     * Rate snapshot for time-series data
     */
    @Getter
    @AllArgsConstructor
    public static class RateSnapshot {
        private long timestamp;
        private int incomingRate;
        private int processedRate;
    }
    
    /**
     * Record incoming messages (pulled from RabbitMQ)
     */
    public void recordIncoming(int count) {
        incomingCount.addAndGet(count);
    }
    
    /**
     * Record processed messages (after classification and storage)
     */
    public void recordProcessed(int count) {
        processedCount.addAndGet(count);
    }
    
    /**
     * Calculate rates, reset counters, and store in history
     * Should be called periodically (e.g., every 10 seconds)
     */
    public void calculateRates() {
        long now = Instant.now().toEpochMilli();
        long lastReset = lastResetTime.get();
        
        // Calculate elapsed time in seconds
        double elapsedSeconds = (now - lastReset) / 1000.0;
        
        if (elapsedSeconds > 0) {
            // Calculate rates (messages per second)
            int incoming = incomingCount.get();
            int processed = processedCount.get();
            
            this.incomingRate = (int) (incoming / elapsedSeconds);
            this.processedRate = (int) (processed / elapsedSeconds);
            
            // Store snapshot in history
            RateSnapshot snapshot = new RateSnapshot(now, incomingRate, processedRate);
            rateHistory.offer(snapshot);
            
            // Maintain max history size (circular buffer behavior)
            while (rateHistory.size() > MAX_HISTORY_SIZE) {
                rateHistory.poll();
            }
            
            log.debug("Metrics calculated: incoming={}/s, processed={}/s (period={}s, history size={})", 
                    incomingRate, processedRate, String.format("%.1f", elapsedSeconds), rateHistory.size());
            
            // Reset counters
            incomingCount.set(0);
            processedCount.set(0);
            lastResetTime.set(now);
        }
    }
    
    /**
     * Get current incoming rate (messages/second)
     */
    public int getIncomingRate() {
        calculateRates(); // Auto-calculate on get
        return incomingRate;
    }
    
    /**
     * Get current processed rate (messages/second)
     */
    public int getProcessedRate() {
        calculateRates(); // Auto-calculate on get
        return processedRate;
    }
    
    /**
     * Get rate history for chart visualization
     * Returns list ordered by timestamp (oldest to newest)
     */
    public List<RateSnapshot> getRateHistory() {
        calculateRates(); // Ensure latest data is included
        return new ArrayList<>(rateHistory);
    }
    
    /**
     * Clear all history (useful for testing or reset)
     */
    public void clearHistory() {
        rateHistory.clear();
        log.info("Rate history cleared");
    }
}
