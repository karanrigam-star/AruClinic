package com.aruclinic.service;

import com.aruclinic.entity.*;
import java.util.List;

public interface AdminService {
    // Stats
    long getTotalPatients();
    long getTotalDoctors();
    long getTotalReceptionists();
    long getTotalUsers();
    long getTodaysAppointments();
    long getWaitingPatients();
    long getCompletedConsultations();
    double getRevenueToday();
    double getRevenueThisMonth();
    long getPendingBillsCount();
    long getNewRegistrationsCount();

    // User Management
    List<User> getAllUsers();
    User createUser(User user, String roleName);
    User updateUser(Long id, User user, String roleName);
    void deleteUser(Long id);
    void resetUserPassword(Long id, String newPassword);
    void toggleUserStatus(Long id, boolean enabled);
    boolean isUserEnabled(Long id);
    User getUserByEmail(String email);

    // Doctor Management
    List<Doctor> getAllDoctors();
    Doctor createDoctor(Doctor doctor);
    Doctor updateDoctor(Long id, Doctor doctor);
    void deleteDoctor(Long id);

    // Receptionist Management
    List<User> getReceptionists();

    // Patient Management
    List<Patient> getAllPatients();
    Patient createPatient(Patient patient);
    Patient updatePatient(Long id, Patient patient);
    void deletePatient(Long id);

    // Appointment Management
    List<Appointment> getAllAppointments();
    Appointment createAppointment(Appointment appointment);
    Appointment updateAppointment(Long id, Appointment appointment);
    void cancelAppointment(Long id);
    void cancelAppointment(Long id, String reason);
    void rescheduleAppointment(Long id, java.time.LocalDate date, java.time.LocalTime time, String reason);
    void rescheduleAppointment(Long id, java.time.LocalDate date, java.time.LocalTime time, String reason, Long newDoctorId);

    // Billing
    List<Bill> getAllBills();
    Bill createBill(Bill bill);
    Bill updateBill(Long id, Bill bill);
    void payBill(Long id);
    void payBill(Long id, String paymentMethod);

    // Audit Logs
    List<AuditLog> getAuditLogs();

    // Settings
    String getClinicSetting(String key, String defaultValue);
    void saveClinicSetting(String key, String value);

    // Repository Decoupling Helpers
    java.util.Optional<Patient> findPatientByEmail(String email);
    Patient savePatient(Patient patient);
    java.util.Optional<Doctor> findDoctorByEmail(String email);
    Doctor saveDoctor(Doctor doctor);
    User saveUser(User user);
}
