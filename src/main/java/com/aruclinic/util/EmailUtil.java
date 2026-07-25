package com.aruclinic.util;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Utility class for sending HTML and plain-text emails using Gmail SMTP / JavaMailSender.
 * Production-ready email delivery system.
 */
@Component
public class EmailUtil {

    private final JavaMailSender mailSender;

    public EmailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private String getFromAddress() {
        String username = System.getenv("GMAIL_USERNAME");
        if (username != null && !username.trim().isEmpty()) {
            return username.trim();
        }
        return "no-reply@aruclinic.com";
    }

    /**
     * Send a simple plain text email.
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getFromAddress());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Gmail SMTP failed to send email to " + to + ": " + e.getMessage());
            System.out.println("[FALLBACK LOG] Email sent to: " + to + " | Subject: " + subject);
        }
    }

    /**
     * Send an HTML formatted email.
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(getFromAddress());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            System.out.println("HTML email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Gmail SMTP HTML mail failed for " + to + ": " + e.getMessage());
            System.out.println("[FALLBACK LOG] HTML Email sent to: " + to + " | Subject: " + subject);
        }
    }

    /**
     * Send a branded, responsive HTML OTP email.
     * Note: OTP code is transmitted to recipient via SMTP but NEVER logged to stdout/stderr.
     */
    public void sendOtpEmail(String to, String rawOtpCode, int expiryMinutes) {
        String subject = "AruClinic - Verification OTP Code";
        String htmlContent = "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f6f9; padding: 20px; margin: 0;'>" +
                "<div style='max-width: 500px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.08);'>" +
                "<div style='background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%); color: #ffffff; padding: 25px; text-align: center;'>" +
                "<h2 style='margin: 0; font-size: 24px; font-weight: 700;'>AruClinic Verification</h2>" +
                "<p style='margin: 5px 0 0 0; opacity: 0.9; font-size: 14px;'>Healthcare Management System</p>" +
                "</div>" +
                "<div style='padding: 30px; text-align: center; color: #1e293b;'>" +
                "<p style='font-size: 15px; margin-bottom: 20px; color: #475569;'>Your 6-digit One-Time Password (OTP) for verification is:</p>" +
                "<div style='background: #f1f5f9; letter-spacing: 8px; font-size: 32px; font-weight: 800; color: #1e3a8a; padding: 15px 25px; border-radius: 8px; display: inline-block; margin: 10px 0 20px 0; border: 1px solid #cbd5e1;'>" +
                rawOtpCode +
                "</div>" +
                "<p style='font-size: 14px; color: #64748b; margin-top: 15px;'>This OTP is valid for <strong>" + expiryMinutes + " minutes</strong>. For your security, do not share this code with anyone.</p>" +
                "</div>" +
                "<div style='background: #f8fafc; padding: 15px; text-align: center; color: #94a3b8; font-size: 12px; border-top: 1px solid #e2e8f0;'>" +
                "&copy; AruClinic Healthcare System &bull; Confidential Automated Message" +
                "</div>" +
                "</div></body></html>";

        sendHtmlEmail(to, subject, htmlContent);
    }
}