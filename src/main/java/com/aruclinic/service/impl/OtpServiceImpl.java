package com.aruclinic.service.impl;

import com.aruclinic.entity.OtpVerification;
import com.aruclinic.repository.OtpVerificationRepository;
import com.aruclinic.service.OtpService;
import com.aruclinic.util.EmailUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Production-ready implementation of OtpService featuring:
 * 1. Cryptographically secure 6-digit OTP generation via SecureRandom.
 * 2. SHA-256 OTP hashing with 5-minute expiry in DB.
 * 3. 60-second resend rate-limiting / cooldown.
 * 4. Zero sensitive OTP value logging.
 * 5. Automatic HTML email delivery via Gmail SMTP.
 */
@Service
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailUtil emailUtil;
    private static final java.security.SecureRandom secureRandom = new java.security.SecureRandom();

    public OtpServiceImpl(OtpVerificationRepository otpVerificationRepository, EmailUtil emailUtil) {
        this.otpVerificationRepository = otpVerificationRepository;
        this.emailUtil = emailUtil;
    }

    @Override
    @Transactional
    @PreAuthorize("permitAll()")
    public OtpVerification saveOtp(OtpVerification otpVerification) {
        return otpVerificationRepository.save(otpVerification);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("permitAll()")
    public Optional<OtpVerification> findByEmailAndMobileNumber(String email, String mobileNumber) {
        return otpVerificationRepository.findTopByEmailAndMobileNumberOrderByCreatedAtDesc(email, mobileNumber);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("permitAll()")
    public Optional<OtpVerification> findByOtpCode(String otpCode) {
        return otpVerificationRepository.findTopByOtpCodeAndExpiresAtAfter(otpCode, LocalDateTime.now());
    }

    @Override
    @Transactional
    @PreAuthorize("permitAll()")
    public void deleteOtp(OtpVerification otpVerification) {
        otpVerificationRepository.delete(otpVerification);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public void deleteExpiredOtps() {
        otpVerificationRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }

    @Override
    @Transactional
    @PreAuthorize("permitAll()")
    public OtpVerification generateOtp(String email, String mobileNumber) {
        // Enforce 60-second resend cooldown rate-limiting
        Optional<OtpVerification> existingOtpOpt = findByEmailAndMobileNumber(email, mobileNumber);
        if (existingOtpOpt.isPresent() && existingOtpOpt.get().getCreatedAt() != null) {
            LocalDateTime lastCreatedAt = existingOtpOpt.get().getCreatedAt();
            long elapsedSeconds = Duration.between(lastCreatedAt, LocalDateTime.now()).getSeconds();
            if (elapsedSeconds < 60) {
                long waitSeconds = 60 - elapsedSeconds;
                throw new IllegalStateException("Resend cooldown active. Please wait " + waitSeconds + " seconds before requesting a new OTP.");
            }
        }

        // Generate cryptographically secure 6-digit OTP code using SecureRandom
        String rawOtp = String.valueOf(secureRandom.nextInt(900000) + 100000);

        // Hash the OTP using SHA-256 for secure database storage
        String hashedOtp;
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawOtp.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            hashedOtp = hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to compute SHA-256 OTP hash", e);
        }

        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(email);
        otpVerification.setMobileNumber(mobileNumber);
        otpVerification.setOtpCode(hashedOtp);
        otpVerification.setRawOtpCode(rawOtp); // Transient field for email transmission
        otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpVerification.setVerified(false);
        otpVerification.setAttempts(0);

        OtpVerification savedOtp = saveOtp(otpVerification);

        // Log OTP generation WITHOUT logging the raw OTP code
        System.out.println("Secure 6-digit OTP generated and hashed for recipient: " + email + " (5-minute expiry)");

        // Send branded HTML email via Gmail SMTP
        try {
            emailUtil.sendOtpEmail(email, rawOtp, 5);
        } catch (Exception e) {
            System.err.println("Failed to transmit OTP email to " + email + ": " + e.getMessage());
        }

        return savedOtp;
    }
}
