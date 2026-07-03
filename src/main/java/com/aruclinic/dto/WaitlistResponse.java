package com.aruclinic.dto;

/**
 * Response DTO for waitlist/appointment operations.
 */
public class WaitlistResponse {

    private String message;
    private boolean success;

    public WaitlistResponse() {}

    public WaitlistResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}