package com.aruclinic.service;

import com.aruclinic.dto.AppointmentDto;
import com.aruclinic.entity.Appointment;
import com.aruclinic.entity.AppointmentStatus;
import com.aruclinic.exception.UserNotFoundException;
import com.aruclinic.repository.AppointmentRepository;
import com.aruclinic.repository.PatientRepository;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.repository.NotificationRepository;
import com.aruclinic.entity.Notification;
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
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              AppointmentMapper appointmentMapper,
                              UserRepository userRepository,
                              NotificationRepository notificationRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentMapper = appointmentMapper;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    public Appointment createAppointment(Appointment appointment) {
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(LocalDateTime.now());
        Appointment saved = appointmentRepository.save(appointment);

        // Send notification to the Doctor and Patient's corresponding User record
        try {
            if (saved.getDoctor() != null && saved.getDoctor().getEmail() != null) {
                String doctorEmail = saved.getDoctor().getEmail();
                com.aruclinic.entity.User doctorUser = userRepository.findByEmail(doctorEmail).orElse(null);
                if (doctorUser != null) {
                    Notification notification = new Notification();
                    notification.setUser(doctorUser);
                    notification.setTitle("New Appointment Booked");
                    String patientName = saved.getPatient() != null ? (saved.getPatient().getFirstName() + " " + saved.getPatient().getLastName()) : "Patient";
                    String dateStr = saved.getAppointmentDateTime() != null ? saved.getAppointmentDateTime().toString().replace("T", " ") : "";
                    notification.setMessage("You have a new appointment booked with patient " + patientName + " on " + dateStr + ".");
                    notification.setRead(false);
                    notification.setCreatedAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                }
            }
            if (saved.getPatient() != null && saved.getPatient().getEmail() != null) {
                String patientEmail = saved.getPatient().getEmail();
                com.aruclinic.entity.User patientUser = userRepository.findByEmail(patientEmail).orElse(null);
                if (patientUser != null) {
                    Notification notification = new Notification();
                    notification.setUser(patientUser);
                    notification.setTitle("Appointment Scheduled");
                    String docName = saved.getDoctor() != null ? saved.getDoctor().getName() : "Doctor";
                    String dateStr = saved.getAppointmentDateTime() != null ? saved.getAppointmentDateTime().toString().replace("T", " ") : "";
                    notification.setMessage("Your appointment with " + docName + " has been scheduled on " + dateStr + ".");
                    notification.setRead(false);
                    notification.setCreatedAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                }
            }
        } catch (Exception e) {
            // Ignore notification failure to ensure booking completes
        }

        return saved;
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

        // Send notification to the Doctor and Patient's corresponding User record
        try {
            if (saved.getDoctor() != null && saved.getDoctor().getEmail() != null) {
                String doctorEmail = saved.getDoctor().getEmail();
                com.aruclinic.entity.User doctorUser = userRepository.findByEmail(doctorEmail).orElse(null);
                if (doctorUser != null) {
                    Notification notification = new Notification();
                    notification.setUser(doctorUser);
                    notification.setTitle("New Appointment Booked");
                    String patientName = saved.getPatient() != null ? (saved.getPatient().getFirstName() + " " + saved.getPatient().getLastName()) : "Patient";
                    String dateStr = saved.getAppointmentDateTime() != null ? saved.getAppointmentDateTime().toString().replace("T", " ") : "";
                    notification.setMessage("You have a new appointment booked with patient " + patientName + " on " + dateStr + ".");
                    notification.setRead(false);
                    notification.setCreatedAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                }
            }
            if (saved.getPatient() != null && saved.getPatient().getEmail() != null) {
                String patientEmail = saved.getPatient().getEmail();
                com.aruclinic.entity.User patientUser = userRepository.findByEmail(patientEmail).orElse(null);
                if (patientUser != null) {
                    Notification notification = new Notification();
                    notification.setUser(patientUser);
                    notification.setTitle("Appointment Scheduled");
                    String docName = saved.getDoctor() != null ? saved.getDoctor().getName() : "Doctor";
                    String dateStr = saved.getAppointmentDateTime() != null ? saved.getAppointmentDateTime().toString().replace("T", " ") : "";
                    notification.setMessage("Your appointment with " + docName + " has been scheduled on " + dateStr + ".");
                    notification.setRead(false);
                    notification.setCreatedAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                }
            }
        } catch (Exception e) {
            // Ignore notification failure to ensure booking completes
        }

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

    @org.springframework.transaction.annotation.Transactional
    public void patientCancelAppointment(Long id, String reason) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Appointment not found with ID: " + id));
        appointmentRepository.cancelAppointmentById(id, AppointmentStatus.CANCELLED, reason);
        appt.setStatus(AppointmentStatus.CANCELLED);
        appt.setReason(reason);

        String patientName = appt.getPatient() != null ? appt.getPatient().getFirstName() + " " + appt.getPatient().getLastName() : "Patient";
        String title = "Appointment Cancelled by Patient";
        String msg = "Appointment #" + appt.getId() + " for patient " + patientName + " on " + appt.getAppointmentDate() + " has been cancelled. Reason: " + reason;

        sendNotificationsToStaff(appt, title, msg);
    }

    @org.springframework.transaction.annotation.Transactional
    public void patientRescheduleAppointment(Long id, java.time.LocalDate date, java.time.LocalTime time, String reason) {
        patientRescheduleAppointment(id, date, time, reason, null);
    }

    @org.springframework.transaction.annotation.Transactional
    public void patientRescheduleAppointment(Long id, java.time.LocalDate date, java.time.LocalTime time, String reason, Long newDoctorId) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Appointment not found with ID: " + id));

        com.aruclinic.entity.Doctor oldDoc = appt.getDoctor();
        boolean doctorChanged = false;
        if (newDoctorId != null && (oldDoc == null || !oldDoc.getId().equals(newDoctorId))) {
            com.aruclinic.entity.Doctor newDoc = doctorRepository.findById(newDoctorId)
                    .orElseThrow(() -> new UserNotFoundException("Doctor not found with ID: " + newDoctorId));
            appt.setDoctor(newDoc);
            doctorChanged = true;
        }

        appointmentRepository.rescheduleAppointmentById(id, date, time, reason);
        appt.setAppointmentDate(date);
        appt.setAppointmentTime(time);
        appt.setAppointmentDateTime(LocalDateTime.of(date, time));
        appt.setReason(reason);
        appointmentRepository.save(appt);

        String patientName = appt.getPatient() != null ? appt.getPatient().getFirstName() + " " + appt.getPatient().getLastName() : "Patient";
        String title = "Appointment Rescheduled by Patient";
        String msg = "Appointment #" + appt.getId() + " for patient " + patientName + " has been rescheduled to " + date + " at " + time;
        if (doctorChanged && oldDoc != null && appt.getDoctor() != null) {
            msg += " and reassigned from Dr. " + oldDoc.getName() + " to Dr. " + appt.getDoctor().getName();
        }
        msg += ". Reason: " + reason;

        final String finalMsg = msg;
        final String finalTitle = title;

        if (doctorChanged) {
            // Notify new doctor
            try {
                if (appt.getDoctor() != null && appt.getDoctor().getEmail() != null) {
                    userRepository.findByEmail(appt.getDoctor().getEmail()).ifPresent(u -> {
                        Notification notification = new Notification();
                        notification.setUser(u);
                        notification.setTitle("New Appointment Assigned");
                        notification.setMessage(finalMsg);
                        notification.setRead(false);
                        notification.setCreatedAt(LocalDateTime.now());
                        notificationRepository.save(notification);
                    });
                }
            } catch (Exception e) {}

            // Notify old doctor
            try {
                if (oldDoc != null && oldDoc.getEmail() != null) {
                    userRepository.findByEmail(oldDoc.getEmail()).ifPresent(u -> {
                        Notification notification = new Notification();
                        notification.setUser(u);
                        notification.setTitle("Appointment Reassigned");
                        notification.setMessage("Your appointment #" + appt.getId() + " with patient " + patientName + " has been rescheduled and reassigned to another doctor.");
                        notification.setRead(false);
                        notification.setCreatedAt(LocalDateTime.now());
                        notificationRepository.save(notification);
                    });
                }
            } catch (Exception e) {}
        } else {
            // Notify original doctor
            try {
                if (appt.getDoctor() != null && appt.getDoctor().getEmail() != null) {
                    userRepository.findByEmail(appt.getDoctor().getEmail()).ifPresent(u -> {
                        Notification notification = new Notification();
                        notification.setUser(u);
                        notification.setTitle(finalTitle);
                        notification.setMessage(finalMsg);
                        notification.setRead(false);
                        notification.setCreatedAt(LocalDateTime.now());
                        notificationRepository.save(notification);
                    });
                }
            } catch (Exception e) {}
        }

        // Notify other staff
        try {
            java.util.Set<com.aruclinic.entity.User> staffUsers = new java.util.HashSet<>();
            staffUsers.addAll(userRepository.findByRoleName("SUPER_ADMIN"));
            staffUsers.addAll(userRepository.findByRoleName("CLINIC_ADMIN"));
            staffUsers.addAll(userRepository.findByRoleName("RECEPTIONIST"));

            for (com.aruclinic.entity.User u : staffUsers) {
                Notification notification = new Notification();
                notification.setUser(u);
                notification.setTitle(title);
                notification.setMessage(msg);
                notification.setRead(false);
                notification.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(notification);
            }
        } catch (Exception e) {}
    }

    private void sendNotificationsToStaff(Appointment appt, String title, String msg) {
        try {
            if (appt.getDoctor() != null && appt.getDoctor().getEmail() != null) {
                userRepository.findByEmail(appt.getDoctor().getEmail()).ifPresent(u -> {
                    Notification notification = new Notification();
                    notification.setUser(u);
                    notification.setTitle(title);
                    notification.setMessage(msg);
                    notification.setRead(false);
                    notification.setCreatedAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                });
            }
        } catch (Exception e) {}

        try {
            java.util.Set<com.aruclinic.entity.User> staffUsers = new java.util.HashSet<>();
            staffUsers.addAll(userRepository.findByRoleName("SUPER_ADMIN"));
            staffUsers.addAll(userRepository.findByRoleName("CLINIC_ADMIN"));
            staffUsers.addAll(userRepository.findByRoleName("RECEPTIONIST"));

            for (com.aruclinic.entity.User u : staffUsers) {
                Notification notification = new Notification();
                notification.setUser(u);
                notification.setTitle(title);
                notification.setMessage(msg);
                notification.setRead(false);
                notification.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(notification);
            }
        } catch (Exception e) {}
    }
}