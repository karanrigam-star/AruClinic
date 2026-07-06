package com.aruclinic.repository;

import com.aruclinic.entity.Prescription;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link Prescription} entity.
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatientId(Long patientId);

    List<Prescription> findByDoctorId(Long doctorId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p FROM Prescription p " +
            "LEFT JOIN FETCH p.patient pat " +
            "LEFT JOIN FETCH p.doctor doc " +
            "LEFT JOIN FETCH p.items it " +
            "WHERE LOWER(pat.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(pat.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(CONCAT(pat.firstName, ' ', pat.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(doc.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.diagnosis) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.symptoms) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(it.medicineName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR CAST(p.id AS string) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Prescription> searchPrescriptionsGlobal(@org.springframework.data.repository.query.Param("query") String query);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p.patient.id FROM Prescription p WHERE p.doctor.id = :doctorId")
    List<Long> findPatientIdsByDoctorId(@org.springframework.data.repository.query.Param("doctorId") Long doctorId);

    List<Prescription> findTop5ByOrderByCreatedAtDesc();
}