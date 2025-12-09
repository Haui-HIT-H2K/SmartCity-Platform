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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OtpService {

    @Autowired
    private EmailService emailService;

    // Store OTP with expiration: email -> {otp, expireTime}
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    private static final int OTP_VALIDITY_MINUTES = 5;

    /**
     * Generate and send OTP to email
     */
    public boolean generateAndSendOtp(String email) {
        String otp = generateOtp();
        
        // Store OTP with expiration time
        OtpData otpData = new OtpData(otp, LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        otpStore.put(email, otpData);
        
        log.info("Generated OTP for {}: {}", email, otp);
        
        // Send email
        boolean sent = emailService.sendOtpEmail(email, otp);
        
        if (!sent) {
            otpStore.remove(email);
            return false;
        }
        
        return true;
    }

    /**
     * Verify OTP for email
     */
    public boolean verifyOtp(String email, String otp) {
        OtpData otpData = otpStore.get(email);
        
        if (otpData == null) {
            log.warn("No OTP found for email: {}", email);
            return false;
        }
        
        // Check if expired
        if (LocalDateTime.now().isAfter(otpData.expireTime)) {
            log.warn("OTP expired for email: {}", email);
            otpStore.remove(email);
            return false;
        }
        
        // Verify OTP
        if (otpData.otp.equals(otp)) {
            log.info("OTP verified successfully for: {}", email);
            otpStore.remove(email); // Remove after successful verification
            return true;
        }
        
        log.warn("Invalid OTP for email: {}", email);
        return false;
    }

    /**
     * Generate 6-digit OTP
     */
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Inner class to store OTP data
     */
    private static class OtpData {
        String otp;
        LocalDateTime expireTime;

        OtpData(String otp, LocalDateTime expireTime) {
            this.otp = otp;
            this.expireTime = expireTime;
        }
    }
}
