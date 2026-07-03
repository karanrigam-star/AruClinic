package com.aruclinic.repository;

import com.aruclinic.entity.Doctor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Doctor} entity.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByEmail(String email);

    Optional<Doctor> findByMobileNumber(String mobileNumber);

    List<Doctor> findByDepartment(String department);

    List<Doctor> findBySpecialization(String specialization);

    List<Doctor> findByNameContainingIgnoreCase(String name);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);

    List<Doctor> findByDepartmentContainingIgnoreCase(String department);

    List<Doctor> findByNameContainingIgnoreCaseOrSpecializationContainingIgnoreCaseOrDepartmentContainingIgnoreCase(
            String name, String specialization, String department);

    @Query("SELECT d FROM Doctor d WHERE d.experience >= :years")
    List<Doctor> findByExperienceGreaterThanEqual(Integer years, Pageable pageable);
}