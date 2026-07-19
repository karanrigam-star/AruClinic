package com.aruclinic.exception;

/**
 * Exception thrown when an appointment slot is already booked (double-booking protection).
 */
public class AppointmentSlotConflictException extends AruClinicException {
    public AppointmentSlotConflictException(String message) {
        super(message);
    }
}
