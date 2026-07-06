package com.aruclinic.service.impl;

import com.aruclinic.dto.PatientDto;
import com.aruclinic.entity.Patient;
import com.aruclinic.exception.UserNotFoundException;
import com.aruclinic.mapper.PatientMapper;
import com.aruclinic.repository.PatientRepository;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of PatientService.
 */
@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientMapper patientMapper;

    public PatientServiceImpl(PatientRepository patientRepository, UserRepository userRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.patientMapper = patientMapper;
    }

    @Override
    @Transactional
    public PatientDto createPatient(PatientDto patientDto) {
        Patient patient = patientMapper.toPatient(patientDto);

        // Calculate age from date of birth
        if (patientDto.getDateOfBirth() != null) {
            patient.setAge(calculateAge(patientDto.getDateOfBirth()));
        }

        Patient savedPatient = patientRepository.save(patient);
        return patientMapper.toPatientDto(savedPatient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDto getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Patient not found with id: " + id));
        return patientMapper.toPatientDto(patient);
    }

    @Override
    @Transactional
    public PatientDto updatePatient(Long id, PatientDto patientDto) {
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Patient not found with id: " + id));

        String oldEmail = existingPatient.getEmail();

        // Update fields
        if (patientDto.getFirstName() != null) {
            existingPatient.setFirstName(patientDto.getFirstName());
        }
        if (patientDto.getLastName() != null) {
            existingPatient.setLastName(patientDto.getLastName());
        }
        if (patientDto.getDateOfBirth() != null) {
            existingPatient.setDateOfBirth(patientDto.getDateOfBirth());
            existingPatient.setAge(calculateAge(patientDto.getDateOfBirth()));
        }
        if (patientDto.getGender() != null) {
            existingPatient.setGender(patientDto.getGender());
        }
        if (patientDto.getBloodGroup() != null) {
            existingPatient.setBloodGroup(patientDto.getBloodGroup());
        }
        if (patientDto.getMobileNumber() != null) {
            existingPatient.setMobileNumber(patientDto.getMobileNumber());
        }
        if (patientDto.getEmail() != null) {
            existingPatient.setEmail(patientDto.getEmail());
        }
        if (patientDto.getAddress() != null) {
            existingPatient.setAddress(patientDto.getAddress());
        }
        if (patientDto.getCity() != null) {
            existingPatient.setCity(patientDto.getCity());
        }
        if (patientDto.getState() != null) {
            existingPatient.setState(patientDto.getState());
        }
        if (patientDto.getDistrict() != null) {
            existingPatient.setDistrict(patientDto.getDistrict());
        }
        if (patientDto.getZipCode() != null) {
            existingPatient.setZipCode(patientDto.getZipCode());
        }
        if (patientDto.getEmergencyContact() != null) {
            existingPatient.setEmergencyContact(patientDto.getEmergencyContact());
        }
        if (patientDto.getEmergencyPhone() != null) {
            existingPatient.setEmergencyPhone(patientDto.getEmergencyPhone());
        }
        if (patientDto.getAllergies() != null) {
            existingPatient.setAllergies(patientDto.getAllergies());
        }
        if (patientDto.getMedicalHistory() != null) {
            existingPatient.setMedicalHistory(patientDto.getMedicalHistory());
        }

        Patient updatedPatient = patientRepository.save(existingPatient);

        // Sync with corresponding User entity
        if (oldEmail != null) {
            userRepository.findByEmail(oldEmail).ifPresent(user -> {
                if (patientDto.getFirstName() != null) {
                    user.setFirstName(patientDto.getFirstName());
                }
                if (patientDto.getLastName() != null) {
                    user.setLastName(patientDto.getLastName());
                }
                if (patientDto.getEmail() != null) {
                    user.setEmail(patientDto.getEmail());
                }
                if (patientDto.getMobileNumber() != null) {
                    user.setMobileNumber(patientDto.getMobileNumber());
                }
                userRepository.save(user);
            });
        }

        return patientMapper.toPatientDto(updatedPatient);
    }

    @Override
    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new UserNotFoundException("Patient not found with id: " + id);
        }
        patientRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> searchPatients(String query) {
        return patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        query, query, query)
                .stream()
                .map(patientMapper::toPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDto getPatientByEmail(String email) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Patient not found with email: " + email));
        return patientMapper.toPatientDto(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDto getPatientByMobileNumber(String mobileNumber) {
        Patient patient = patientRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new UserNotFoundException("Patient not found with mobile: " + mobileNumber));
        return patientMapper.toPatientDto(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> getPatientsByDoctorId(Long doctorId) {
        // This would need a custom query in the repository
        // For now, return all patients as a fallback
        return getAllPatients();
    }

    private int calculateAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
