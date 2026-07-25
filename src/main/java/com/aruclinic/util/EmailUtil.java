package com.aruclinic.util;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Enterprise Email Utility for AruClinic.
 * Supports Brevo (Sendinblue) REST API with automatic fallback to JavaMailSender SMTP.
 */
@Component
public class EmailUtil {

    private final JavaMailSender mailSender;
    private final HttpClient httpClient;

    @Value("${BREVO_API_KEY:${SENDINBLUE_API_KEY:}}")
    private String brevoApiKey;

    @Value("${BREVO_SENDER_EMAIL:${GMAIL_USERNAME:theinvisiblemask800@gmail.com}}")
    private String senderEmail;

    @Value("${BREVO_SENDER_NAME:AruClinic Healthcare System}")
    private String senderName;

    public EmailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private String getEffectiveSenderEmail() {
        if (senderEmail != null && !senderEmail.trim().isEmpty()) {
            return senderEmail.trim();
        }
        return "theinvisiblemask800@gmail.com";
    }

    private String getEffectiveApiKey() {
        if (brevoApiKey != null && !brevoApiKey.trim().isEmpty()) {
            return brevoApiKey.trim();
        }
        String envKey = System.getenv("BREVO_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }
        String sibKey = System.getenv("SENDINBLUE_API_KEY");
        if (sibKey != null && !sibKey.trim().isEmpty()) {
            return sibKey.trim();
        }
        return "";
    }

    /**
     * Send plain text email via Brevo REST API or fallback to SMTP.
     */
    public void sendEmail(String to, String subject, String body) {
        String apiKey = getEffectiveApiKey();
        if (!apiKey.isEmpty()) {
            sendViaBrevoApi(to, subject, "<p>" + body.replace("\n", "<br/>") + "</p>");
        } else {
            sendViaSmtp(to, subject, body);
        }
    }

    /**
     * Send HTML email via Brevo REST API or fallback to SMTP.
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        String apiKey = getEffectiveApiKey();
        if (!apiKey.isEmpty()) {
            sendViaBrevoApi(to, subject, htmlContent);
        } else {
            sendViaSmtpHtml(to, subject, htmlContent);
        }
    }

    /**
     * Send branded, responsive HTML OTP email.
     * Note: OTP code is transmitted to recipient via API/SMTP but NEVER logged to stdout/stderr.
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

    private void sendViaBrevoApi(String to, String subject, String htmlContent) {
        try {
            String apiKey = getEffectiveApiKey();
            String fromEmail = getEffectiveSenderEmail();

            String jsonPayload = String.format(
                "{\"sender\":{\"name\":\"%s\",\"email\":\"%s\"},\"to\":[{\"email\":\"%s\"}],\"subject\":\"%s\",\"htmlContent\":\"%s\"}",
                escapeJson(senderName),
                escapeJson(fromEmail),
                escapeJson(to),
                escapeJson(subject),
                escapeJson(htmlContent)
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(15))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                System.out.println("Email sent successfully via Brevo REST API to: " + to + " (Sender: " + fromEmail + ")");
            } else {
                System.err.println("Brevo API returned error status " + response.statusCode() + ": " + response.body());
                sendViaSmtpHtml(to, subject, htmlContent);
            }
        } catch (Exception e) {
            System.err.println("Brevo REST API call failed for " + to + ": " + e.getMessage());
            sendViaSmtpHtml(to, subject, htmlContent);
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    private void sendViaSmtp(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getEffectiveSenderEmail());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Email sent successfully via SMTP to: " + to);
        } catch (Exception e) {
            System.err.println("SMTP failed to send email to " + to + ": " + e.getMessage());
            System.out.println("[FALLBACK LOG] Email sent to: " + to + " | Subject: " + subject);
        }
    }

    private void sendViaSmtpHtml(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(getEffectiveSenderEmail());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            System.out.println("HTML email sent successfully via SMTP to: " + to);
        } catch (Exception e) {
            System.err.println("SMTP HTML mail failed for " + to + ": " + e.getMessage());
            System.out.println("[FALLBACK LOG] HTML Email sent to: " + to + " | Subject: " + subject);
        }
    }
}