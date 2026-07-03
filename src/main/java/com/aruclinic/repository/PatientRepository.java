package com.aruclinic.repository;

import com.aruclinic.entity.Patient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Patient} entity.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmail(String email);

    Optional<Patient> findByMobileNumber(String mobileNumber);

    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String firstName, String lastName, String email);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    @Query("SELECT p FROM Patient p WHERE p.age >= :age")
    List<Patient> findPatientsByAgeGreaterThanEqual(Integer age, Pageable pageable);

    long countByBloodGroup(String bloodGroup);
}