package com.aruclinic.dto;

import java.time.LocalDateTime;

/**
 * DTO for OtpVerification entity.
 */
public class OtpVerificationDto {

    private Long id;
    private String email;
    private String mobileNumber;
    private String otpCode;
    private LocalDateTime expiresAt;
    private boolean verified;
    private LocalDateTime createdAt;

    public OtpVerificationDto() {}

    public OtpVerificationDto(Long id, String email, String mobileNumber, String otpCode,
                               LocalDateTime expiresAt, boolean verified, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.otpCode = otpCode;
        this.expiresAt = expiresAt;
        this.verified = verified;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}