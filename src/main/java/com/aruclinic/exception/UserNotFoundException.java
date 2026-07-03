package com.aruclinic.exception;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends AruClinicException {
    public UserNotFoundException(String message) {
        super(message);
    }
}