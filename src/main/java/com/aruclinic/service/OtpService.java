package com.aruclinic.service;

import com.aruclinic.entity.OtpVerification;
import java.util.Optional;

/**
 * Service interface for OTP operations.
 */
public interface OtpService {

    OtpVerification saveOtp(OtpVerification otpVerification);

    Optional<OtpVerification> findByEmailAndMobileNumber(String email, String mobileNumber);

    void deleteOtp(OtpVerification otpVerification);

    void deleteExpiredOtps();

    Optional<OtpVerification> findByOtpCode(String otpCode);

    OtpVerification generateOtp(String email, String mobileNumber);
}