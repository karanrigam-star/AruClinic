package com.aruclinic.dto;

/**
 * DTO version of AppointmentStatus for transfer over API.
 */
public enum AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}