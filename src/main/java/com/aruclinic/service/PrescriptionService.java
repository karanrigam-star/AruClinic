package com.aruclinic.service;

import com.aruclinic.dto.PrescriptionDto;
import java.util.List;

/**
 * Service interface for prescription management operations.
 */
public interface PrescriptionService {

    PrescriptionDto createPrescription(PrescriptionDto prescriptionDto);

    PrescriptionDto getPrescriptionById(Long id);

    PrescriptionDto updatePrescription(Long id, PrescriptionDto prescriptionDto);

    void deletePrescription(Long id);

    List<PrescriptionDto> getAllPrescriptions();

    List<PrescriptionDto> getPrescriptionsByPatientId(Long patientId);

    List<PrescriptionDto> getPrescriptionsByDoctorId(Long doctorId);

    List<PrescriptionDto> getActivePrescriptionsByPatientId(Long patientId);

    List<PrescriptionDto> searchPrescriptions(String query);
}
