package com.aruclinic.util;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Utility class for sending emails using JavaMailSender.
 */
@Component
public class EmailUtil {

    private final JavaMailSender mailSender;

    public EmailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a simple email.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@aruclinic.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Gmail SMTP failed to send email to " + to + ": " + e.getMessage());
            // Fallback to console logging for development/testing if SMTP credentials are not set/correct
            System.out.println("[FALLBACK CONSOLE LOG] Sending email to: " + to);
            System.out.println("[FALLBACK CONSOLE LOG] Subject: " + subject);
            System.out.println("[FALLBACK CONSOLE LOG] Body: " + body);
        }
    }
}