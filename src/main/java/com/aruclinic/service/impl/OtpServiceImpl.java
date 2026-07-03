package com.aruclinic.service.impl;

import com.aruclinic.entity.OtpVerification;
import com.aruclinic.repository.OtpVerificationRepository;
import com.aruclinic.service.OtpService;
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

    public OtpServiceImpl(OtpVerificationRepository otpVerificationRepository) {
        this.otpVerificationRepository = otpVerificationRepository;
    }

    @Override
    @Transactional
    public OtpVerification saveOtp(OtpVerification otpVerification) {
        return otpVerificationRepository.save(otpVerification);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OtpVerification> findByEmailAndMobileNumber(String email, String mobileNumber) {
        return otpVerificationRepository.findTopByEmailAndMobileNumberOrderByCreatedAtDesc(email, mobileNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OtpVerification> findByOtpCode(String otpCode) {
        return otpVerificationRepository.findTopByOtpCodeAndExpiresAtAfter(otpCode, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteOtp(OtpVerification otpVerification) {
        otpVerificationRepository.delete(otpVerification);
    }

    @Override
    @Transactional
    public void deleteExpiredOtps() {
        otpVerificationRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }

    @Override
    @Transactional
    public OtpVerification generateOtp(String email, String mobileNumber) {
        // Generate new OTP
        String otp = String.valueOf(new Random().nextInt(899999) + 100000);

        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(email);
        otpVerification.setMobileNumber(mobileNumber);
        otpVerification.setOtpCode(otp);
        otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpVerification.setVerified(false);

        return saveOtp(otpVerification);
    }
}