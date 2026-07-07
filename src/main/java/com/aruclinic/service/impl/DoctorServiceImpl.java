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
    public DoctorDto createDoctor(DoctorDto doctorDto) {
        Doctor doctor = doctorMapper.toDoctor(doctorDto);
        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDoctorDto(savedDoctor);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorDto getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with id: " + id));
        return doctorMapper.toDoctorDto(doctor);
    }

    @Override
    @Transactional
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
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new UserNotFoundException("Doctor not found with id: " + id);
        }
        doctorRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDto> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(doctorMapper::toDoctorDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDto> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecializationContainingIgnoreCase(specialization).stream()
                .map(doctorMapper::toDoctorDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDto> getDoctorsByDepartment(String department) {
        return doctorRepository.findByDepartmentContainingIgnoreCase(department).stream()
                .map(doctorMapper::toDoctorDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDto> searchDoctors(String query) {
        return doctorRepository.findByNameContainingIgnoreCaseOrSpecializationContainingIgnoreCaseOrDepartmentContainingIgnoreCase(
                        query, query, query)
                .stream()
                .map(doctorMapper::toDoctorDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorDto getDoctorByEmail(String email) {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with email: " + email));
        return doctorMapper.toDoctorDto(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDto> getAvailableDoctors() {
        // For now, return all doctors
        // In a real application, this would filter by availability
        return getAllDoctors();
    }

    @Override
    @Transactional(readOnly = true)
    public Doctor getDoctorEntityById(Long id) {
        return doctorRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsBySpecializationEntity(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }
}
