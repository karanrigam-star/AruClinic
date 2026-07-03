package com.aruclinic.util;

import org.springframework.stereotype.Component;

/**
 * Utility class for sending SMS messages.
 * In production, integrate with a real SMS service (Twilio, AWS SNS, etc.).
 */
@Component
public class SmsUtil {

    public void sendSms(String mobileNumber, String message) {
        // In production: use Twilio, AWS SNS, or similar service
        // For now, just log to console
        System.out.println("Sending SMS to: " + mobileNumber);
        System.out.println("Message: " + message);
    }
}