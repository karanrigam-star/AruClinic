package com.aruclinic.service;

import com.aruclinic.dto.PatientDto;
import java.util.List;

/**
 * Service interface for patient management operations.
 */
public interface PatientService {

    PatientDto createPatient(PatientDto patientDto);

    PatientDto getPatientById(Long id);

    PatientDto updatePatient(Long id, PatientDto patientDto);

    void deletePatient(Long id);

    List<PatientDto> getAllPatients();

    List<PatientDto> searchPatients(String query);

    PatientDto getPatientByEmail(String email);

    PatientDto getPatientByMobileNumber(String mobileNumber);

    List<PatientDto> getPatientsByDoctorId(Long doctorId);

    com.aruclinic.entity.Patient getPatientEntityById(Long id);

    com.aruclinic.entity.Patient getPatientEntityByEmail(String email);

    List<com.aruclinic.entity.Patient> getAllPatientEntities();

    List<com.aruclinic.entity.Patient> getPatientEntitiesByIds(List<Long> ids);

    com.aruclinic.entity.Patient savePatient(com.aruclinic.entity.Patient patient);
}
