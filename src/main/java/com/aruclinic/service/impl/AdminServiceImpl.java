package com.aruclinic.service.impl;

import com.aruclinic.entity.*;
import com.aruclinic.repository.*;
import com.aruclinic.service.AdminService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;
    private final AuditLogRepository auditLogRepository;
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminServiceImpl(UserRepository userRepository,
                            RoleRepository roleRepository,
                            PatientRepository patientRepository,
                            DoctorRepository doctorRepository,
                            AppointmentRepository appointmentRepository,
                            BillRepository billRepository,
                            AuditLogRepository auditLogRepository,
                            JdbcTemplate jdbcTemplate,
                            BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.billRepository = billRepository;
        this.auditLogRepository = auditLogRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;

        // Initialize settings table if not exists
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS clinic_settings (setting_key VARCHAR(100) PRIMARY KEY, setting_value VARCHAR(500))");
        } catch (Exception e) {}

        // Initialize deleted records statistics archive table
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS deleted_records_archive (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "record_type VARCHAR(50) NOT NULL, " +
                "amount DECIMAL(12,2) DEFAULT 0.00, " +
                "count BIGINT DEFAULT 1, " +
                "invoice_date DATE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        } catch (Exception e) {}
    }

    @Override
    public long getTotalPatients() {
        return patientRepository.count();
    }

    @Override
    public long getTotalDoctors() {
        return doctorRepository.count();
    }

    @Override
    public long getTotalReceptionists() {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_roles ur JOIN roles r ON ur.role_id = r.id WHERE r.name = 'RECEPTIONIST'",
                Long.class
            );
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public long getTotalUsers() {
        return userRepository.count();
    }

    @Override
    public long getTodaysAppointments() {
        return appointmentRepository.countByAppointmentDate(LocalDate.now());
    }

    @Override
    public long getWaitingPatients() {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointments WHERE status = 'WAITING' OR status = 'CHECKED_IN'",
                Long.class
            );
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public long getCompletedConsultations() {
        try {
            Long active = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointments WHERE status = 'COMPLETED'",
                Long.class
            );
            Long archived = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(count), 0) FROM deleted_records_archive WHERE record_type = 'CONSULTATION'",
                Long.class
            );
            return (active != null ? active : 0) + (archived != null ? archived : 0);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public double getRevenueToday() {
        try {
            Double active = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total), 0) FROM bills WHERE status = 'PAID' AND invoice_date = ?",
                Double.class,
                java.sql.Date.valueOf(LocalDate.now())
            );
            Double archived = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM deleted_records_archive WHERE record_type = 'REVENUE' AND invoice_date = ?",
                Double.class,
                java.sql.Date.valueOf(LocalDate.now())
            );
            return (active != null ? active : 0.0) + (archived != null ? archived : 0.0);
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public double getRevenueThisMonth() {
        try {
            Double active = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total), 0) FROM bills WHERE status = 'PAID' AND MONTH(invoice_date) = MONTH(?) AND YEAR(invoice_date) = YEAR(?)",
                Double.class,
                java.sql.Date.valueOf(LocalDate.now()),
                java.sql.Date.valueOf(LocalDate.now())
            );
            Double archived = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM deleted_records_archive WHERE record_type = 'REVENUE' AND MONTH(invoice_date) = MONTH(?) AND YEAR(invoice_date) = YEAR(?)",
                Double.class,
                java.sql.Date.valueOf(LocalDate.now()),
                java.sql.Date.valueOf(LocalDate.now())
            );
            return (active != null ? active : 0.0) + (archived != null ? archived : 0.0);
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public long getPendingBillsCount() {
        try {
            Long active = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bills WHERE status = 'UNPAID'",
                Long.class
            );
            Long archived = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(count), 0) FROM deleted_records_archive WHERE record_type = 'PENDING_BILL'",
                Long.class
            );
            return (active != null ? active : 0) + (archived != null ? archived : 0);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public long getNewRegistrationsCount() {
        try {
            Long active = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)",
                Long.class
            );
            Long archived = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(count), 0) FROM deleted_records_archive WHERE record_type = 'REGISTRATION' AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)",
                Long.class
            );
            return (active != null ? active : 0) + (archived != null ? archived : 0);
        } catch (Exception e) {
            return 0;
        }
    }

    // ==========================================
    // USER MANAGEMENT
    // ==========================================
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User createUser(User user, String roleName) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleRepository.findByName(roleName).orElseGet(() -> {
            Role r = new Role(roleName);
            return roleRepository.save(r);
        });
        user.addRole(role);
        User savedUser = userRepository.save(user);

        // Sync with Doctor or Patient
        if ("DOCTOR".equalsIgnoreCase(roleName)) {
            if (!doctorRepository.existsByEmail(user.getEmail())) {
                Doctor doc = new Doctor();
                doc.setEmail(user.getEmail());
                doc.setName(user.getFirstName() + " " + user.getLastName());
                doc.setMobileNumber(user.getMobileNumber());
                doc.setQualification("MBBS");
                doc.setExperience(2);
                doc.setDepartment("General Outpatient");
                doc.setSpecialization("General Medicine");
                doctorRepository.save(doc);
            }
        } else if ("PATIENT".equalsIgnoreCase(roleName)) {
            if (!patientRepository.existsByEmail(user.getEmail())) {
                Patient pat = new Patient();
                pat.setEmail(user.getEmail());
                pat.setFirstName(user.getFirstName());
                pat.setLastName(user.getLastName());
                pat.setMobileNumber(user.getMobileNumber());
                pat.setDateOfBirth(LocalDate.of(1990, 1, 1));
                pat.setAge(36);
                pat.setGender("Other");
                pat.setBloodGroup("O+");
                patientRepository.save(pat);
            }
        }
        return savedUser;
    }

    @Override
    public User updateUser(Long id, User userDetails, String roleName) {
        User user = userRepository.findById(id).orElseThrow();
        String oldEmail = user.getEmail();
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        user.setMobileNumber(userDetails.getMobileNumber());
        if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        if (roleName != null && !roleName.isEmpty()) {
            user.getRoles().clear();
            Role role = roleRepository.findByName(roleName).orElseGet(() -> {
                Role r = new Role(roleName);
                return roleRepository.save(r);
            });
            user.addRole(role);
        }

        User savedUser = userRepository.save(user);

        // Sync with linked Patient/Doctor records
        if (oldEmail != null) {
            patientRepository.findByEmail(oldEmail).ifPresent(patient -> {
                patient.setFirstName(userDetails.getFirstName());
                patient.setLastName(userDetails.getLastName());
                patient.setEmail(userDetails.getEmail());
                patient.setMobileNumber(userDetails.getMobileNumber());
                patientRepository.save(patient);
            });
            doctorRepository.findByEmail(oldEmail).ifPresent(doctor -> {
                doctor.setName(userDetails.getFirstName() + " " + userDetails.getLastName());
                doctor.setEmail(userDetails.getEmail());
                doctor.setMobileNumber(userDetails.getMobileNumber());
                doctorRepository.save(doctor);
            });
        }

        return savedUser;
    }

    @Override
    public void deleteUser(Long id) {
        try {
            // Prevent self-deletion
            try {
                org.springframework.security.core.Authentication authentication = 
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null) {
                    String loggedInEmail = authentication.getName();
                    Optional<User> targetUserOpt = userRepository.findById(id);
                    if (targetUserOpt.isPresent()) {
                        User targetUser = targetUserOpt.get();
                        if (targetUser.getEmail() != null && targetUser.getEmail().equalsIgnoreCase(loggedInEmail)) {
                            throw new RuntimeException("You cannot delete your own account while you are logged in!");
                        }
                    }
                }
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception e) {
                // Ignore ContextHolder issues during tests
            }

            // Prevent deleting the last administrator
            userRepository.findById(id).ifPresent(user -> {
                boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
                if (isAdmin) {
                    Long adminCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM user_roles ur JOIN roles r ON ur.role_id = r.id WHERE r.name = 'ADMIN'",
                        Long.class
                    );
                    if (adminCount != null && adminCount <= 1) {
                        throw new RuntimeException("You cannot delete this administrator account. The system must have at least one active administrator.");
                    }
                }
            });

            // Archive registration first
            jdbcTemplate.update("INSERT INTO deleted_records_archive (record_type, amount, count) VALUES ('REGISTRATION', 0, 1)");

            userRepository.findById(id).ifPresent(user -> {
                String email = user.getEmail();
                if (email != null) {
                    doctorRepository.findByEmail(email).ifPresent(doctor -> {
                        // Archive Doctor statistics before delete
                        long completed = doctor.getAppointments().stream()
                            .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                            .count();
                        if (completed > 0) {
                            jdbcTemplate.update("INSERT INTO deleted_records_archive (record_type, amount, count) VALUES ('CONSULTATION', 0, ?)", completed);
                        }
                        BigDecimal doctorPaidTotal = billRepository.findByDoctorId(doctor.getId()).stream()
                            .filter(b -> "PAID".equalsIgnoreCase(b.getStatus()))
                            .map(Bill::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                        if (doctorPaidTotal.compareTo(BigDecimal.ZERO) > 0) {
                            jdbcTemplate.update("INSERT INTO deleted_records_archive (record_type, amount, count, invoice_date) VALUES ('REVENUE', ?, 1, CURDATE())", doctorPaidTotal);
                        }
                        doctorRepository.delete(doctor);
                    });
                    patientRepository.findByEmail(email).ifPresent(patient -> {
                        // Archive Patient statistics before delete
                        long completed = patient.getAppointments().stream()
                            .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                            .count();
                        if (completed > 0) {
                            jdbcTemplate.update("INSERT INTO deleted_records_archive (record_type, amount, count) VALUES ('CONSULTATION', 0, ?)", completed);
                        }
                        BigDecimal patientPaidTotal = patient.getBills().stream()
                            .filter(b -> "PAID".equalsIgnoreCase(b.getStatus()))
                            .map(Bill::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                        if (patientPaidTotal.compareTo(BigDecimal.ZERO) > 0) {
                            jdbcTemplate.update("INSERT INTO deleted_records_archive (record_type, amount, count, invoice_date) VALUES ('REVENUE', ?, 1, CURDATE())", patientPaidTotal);
                        }
                        long pending = patient.getBills().stream()
                            .filter(b -> "UNPAID".equalsIgnoreCase(b.getStatus()))
                            .count();
                        if (pending > 0) {
                            jdbcTemplate.update("INSERT INTO deleted_records_archive (record_type, amount, count) VALUES ('PENDING_BILL', 0, ?)", pending);
                        }
                        patientRepository.delete(patient);
                    });
                }
            });

            // Delete associated notifications and audit logs first to prevent foreign key issues
            jdbcTemplate.update("DELETE FROM notifications WHERE user_id = ?", id);
            jdbcTemplate.update("DELETE FROM audit_logs WHERE performed_by = ?", id);

            // Clean up receptionists, patients, doctors, and role mappings to resolve database constraints
            jdbcTemplate.update("DELETE FROM receptionists WHERE user_id = ?", id);
            
            try {
                jdbcTemplate.update("DELETE FROM bill_items WHERE bill_id IN (SELECT id FROM bills WHERE patient_id IN (SELECT id FROM patients WHERE user_id = ?))", id);
                jdbcTemplate.update("DELETE FROM bills WHERE patient_id IN (SELECT id FROM patients WHERE user_id = ?)", id);
                jdbcTemplate.update("DELETE FROM prescription_items WHERE prescription_id IN (SELECT id FROM prescriptions WHERE patient_id IN (SELECT id FROM patients WHERE user_id = ?))", id);
                jdbcTemplate.update("DELETE FROM prescriptions WHERE patient_id IN (SELECT id FROM patients WHERE user_id = ?)", id);
                jdbcTemplate.update("DELETE FROM appointments WHERE patient_id IN (SELECT id FROM patients WHERE user_id = ?)", id);
                jdbcTemplate.update("DELETE FROM patients WHERE user_id = ?", id);
            } catch (Exception e) {}

            try {
                jdbcTemplate.update("DELETE FROM prescriptions WHERE doctor_id IN (SELECT id FROM doctors WHERE user_id = ?)", id);
                jdbcTemplate.update("DELETE FROM appointments WHERE doctor_id IN (SELECT id FROM doctors WHERE user_id = ?)", id);
                jdbcTemplate.update("DELETE FROM bills WHERE doctor_id IN (SELECT id FROM doctors WHERE user_id = ?)", id);
                jdbcTemplate.update("DELETE FROM doctors WHERE user_id = ?", id);
            } catch (Exception e) {}

            jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", id);

            userRepository.deleteById(id);
            userRepository.flush(); // Force immediate execution to catch constraint violations
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new RuntimeException("This user cannot be deleted because they are referenced by existing active records (such as bills or prescriptions) in the database. Please resolve those records first or disable the user's login access instead.");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete user: " + ex.getMessage());
        }
    }

    @Override
    public void resetUserPassword(Long id, String newPassword) {
        User user = userRepository.findById(id).orElseThrow();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void toggleUserStatus(Long id, boolean enabled) {
        saveClinicSetting("user_disabled_" + id, enabled ? null : "true");
    }

    @Override
    public boolean isUserEnabled(Long id) {
        return getClinicSetting("user_disabled_" + id, null) == null;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // ==========================================
    // DOCTOR MANAGEMENT
    // ==========================================
    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    public Doctor createDoctor(Doctor doctor) {
        if (!userRepository.existsByEmail(doctor.getEmail())) {
            User u = new User();
            u.setEmail(doctor.getEmail());
            String[] names = doctor.getName().split(" ", 2);
            u.setFirstName(names[0]);
            u.setLastName(names.length > 1 ? names[1] : "");
            u.setMobileNumber(doctor.getMobileNumber());
            u.setPassword(passwordEncoder.encode("doctor123!"));
            Role role = roleRepository.findByName("DOCTOR").orElseGet(() -> {
                Role r = new Role("DOCTOR");
                return roleRepository.save(r);
            });
            u.addRole(role);
            userRepository.save(u);
        }
        return doctorRepository.save(doctor);
    }

    @Override
    public Doctor updateDoctor(Long id, Doctor docDetails) {
        Doctor doc = doctorRepository.findById(id).orElseThrow();
        String oldEmail = doc.getEmail();
        doc.setName(docDetails.getName());
        doc.setSpecialization(docDetails.getSpecialization());
        doc.setDepartment(docDetails.getDepartment());
        doc.setQualification(docDetails.getQualification());
        doc.setExperience(docDetails.getExperience());
        doc.setMobileNumber(docDetails.getMobileNumber());
        doc.setEmail(docDetails.getEmail());

        Doctor savedDoctor = doctorRepository.save(doc);

        // Sync with corresponding User record
        if (oldEmail != null) {
            userRepository.findByEmail(oldEmail).ifPresent(user -> {
                String docName = docDetails.getName();
                String fn = "";
                String ln = "";
                if (docName != null) {
                    String[] parts = docName.trim().split("\\s+", 2);
                    fn = parts[0];
                    if (parts.length > 1) {
                        ln = parts[1];
                    }
                }
                user.setFirstName(fn);
                user.setLastName(ln);
                user.setEmail(docDetails.getEmail());
                user.setMobileNumber(docDetails.getMobileNumber());
                userRepository.save(user);
            });
        }

        return savedDoctor;
    }

    @Override
    public void deleteDoctor(Long id) {
        try {
            doctorRepository.findById(id).ifPresent(doc -> {
                // Archive Doctor statistics before delete
                long completed = doc.getAppointments().stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                    .count();
                if (completed > 0) {
                    jdbcTemplate.update("INSERT INTO deleted_records_archive (record_type, amount, count) VALUES ('CONSULTATION', 0, ?)", completed);
                }
                BigDecimal doctorPaidTotal = billRepository.findByDoctorId(doc.getId()).stream()
                    .filter(b -> "PAID".equalsIgnoreCase(b.getStatus()))
                    .map(Bill::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (doctorPaidTotal.compareTo(BigDecimal.ZERO) > 0) {
                    jdbcTemplate.update("INSERT INTO deleted_records_archive (record_type, amount, count, invoice_date) VALUES ('REVENUE', ?, 1, CURDATE())", doctorPaidTotal);
                }
            });

            doctorRepository.deleteById(id);
            doctorRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new RuntimeException("This doctor cannot be deleted because they are referenced by existing active records (such as bills or appointments) in the database. Please resolve those records first.");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete doctor: " + ex.getMessage());
        }
    }

    // ==========================================
    // RECEPTIONIST MANAGEMENT
    // ==========================================
    @Override
    public List<User> getReceptionists() {
        try {
            List<Long> userIds = jdbcTemplate.queryForList(
                "SELECT user_id FROM user_roles ur JOIN roles r ON ur.role_id = r.id WHERE r.name = 'RECEPTIONIST'",
                Long.class
            );
            return userRepository.findAllById(userIds);
        } catch (Exception e) {
            return List.of();
        }
    }

    // ==========================================
    // PATIENT MANAGEMENT
    // ==========================================
    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @Override
    public Patient updatePatient(Long id, Patient patDetails) {
        Patient pat = patientRepository.findById(id).orElseThrow();
        String oldEmail = pat.getEmail();
        pat.setFirstName(patDetails.getFirstName());
        pat.setLastName(patDetails.getLastName());
        pat.setEmail(patDetails.getEmail());
        pat.setMobileNumber(patDetails.getMobileNumber());
        pat.setDateOfBirth(patDetails.getDateOfBirth());
        pat.setGender(patDetails.getGender());
        pat.setBloodGroup(patDetails.getBloodGroup());
        pat.setAge(patDetails.getAge());
        pat.setAddress(patDetails.getAddress());
        pat.setCity(patDetails.getCity());
        pat.setState(patDetails.getState());
        pat.setZipCode(patDetails.getZipCode());
        pat.setDistrict(patDetails.getDistrict());
        pat.setEmergencyContact(patDetails.getEmergencyContact());
        pat.setEmergencyPhone(patDetails.getEmergencyPhone());

        Patient savedPatient = patientRepository.save(pat);

        // Sync with corresponding User record
        if (oldEmail != null) {
            userRepository.findByEmail(oldEmail).ifPresent(user -> {
                user.setFirstName(patDetails.getFirstName());
                user.setLastName(patDetails.getLastName());
                user.setEmail(patDetails.getEmail());
                user.setMobileNumber(patDetails.getMobileNumber());
                userRepository.save(user);
            });
        }

        return savedPatient;
    }

    @Override
    public void deletePatient(Long id) {
        try {
            patientRepository.findById(id).ifPresent(pat -> {
                // Archive Patient statistics before delete
                long completed = pat.getAppointments().stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                    .count();
                if (completed > 0) {
                    jdbcTemplate.update("INSERT INTO deleted_records_archive (record_type, amount, count) VALUES ('CONSULTATION', 0, ?)", completed);
                }
                BigDecimal patientPaidTotal = pat.getBills().stream()
                    .filter(b -> "PAID".equalsIgnoreCase(b.getStatus()))
                    .map(Bill::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (patientPaidTotal.compareTo(BigDecimal.ZERO) > 0) {
                    jdbcTemplate.update("INSERT INTO deleted_records_archive (record_type, amount, count, invoice_date) VALUES ('REVENUE', ?, 1, CURDATE())", patientPaidTotal);
                }
                long pending = pat.getBills().stream()
                    .filter(b -> "UNPAID".equalsIgnoreCase(b.getStatus()))
                    .count();
                if (pending > 0) {
                    jdbcTemplate.update("INSERT INTO deleted_records_archive (record_type, amount, count) VALUES ('PENDING_BILL', 0, ?)", pending);
                }
            });

            patientRepository.deleteById(id);
            patientRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new RuntimeException("This patient cannot be deleted because they are referenced by existing active records (such as bills or appointments) in the database. Please resolve those records first.");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete patient: " + ex.getMessage());
        }
    }

    // ==========================================
    // APPOINTMENT MANAGEMENT
    // ==========================================
    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment createAppointment(Appointment appointment) {
        Appointment saved = appointmentRepository.save(appointment);
        try {
            sendAppointmentNotification(saved, "New Appointment Booked", 
                "A new appointment #" + saved.getId() + " has been booked for you on " + saved.getAppointmentDate() + " at " + saved.getAppointmentTime() + ".");
        } catch (Exception e) {}
        return saved;
    }

    @Override
    public Appointment updateAppointment(Long id, Appointment apptDetails) {
        Appointment appt = appointmentRepository.findById(id).orElseThrow();
        appt.setAppointmentDate(apptDetails.getAppointmentDate());
        appt.setAppointmentTime(apptDetails.getAppointmentTime());
        appt.setStatus(apptDetails.getStatus());
        if (apptDetails.getDoctor() != null) {
            appt.setDoctor(apptDetails.getDoctor());
        }
        return appointmentRepository.save(appt);
    }

    @Override
    public void cancelAppointment(Long id) {
        cancelAppointment(id, "Cancelled by Admin");
    }

    @Override
    public void cancelAppointment(Long id, String reason) {
        Appointment appt = appointmentRepository.findById(id).orElseThrow();
        appointmentRepository.cancelAppointmentById(id, com.aruclinic.entity.AppointmentStatus.CANCELLED, reason);
        appt.setStatus(com.aruclinic.entity.AppointmentStatus.CANCELLED);
        appt.setReason(reason);

        try {
            sendAppointmentNotification(appt, "Appointment Cancelled", 
                "Your appointment #" + appt.getId() + " on " + appt.getAppointmentDate() + " has been cancelled. Reason: " + reason);
        } catch (Exception e) {}
    }

    @Override
    public void rescheduleAppointment(Long id, java.time.LocalDate date, java.time.LocalTime time, String reason) {
        rescheduleAppointment(id, date, time, reason, null);
    }

    @Override
    public void rescheduleAppointment(Long id, java.time.LocalDate date, java.time.LocalTime time, String reason, Long newDoctorId) {
        Appointment appt = appointmentRepository.findById(id).orElseThrow();
        com.aruclinic.entity.Doctor oldDoc = appt.getDoctor();
        boolean doctorChanged = false;
        if (newDoctorId != null && (oldDoc == null || !oldDoc.getId().equals(newDoctorId))) {
            com.aruclinic.entity.Doctor newDoc = doctorRepository.findById(newDoctorId)
                    .orElseThrow(() -> new com.aruclinic.exception.UserNotFoundException("Doctor not found with ID: " + newDoctorId));
            appt.setDoctor(newDoc);
            doctorChanged = true;
        }

        appointmentRepository.rescheduleAppointmentById(id, date, time, reason);
        appt.setAppointmentDate(date);
        appt.setAppointmentTime(time);
        appt.setAppointmentDateTime(java.time.LocalDateTime.of(date, time));
        appt.setReason(reason);
        appointmentRepository.save(appt);

        try {
            String msg = "Your appointment #" + appt.getId() + " has been rescheduled to " + date + " at " + time;
            if (doctorChanged && oldDoc != null && appt.getDoctor() != null) {
                msg += " and reassigned from Dr. " + oldDoc.getName() + " to Dr. " + appt.getDoctor().getName();
            }
            msg += ". Reason: " + reason;

            sendAppointmentNotification(appt, "Appointment Rescheduled", msg);
            
            if (doctorChanged && oldDoc != null) {
                userRepository.findByEmail(oldDoc.getEmail()).ifPresent(u -> {
                    jdbcTemplate.update(
                        "INSERT INTO notifications (user_id, title, message, is_read, created_at) VALUES (?, ?, ?, ?, NOW())",
                        u.getId(), "Appointment Reassigned", "Your appointment #" + appt.getId() + " has been rescheduled and reassigned to another doctor.", false
                    );
                });
            }
        } catch (Exception e) {}
    }

    private void sendAppointmentNotification(Appointment appt, String title, String msg) {
        if (appt.getPatient() != null && appt.getPatient().getEmail() != null) {
            userRepository.findByEmail(appt.getPatient().getEmail()).ifPresent(u -> {
                jdbcTemplate.update(
                    "INSERT INTO notifications (user_id, title, message, is_read, created_at) VALUES (?, ?, ?, ?, NOW())",
                    u.getId(), title, msg, false
                );
            });
        }
        if (appt.getDoctor() != null && appt.getDoctor().getEmail() != null) {
            userRepository.findByEmail(appt.getDoctor().getEmail()).ifPresent(u -> {
                jdbcTemplate.update(
                    "INSERT INTO notifications (user_id, title, message, is_read, created_at) VALUES (?, ?, ?, ?, NOW())",
                    u.getId(), title, msg, false
                );
            });
        }
    }

    // ==========================================
    // BILLING
    // ==========================================
    @Override
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    @Override
    public Bill createBill(Bill bill) {
        if (bill.getInvoiceNumber() == null || bill.getInvoiceNumber().trim().isEmpty()) {
            bill.setInvoiceNumber("INV-" + System.currentTimeMillis());
        }
        Bill saved = billRepository.save(bill);
        try {
            if (saved.getPatient() != null && saved.getPatient().getEmail() != null) {
                Optional<User> patientUserOpt = userRepository.findByEmail(saved.getPatient().getEmail());
                patientUserOpt.ifPresent(u -> {
                    String title = "New Invoice Generated: INV-" + saved.getId();
                    String msg = "A new invoice of ₹" + saved.getTotal() + " from Dr. " + (saved.getDoctor() != null ? saved.getDoctor().getName() : "AruClinic") + " has been generated. Date: " + saved.getInvoiceDate();
                    
                    jdbcTemplate.update(
                        "INSERT INTO notifications (user_id, title, message, is_read, created_at) VALUES (?, ?, ?, ?, NOW())",
                        u.getId(), title, msg, false
                    );
                });
            }
            if ("PAID".equalsIgnoreCase(saved.getStatus())) {
                sendPaidNotificationToAdmins(saved);
            }
        } catch (Exception e) {
            // Ignore notification error so billing doesn't fail
        }
        return saved;
    }

    @Override
    public Bill updateBill(Long id, Bill billDetails) {
        Bill bill = billRepository.findById(id).orElseThrow();
        bill.setAmount(billDetails.getAmount());
        bill.setTax(billDetails.getTax());
        bill.setTotal(billDetails.getTotal());
        bill.setStatus(billDetails.getStatus());
        return billRepository.save(bill);
    }

    @Override
    public void payBill(Long id) {
        Bill bill = billRepository.findById(id).orElseThrow();
        bill.setStatus("PAID");
        bill.setPaidDate(LocalDate.now());
        Bill saved = billRepository.save(bill);
        sendPaidNotificationToAdmins(saved);
    }

    private void sendPaidNotificationToAdmins(Bill bill) {
        try {
            String invoiceNum = bill.getInvoiceNumber() != null ? bill.getInvoiceNumber() : ("INV-" + bill.getId());
            String patientName = bill.getPatient() != null ? (bill.getPatient().getFirstName() + " " + bill.getPatient().getLastName()) : "Patient";
            String title = "Invoice Paid: " + invoiceNum;
            String msg = "Invoice " + invoiceNum + " for patient " + patientName + " of amount ₹" + bill.getTotal() + " has been marked as PAID via " + (bill.getPaymentMethod() != null ? bill.getPaymentMethod() : "Cash") + ".";

            List<User> admins = userRepository.findByRoleName("ADMIN");
            List<User> superAdmins = userRepository.findByRoleName("SUPER_ADMIN");
            
            java.util.Set<User> allAdmins = new java.util.HashSet<>();
            allAdmins.addAll(admins);
            allAdmins.addAll(superAdmins);

            for (User admin : allAdmins) {
                jdbcTemplate.update(
                    "INSERT INTO notifications (user_id, title, message, is_read, created_at) VALUES (?, ?, ?, ?, NOW())",
                    admin.getId(), title, msg, false
                );
            }
        } catch (Exception e) {
            // Ignore notification error
        }
    }

    // ==========================================
    // AUDIT LOGS
    // ==========================================
    @Override
    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAll();
    }

    // ==========================================
    // CLINIC SETTINGS
    // ==========================================
    @Override
    public String getClinicSetting(String key, String defaultValue) {
        try {
            List<String> results = jdbcTemplate.query(
                "SELECT setting_value FROM clinic_settings WHERE setting_key = ?",
                (rs, rowNum) -> rs.getString("setting_value"),
                key
            );
            if (results.isEmpty() || results.get(0) == null) {
                return defaultValue;
            }
            return results.get(0);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public void saveClinicSetting(String key, String value) {
        try {
            if (value == null) {
                jdbcTemplate.update("DELETE FROM clinic_settings WHERE setting_key = ?", key);
            } else {
                jdbcTemplate.update(
                    "INSERT INTO clinic_settings (setting_key, setting_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE setting_value = ?",
                    key, value, value
                );
            }
        } catch (Exception e) {}
    }

    @Override
    public java.util.Optional<Patient> findPatientByEmail(String email) {
        return patientRepository.findByEmail(email);
    }

    @Override
    public Patient savePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @Override
    public java.util.Optional<Doctor> findDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email);
    }

    @Override
    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
