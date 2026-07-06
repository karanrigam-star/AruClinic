package com.aruclinic.service.impl;

import com.aruclinic.entity.*;
import com.aruclinic.repository.*;
import com.aruclinic.service.AdminService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointments WHERE status = 'COMPLETED'",
                Long.class
            );
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public double getRevenueToday() {
        try {
            Double val = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total), 0) FROM bills WHERE status = 'PAID' AND invoice_date = ?",
                Double.class,
                java.sql.Date.valueOf(LocalDate.now())
            );
            return val != null ? val : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public double getRevenueThisMonth() {
        try {
            Double val = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total), 0) FROM bills WHERE status = 'PAID' AND MONTH(invoice_date) = MONTH(?) AND YEAR(invoice_date) = YEAR(?)",
                Double.class,
                java.sql.Date.valueOf(LocalDate.now()),
                java.sql.Date.valueOf(LocalDate.now())
            );
            return val != null ? val : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public long getPendingBillsCount() {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bills WHERE status = 'UNPAID'",
                Long.class
            );
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public long getNewRegistrationsCount() {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)",
                Long.class
            );
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
            userRepository.findById(id).ifPresent(user -> {
                String email = user.getEmail();
                if (email != null) {
                    doctorRepository.findByEmail(email).ifPresent(doctorRepository::delete);
                    patientRepository.findByEmail(email).ifPresent(patientRepository::delete);
                }
            });
        } catch (Exception ex) {
            // ignore
        }
        userRepository.deleteById(id);
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
        doctorRepository.deleteById(id);
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
        patientRepository.deleteById(id);
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
        billRepository.save(bill);
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
}
