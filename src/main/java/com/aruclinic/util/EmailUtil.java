package com.aruclinic.util;

import org.springframework.stereotype.Component;

/**
 * Utility class for sending emails.
 * This is a stub implementation - in production, integrate with an actual mail service.
 */
@Component
public class EmailUtil {

    /**
     * Send a simple email.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     */
    public void sendEmail(String to, String subject, String body) {
        // For development/testing purposes, just log the email
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        // In a real application, you would use JavaMailSender or similar
    }
}