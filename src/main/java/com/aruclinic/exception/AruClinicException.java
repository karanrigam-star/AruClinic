package com.aruclinic.exception;

/**
 * Base exception class for application-specific exceptions.
 */
public class AruClinicException extends RuntimeException {
    public AruClinicException(String message) {
        super(message);
    }

    public AruClinicException(String message, Throwable cause) {
        super(message, cause);
    }
}