package com.smartcity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service để track ingestion metrics (incoming and processed rates)
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
     * Calculate rates and reset counters
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
            
            log.debug("Metrics calculated: incoming={}/s, processed={}/s (period={}s)", 
                    incomingRate, processedRate, String.format("%.1f", elapsedSeconds));
            
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
}
