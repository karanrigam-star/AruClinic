package com.aruclinic.exception;

/**
 * Exception thrown when mobile number already exists.
 */
public class UniqueMobileException extends AruClinicException {
    public UniqueMobileException(String message) {
        super(message);
    }
}