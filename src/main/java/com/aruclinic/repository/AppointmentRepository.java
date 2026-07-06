package com.aruclinic.repository;

import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM appointments WHERE CAST(CONCAT(appointment_date, ' ', appointment_time) AS DATETIME) BETWEEN :start AND :end", nativeQuery = true)
    List<Appointment> findByAppointmentDateTimeBetween(@org.springframework.data.repository.query.Param("start") LocalDateTime start, 
                                                       @org.springframework.data.repository.query.Param("end") LocalDateTime end);
    List<Appointment> findByStatus(AppointmentStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT a.patient.id FROM Appointment a WHERE a.doctor.id = :doctorId")
    List<Long> findPatientIdsByDoctorId(@org.springframework.data.repository.query.Param("doctorId") Long doctorId);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE a.doctor.id = :doctorId")
    List<Appointment> findByDoctorIdWithDetails(@org.springframework.data.repository.query.Param("doctorId") Long doctorId);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date")
    List<Appointment> findByDoctorIdAndAppointmentDateWithDetails(@org.springframework.data.repository.query.Param("doctorId") Long doctorId,
                                                                 @org.springframework.data.repository.query.Param("date") java.time.LocalDate date);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Appointment a SET a.status = :status, a.reason = :reason, a.updatedAt = CURRENT_TIMESTAMP WHERE a.id = :id")
    void cancelAppointmentById(@org.springframework.data.repository.query.Param("id") Long id, 
                               @org.springframework.data.repository.query.Param("status") AppointmentStatus status, 
                               @org.springframework.data.repository.query.Param("reason") String reason);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Appointment a SET a.appointmentDate = :date, a.appointmentTime = :time, a.reason = :reason, a.updatedAt = CURRENT_TIMESTAMP WHERE a.id = :id")
    void rescheduleAppointmentById(@org.springframework.data.repository.query.Param("id") Long id, 
                                   @org.springframework.data.repository.query.Param("date") java.time.LocalDate date, 
                                   @org.springframework.data.repository.query.Param("time") java.time.LocalTime time, 
                                   @org.springframework.data.repository.query.Param("reason") String reason);

    long countByAppointmentDate(java.time.LocalDate date);
    List<Appointment> findTop5ByOrderByCreatedAtDesc();
}