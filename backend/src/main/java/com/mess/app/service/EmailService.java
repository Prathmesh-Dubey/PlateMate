package com.mess.app.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendOtpEmail(String to, String otp, String purpose);
}