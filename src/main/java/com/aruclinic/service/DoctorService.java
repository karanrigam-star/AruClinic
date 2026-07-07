package com.aruclinic.service;

import com.aruclinic.dto.DoctorDto;
import java.util.List;

/**
 * Service interface for doctor management operations.
 */
public interface DoctorService {

    DoctorDto createDoctor(DoctorDto doctorDto);

    DoctorDto getDoctorById(Long id);

    DoctorDto updateDoctor(Long id, DoctorDto doctorDto);

    void deleteDoctor(Long id);

    List<DoctorDto> getAllDoctors();

    List<DoctorDto> getDoctorsBySpecialization(String specialization);

    List<DoctorDto> getDoctorsByDepartment(String department);

    List<DoctorDto> searchDoctors(String query);

    DoctorDto getDoctorByEmail(String email);

    List<DoctorDto> getAvailableDoctors();

    com.aruclinic.entity.Doctor getDoctorEntityById(Long id);

    List<com.aruclinic.entity.Doctor> getDoctorsBySpecializationEntity(String specialization);
}
