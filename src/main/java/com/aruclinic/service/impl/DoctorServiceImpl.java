package com.aruclinic.service.impl;

import com.aruclinic.dto.DoctorDto;
import com.aruclinic.entity.Doctor;
import com.aruclinic.entity.User;
import com.aruclinic.exception.UserNotFoundException;
import com.aruclinic.mapper.DoctorMapper;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.service.DoctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of DoctorService.
 */
@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorMapper doctorMapper;

    public DoctorServiceImpl(DoctorRepository doctorRepository, UserRepository userRepository, DoctorMapper doctorMapper) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.doctorMapper = doctorMapper;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public DoctorDto createDoctor(DoctorDto doctorDto) {
        Doctor doctor = doctorMapper.toDoctor(doctorDto);
        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDoctorDto(savedDoctor);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public DoctorDto getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with id: " + id));
        return doctorMapper.toDoctorDto(doctor);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DOCTOR')")
    public DoctorDto updateDoctor(Long id, DoctorDto doctorDto) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with id: " + id));

        String oldEmail = existingDoctor.getEmail();

        // Update fields
        if (doctorDto.getFirstName() != null) {
            existingDoctor.setName(doctorDto.getFirstName() + " " + (doctorDto.getLastName() != null ? doctorDto.getLastName() : ""));
        }
        if (doctorDto.getQualification() != null) {
            existingDoctor.setQualification(doctorDto.getQualification());
        }
        if (doctorDto.getExperience() != null) {
            existingDoctor.setExperience(doctorDto.getExperience());
        }
        if (doctorDto.getDepartment() != null) {
            existingDoctor.setDepartment(doctorDto.getDepartment());
        }
        if (doctorDto.getSpecialization() != null) {
            existingDoctor.setSpecialization(doctorDto.getSpecialization());
        }
        if (doctorDto.getMobileNumber() != null) {
            existingDoctor.setMobileNumber(doctorDto.getMobileNumber());
        }
        if (doctorDto.getEmail() != null) {
            existingDoctor.setEmail(doctorDto.getEmail());
        }

        Doctor updatedDoctor = doctorRepository.save(existingDoctor);

        // Sync with corresponding User entity
        if (oldEmail != null) {
            userRepository.findByEmail(oldEmail).ifPresent(user -> {
                if (doctorDto.getFirstName() != null) {
                    user.setFirstName(doctorDto.getFirstName());
                }
                if (doctorDto.getLastName() != null) {
                    user.setLastName(doctorDto.getLastName());
                }
                if (doctorDto.getEmail() != null) {
                    user.setEmail(doctorDto.getEmail());
                }
                if (doctorDto.getMobileNumber() != null) {
                    user.setMobileNumber(doctorDto.getMobileNumber());
                }
                userRepository.save(user);
            });
        }

        return doctorMapper.toDoctorDto(updatedDoctor);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new UserNotFoundException("Doctor not found with id: " + id);
        }
        doctorRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public List<DoctorDto> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(doctorMapper::toDoctorDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public List<DoctorDto> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecializationContainingIgnoreCase(specialization).stream()
                .map(doctorMapper::toDoctorDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public List<DoctorDto> getDoctorsByDepartment(String department) {
        return doctorRepository.findByDepartmentContainingIgnoreCase(department).stream()
                .map(doctorMapper::toDoctorDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public List<DoctorDto> searchDoctors(String query) {
        return doctorRepository.findByNameContainingIgnoreCaseOrSpecializationContainingIgnoreCaseOrDepartmentContainingIgnoreCase(
                        query, query, query)
                .stream()
                .map(doctorMapper::toDoctorDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public DoctorDto getDoctorByEmail(String email) {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with email: " + email));
        return doctorMapper.toDoctorDto(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public List<DoctorDto> getAvailableDoctors() {
        // For now, return all doctors
        // In a real application, this would filter by availability
        return getAllDoctors();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Doctor getDoctorEntityById(Long id) {
        return doctorRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<Doctor> getDoctorsBySpecializationEntity(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DOCTOR')")
    public Doctor getDoctorEntityByEmail(String email) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
            String loggedInEmail = getLoggedInEmail(auth);
            if (loggedInEmail != null && !loggedInEmail.equalsIgnoreCase(email)) {
                throw new org.springframework.security.access.AccessDeniedException("Doctors are only allowed to view their own profile.");
            }
        }
        return doctorRepository.findByEmail(email).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'DOCTOR')")
    public List<Doctor> getAllDoctorEntities() {
        return doctorRepository.findAll();
    }

    private String getLoggedInEmail(org.springframework.security.core.Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
            return springUser.getUsername();
        } else if (principal instanceof com.aruclinic.entity.User userEntity) {
            return userEntity.getEmail();
        } else if (principal instanceof String principalStr) {
            return principalStr;
        }
        return null;
    }
}
