package com.aruclinic.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for OTP verification requests.
 */
public class OtpVerificationRequestDto {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "OTP code is required")
    private String otpCode;

    public OtpVerificationRequestDto() {}

    public OtpVerificationRequestDto(String email, String otpCode) {
        this.email = email;
        this.otpCode = otpCode;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}