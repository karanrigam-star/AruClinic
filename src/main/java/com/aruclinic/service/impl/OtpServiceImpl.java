package com.aruclinic.service.impl;

import com.aruclinic.entity.OtpVerification;
import com.aruclinic.repository.OtpVerificationRepository;
import com.aruclinic.service.OtpService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of OtpService.
 */
@Service
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private static final java.security.SecureRandom secureRandom = new java.security.SecureRandom();

    public OtpServiceImpl(OtpVerificationRepository otpVerificationRepository) {
        this.otpVerificationRepository = otpVerificationRepository;
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
        // Generate new OTP using SecureRandom
        String otp = String.valueOf(secureRandom.nextInt(900000) + 100000);

        // Hash the OTP using SHA-256 for secure storage
        String hashedOtp;
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(otp.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            hashedOtp = hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash OTP", e);
        }

        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(email);
        otpVerification.setMobileNumber(mobileNumber);
        otpVerification.setOtpCode(hashedOtp);
        otpVerification.setRawOtpCode(otp); // Store raw code in transient field for caller delivery
        otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpVerification.setVerified(false);

        return saveOtp(otpVerification);
    }
}
