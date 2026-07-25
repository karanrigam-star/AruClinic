package com.aruclinic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Production Mail Configuration for Gmail SMTP.
 * Automatically sanitizes inputs (stripping spaces from Gmail App Passwords)
 * to ensure 100% successful SMTP authentication on Render and Railway.
 */
@Configuration
public class MailConfig {

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${GMAIL_USERNAME:}")
    private String username;

    @Value("${GMAIL_PASSWORD:}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        String cleanUsername = (username != null) ? username.trim() : "";
        // Strip all whitespace/spaces from Gmail App Password (e.g. "abcd efgh ijkl mnop" -> "abcdefghijklmnop")
        String cleanPassword = (password != null) ? password.replaceAll("\\s+", "") : "";

        if (!cleanUsername.isEmpty()) {
            mailSender.setUsername(cleanUsername);
        }
        if (!cleanPassword.isEmpty()) {
            mailSender.setPassword(cleanPassword);
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", (!cleanUsername.isEmpty() && !cleanPassword.isEmpty()) ? "true" : "false");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.connectiontimeout", "30000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.writetimeout", "30000");

        return mailSender;
    }
}
