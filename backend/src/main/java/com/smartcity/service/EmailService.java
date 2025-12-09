package com.smartcity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send OTP email to user
     */
    public boolean sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("nguyenhuyhoangpt0402@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Smart City Platform - Mã xác thực OTP");
            message.setText(
                "Xin chào,\n\n" +
                "Mã OTP của bạn là: " + otp + "\n\n" +
                "Mã này có hiệu lực trong 5 phút.\n\n" +
                "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.\n\n" +
                "Trân trọng,\n" +
                "Smart City Platform Team"
            );

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage(), e);
            return false;
        }
    }
}
