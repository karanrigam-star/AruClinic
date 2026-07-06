package com.aruclinic.service.impl;

import com.aruclinic.dto.PrescriptionDto;
import com.aruclinic.dto.PrescriptionItemDto;
import com.aruclinic.entity.Doctor;
import com.aruclinic.entity.Patient;
import com.aruclinic.entity.Prescription;
import com.aruclinic.entity.PrescriptionItem;
import com.aruclinic.exception.AruClinicException;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.repository.PatientRepository;
import com.aruclinic.repository.PrescriptionItemRepository;
import com.aruclinic.repository.PrescriptionRepository;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.repository.NotificationRepository;
import com.aruclinic.service.PrescriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository,
                                   PrescriptionItemRepository prescriptionItemRepository,
                                   PatientRepository patientRepository,
                                   DoctorRepository doctorRepository,
                                   UserRepository userRepository,
                                   NotificationRepository notificationRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionItemRepository = prescriptionItemRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public PrescriptionDto createPrescription(PrescriptionDto dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new AruClinicException("Patient not found with ID: " + dto.getPatientId()));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new AruClinicException("Doctor not found with ID: " + dto.getDoctorId()));

        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setSymptoms((dto.getSymptoms() == null || dto.getSymptoms().trim().isEmpty()) ? "None" : dto.getSymptoms().trim());
        prescription.setDiagnosis((dto.getDiagnosis() == null || dto.getDiagnosis().trim().isEmpty()) ? "None" : dto.getDiagnosis().trim());
        prescription.setAdvice(dto.getAdvice());
        prescription.setPrescriptionDate(dto.getPrescriptionDate() != null ? dto.getPrescriptionDate() : LocalDate.now());
        prescription.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        prescription.setFollowUpDate(dto.getFollowUpDate());

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        List<PrescriptionItem> savedItems = new ArrayList<>();
        if (dto.getItems() != null) {
            for (PrescriptionItemDto itemDto : dto.getItems()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setPrescription(savedPrescription);
                item.setMedicineName(itemDto.getMedicineName());
                item.setDosage(itemDto.getDosage());
                item.setDuration(itemDto.getDuration() != null ? itemDto.getDuration() : 0);
                savedItems.add(prescriptionItemRepository.save(item));
            }
        }
        savedPrescription.setItems(savedItems);

        PrescriptionDto resultDto = mapToDto(savedPrescription);

        // Send notification to the patient
        try {
            if (patient.getEmail() != null) {
                userRepository.findByEmail(patient.getEmail()).ifPresent(patientUser -> {
                    com.aruclinic.entity.Notification notif = new com.aruclinic.entity.Notification();
                    notif.setUser(patientUser);
                    notif.setTitle("New Prescription Issued");
                    notif.setMessage("Dr. " + doctor.getName() + " has issued a new prescription for you. Diagnosis: " + resultDto.getDiagnosis());
                    notif.setRead(false);
                    notif.setCreatedAt(java.time.LocalDateTime.now());
                    notificationRepository.save(notif);
                });
            }
        } catch (Exception ex) {
            // Ignore notification delivery failure
        }

        // Send notification to all admin users
        try {
            List<com.aruclinic.entity.User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRoles() != null && u.getRoles().stream().anyMatch(r -> r.getName() != null && r.getName().contains("ADMIN")))
                .collect(Collectors.toList());
            for (com.aruclinic.entity.User adminUser : admins) {
                com.aruclinic.entity.Notification notif = new com.aruclinic.entity.Notification();
                notif.setUser(adminUser);
                notif.setTitle("Prescription Created");
                String patName = patient.getFirstName() + " " + patient.getLastName();
                notif.setMessage("Dr. " + doctor.getName() + " created a new prescription for patient " + patName + ".");
                notif.setRead(false);
                notif.setCreatedAt(java.time.LocalDateTime.now());
                notificationRepository.save(notif);
            }
        } catch (Exception ex) {
            // Ignore admin notification delivery failure
        }

        return resultDto;
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionDto getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new AruClinicException("Prescription not found with ID: " + id));
        return mapToDto(prescription);
    }

    @Override
    @Transactional
    public PrescriptionDto updatePrescription(Long id, PrescriptionDto dto) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new AruClinicException("Prescription not found with ID: " + id));

        prescription.setSymptoms((dto.getSymptoms() == null || dto.getSymptoms().trim().isEmpty()) ? "None" : dto.getSymptoms().trim());
        prescription.setDiagnosis((dto.getDiagnosis() == null || dto.getDiagnosis().trim().isEmpty()) ? "None" : dto.getDiagnosis().trim());
        prescription.setAdvice(dto.getAdvice());
        prescription.setFollowUpDate(dto.getFollowUpDate());
        if (dto.getStatus() != null) {
            prescription.setStatus(dto.getStatus());
        }

        // Delete old items and insert updated ones
        prescriptionItemRepository.deleteAll(prescription.getItems());
        prescriptionItemRepository.flush();
        prescription.getItems().clear();

        List<PrescriptionItem> updatedItems = new ArrayList<>();
        if (dto.getItems() != null) {
            for (PrescriptionItemDto itemDto : dto.getItems()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setPrescription(prescription);
                item.setMedicineName(itemDto.getMedicineName());
                item.setDosage(itemDto.getDosage());
                item.setDuration(itemDto.getDuration() != null ? itemDto.getDuration() : 0);
                updatedItems.add(prescriptionItemRepository.save(item));
            }
        }
        prescription.setItems(updatedItems);
        prescriptionItemRepository.flush();

        Prescription updatedPrescription = prescriptionRepository.saveAndFlush(prescription);
        return mapToDto(updatedPrescription);
    }

    @Override
    @Transactional
    public void deletePrescription(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new AruClinicException("Prescription not found with ID: " + id));

        Patient patient = prescription.getPatient();
        Doctor doctor = prescription.getDoctor();
        String prescriptionId = "PRESC-" + String.format("%04d", prescription.getId());

        prescription.setStatus("DELETED_BY_DOCTOR");
        prescriptionRepository.save(prescription);
        prescriptionRepository.flush();

        // Send notification to the patient
        try {
            if (patient != null && patient.getEmail() != null) {
                userRepository.findByEmail(patient.getEmail()).ifPresent(patientUser -> {
                    com.aruclinic.entity.Notification notif = new com.aruclinic.entity.Notification();
                    notif.setUser(patientUser);
                    notif.setTitle("Prescription Deleted: " + prescriptionId);
                    String docName = doctor != null ? doctor.getName() : "Doctor";
                    notif.setMessage("Dr. " + docName + " has deleted prescription " + prescriptionId + ". Click 'View' to download your PDF copy. Once downloaded, it will be removed.");
                    notif.setRead(false);
                    notif.setCreatedAt(java.time.LocalDateTime.now());
                    notificationRepository.save(notif);
                });
            }
        } catch (Exception ex) {
            // Ignore
        }

        // Send notification to all admin users
        try {
            List<com.aruclinic.entity.User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRoles() != null && u.getRoles().stream().anyMatch(r -> r.getName() != null && r.getName().contains("ADMIN")))
                .collect(Collectors.toList());
            for (com.aruclinic.entity.User adminUser : admins) {
                com.aruclinic.entity.Notification notif = new com.aruclinic.entity.Notification();
                notif.setUser(adminUser);
                notif.setTitle("Prescription Deleted");
                String patName = patient != null ? patient.getFirstName() + " " + patient.getLastName() : "Patient";
                String docName = doctor != null ? doctor.getName() : "Doctor";
                notif.setMessage("Dr. " + docName + " has deleted prescription " + prescriptionId + " for patient " + patName + ".");
                notif.setRead(false);
                notif.setCreatedAt(java.time.LocalDateTime.now());
                notificationRepository.save(notif);
            }
        } catch (Exception ex) {
            // Ignore
        }
    }

    @Override
    @Transactional
    public void deletePrescriptionReal(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new AruClinicException("Prescription not found with ID: " + id));
        prescriptionRepository.delete(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getAllPrescriptions() {
        return prescriptionRepository.findAll().stream()
                .filter(p -> !"DELETED_BY_DOCTOR".equalsIgnoreCase(p.getStatus()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPrescriptionsByPatientId(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPrescriptionsByDoctorId(Long doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId).stream()
                .filter(p -> !"DELETED_BY_DOCTOR".equalsIgnoreCase(p.getStatus()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getActivePrescriptionsByPatientId(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId).stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDto> searchPrescriptions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPrescriptions();
        }
        String cleanQuery = query.trim();
        if (cleanQuery.toLowerCase().startsWith("presc-")) {
            cleanQuery = cleanQuery.substring(6).trim();
        }
        return prescriptionRepository.searchPrescriptionsGlobal(cleanQuery).stream()
                .filter(p -> !"DELETED_BY_DOCTOR".equalsIgnoreCase(p.getStatus()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private PrescriptionDto mapToDto(Prescription entity) {
        if (entity == null) {
            return null;
        }
        PrescriptionDto dto = new PrescriptionDto();
        dto.setId(entity.getId());
        dto.setPrescriptionId("PRESC-" + String.format("%04d", entity.getId()));
        dto.setPrescriptionDate(entity.getPrescriptionDate());
        if (entity.getPatient() != null) {
            dto.setPatientId(entity.getPatient().getId());
            dto.setPatientName(entity.getPatient().getFirstName() + " " + entity.getPatient().getLastName());
        }
        if (entity.getDoctor() != null) {
            dto.setDoctorId(entity.getDoctor().getId());
            dto.setDoctorName(entity.getDoctor().getName());
        }
        dto.setSymptoms(entity.getSymptoms());
        dto.setDiagnosis(entity.getDiagnosis());
        dto.setAdvice(entity.getAdvice());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus() : "ACTIVE");
        dto.setFollowUpDate(entity.getFollowUpDate());

        List<PrescriptionItemDto> itemDtos = new ArrayList<>();
        if (entity.getItems() != null) {
            for (PrescriptionItem item : entity.getItems()) {
                itemDtos.add(new PrescriptionItemDto(
                        item.getId(),
                        entity.getId(),
                        item.getMedicineName(),
                        item.getDosage(),
                        item.getDuration()
                ));
            }
        }
        dto.setItems(itemDtos);
        return dto;
    }
}
