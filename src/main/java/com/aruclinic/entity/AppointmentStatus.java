package com.aruclinic.entity;

/**
 * Enum representing the status of an appointment.
 */
public enum AppointmentStatus {
    SCHEDULED,
    CHECKED_IN,
    WAITING,
    IN_CONSULTATION,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}