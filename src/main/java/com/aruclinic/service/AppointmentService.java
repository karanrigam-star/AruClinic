package com.aruclinic.service;

import com.aruclinic.dto.AppointmentDto;
import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.AppointmentStatus;
import com.aruclinic.exception.UserNotFoundException;
import com.aruclinic.repository.AppointmentRepository;
import com.aruclinic.repository.PatientRepository;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.mapper.AppointmentMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for appointment operations.
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentMapper = appointmentMapper;
    }

    public Appointment createAppointment(Appointment appointment) {
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setCreatedAt(appointment.getCreatedAt());
        return appointmentRepository.save(appointment);
    }

    public AppointmentDto createAppointment(AppointmentDto dto) {
        Appointment appointment = appointmentMapper.toAppointment(dto);
        
        if (dto.getPatientId() != null) {
            appointment.setPatient(patientRepository.findById(dto.getPatientId()).orElse(null));
        }
        if (dto.getDoctorId() != null) {
            appointment.setDoctor(doctorRepository.findById(dto.getDoctorId()).orElse(null));
        }
        
        if (dto.getAppointmentDate() != null && dto.getAppointmentTime() != null) {
            appointment.setAppointmentDateTime(LocalDateTime.of(dto.getAppointmentDate(), dto.getAppointmentTime()));
        } else if (dto.getAppointmentDateTime() != null) {
            appointment.setAppointmentDateTime(dto.getAppointmentDateTime());
        }
        
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(LocalDateTime.now());
        
        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toAppointmentDto(saved);
    }

    public Appointment getById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Appointment not found with ID: " + id));
    }

    public Appointment updateAppointment(Long appointmentId, Appointment appointmentDetails) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new UserNotFoundException("Appointment not found with ID: " + appointmentId));

        if (appointmentDetails.getPatient() != null) {
            appointment.setPatient(appointmentDetails.getPatient());
        }
        if (appointmentDetails.getDoctor() != null) {
            appointment.setDoctor(appointmentDetails.getDoctor());
        }
        if (appointmentDetails.getAppointmentDateTime() != null) {
            appointment.setAppointmentDateTime(appointmentDetails.getAppointmentDateTime());
        }
        if (appointmentDetails.getStatus() != null) {
            appointment.setStatus(appointmentDetails.getStatus());
        }

        appointment.setCreatedAt(LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long appointmentId) {
        appointmentRepository.deleteById(appointmentId);
    }

    public List<Appointment> findByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> findByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }
}