package com.aruclinic.exception;

/**
 * Exception thrown when email already exists.
 */
public class UniqueEmailException extends AruClinicException {
    public UniqueEmailException(String message) {
        super(message);
    }
}