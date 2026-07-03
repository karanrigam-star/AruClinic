package com.aruclinic.repository;

import com.aruclinic.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByEmailAndMobileNumberOrderByCreatedAtDesc(String email, String mobileNumber);
    Optional<OtpVerification> findTopByOtpCodeAndExpiresAtAfter(String otpCode, LocalDateTime now);
    void deleteAllByExpiresAtBefore(LocalDateTime dateTime);
    List<OtpVerification> findByEmailContainingOrMobileNumberContaining(String email, String mobileNumber);
    List<OtpVerification> findAllByOrderByCreatedAtDesc();
}