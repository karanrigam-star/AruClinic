package com.aruclinic;

import com.aruclinic.entity.Role;
import com.aruclinic.entity.RoleName;
import com.aruclinic.entity.User;
import com.aruclinic.repository.RoleRepository;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.repository.DoctorRepository;
import com.aruclinic.repository.PatientRepository;
import com.aruclinic.entity.Doctor;
import com.aruclinic.entity.Patient;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class AruClinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(AruClinicApplication.class, args);
    }

    @Bean
    public CommandLineRunner init(RoleRepository roleRepository,
                                  UserRepository userRepository,
                                  DoctorRepository doctorRepository,
                                  PatientRepository patientRepository,
                                  JdbcTemplate jdbcTemplate,
                                  BCryptPasswordEncoder passwordEncoder) {

        return args -> {
            // Alter user_id columns to be nullable so JPA inserts do not fail on missing back-references
            try {
                jdbcTemplate.execute("ALTER TABLE doctors MODIFY user_id BIGINT NULL");
                jdbcTemplate.execute("ALTER TABLE patients MODIFY user_id BIGINT NULL");
                jdbcTemplate.execute("ALTER TABLE receptionists MODIFY user_id BIGINT NULL");
            } catch (Exception e) {
                // Ignore if migration has not run or database is in memory testing state
            }

            try {
                jdbcTemplate.execute("ALTER TABLE appointments MODIFY appointment_date_time DATETIME NULL");
            } catch (Exception e) {
                // Ignore if column does not exist
            }

            // Sync prescriptions table schema columns with JPA Prescription entity model
            try {
                jdbcTemplate.execute("ALTER TABLE prescriptions ADD COLUMN symptoms TEXT NULL");
            } catch (Exception e) {}
            try {
                jdbcTemplate.execute("ALTER TABLE prescriptions ADD COLUMN diagnosis TEXT NULL");
            } catch (Exception e) {}
            try {
                jdbcTemplate.execute("ALTER TABLE prescriptions ADD COLUMN advice TEXT NULL");
            } catch (Exception e) {}
            try {
                jdbcTemplate.execute("ALTER TABLE prescriptions ADD COLUMN follow_up_date DATE NULL");
            } catch (Exception e) {}

            // Sync prescription_items table schema columns with JPA PrescriptionItem entity model
            try {
                jdbcTemplate.execute("ALTER TABLE prescription_items ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            } catch (Exception e) {}
            try {
                jdbcTemplate.execute("ALTER TABLE prescription_items ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
            } catch (Exception e) {}

            // Create roles
            for (RoleName roleName : RoleName.values()) {
                if (!roleRepository.existsByName(roleName.name())) {
                    Role role = new Role();
                    role.setName(roleName.name());
                    roleRepository.save(role);
                }
            }

            // =========================
            // ADMIN
            // =========================
            if (!userRepository.existsByEmail("admin@example.com")) {

                User admin = new User();
                admin.setEmail("admin@example.com");
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setMobileNumber("1234567890");
                admin.setPassword(passwordEncoder.encode("admin123!"));

                Role role = roleRepository.findByName(RoleName.SUPER_ADMIN.name()).orElseThrow();
                admin.addRole(role);

                userRepository.save(admin);
            }

            // =========================
            // DOCTOR
            // =========================
            if (!userRepository.existsByEmail("doctor@example.com")) {

                User doctor = new User();
                doctor.setEmail("doctor@example.com");
                doctor.setFirstName("John");
                doctor.setLastName("Doe");
                doctor.setMobileNumber("0987654321");
                doctor.setPassword(passwordEncoder.encode("doctor123!"));

                Role role = roleRepository.findByName(RoleName.DOCTOR.name()).orElseThrow();
                doctor.addRole(role);

                userRepository.save(doctor);
            }

            // Seed/sync corresponding Doctor entity
            if (doctorRepository.existsByEmail("doctor@example.com")) {
                Doctor doc = doctorRepository.findByEmail("doctor@example.com").orElseThrow();
                if ("Doctor".equalsIgnoreCase(doc.getName())) {
                    doc.setName("John Doe");
                    doctorRepository.save(doc);
                }
            } else {
                Doctor doc = new Doctor();
                doc.setEmail("doctor@example.com");
                doc.setName("John Doe");
                doc.setSpecialization("General Medicine");
                doc.setDepartment("General Outpatient");
                doc.setQualification("MBBS, MD");
                doc.setExperience(8);
                doc.setMobileNumber("0987654321");
                doctorRepository.save(doc);
            }

            // =========================
            // RECEPTIONIST
            // =========================
            if (!userRepository.existsByEmail("receptionist@example.com")) {

                User receptionist = new User();
                receptionist.setEmail("receptionist@example.com");
                receptionist.setFirstName("Jane");
                receptionist.setLastName("Smith");
                receptionist.setMobileNumber("1122334455");
                receptionist.setPassword(passwordEncoder.encode("receptionist123!"));

                Role role = roleRepository.findByName(RoleName.RECEPTIONIST.name()).orElseThrow();
                receptionist.addRole(role);

                userRepository.save(receptionist);
            }

            // =========================
            // PATIENT
            // =========================
            if (!userRepository.existsByEmail("patient@example.com")) {

                User patient = new User();
                patient.setEmail("patient@example.com");
                patient.setFirstName("Alice");
                patient.setLastName("Johnson");
                patient.setMobileNumber("5566778899");
                patient.setPassword(passwordEncoder.encode("patient123!"));

                Role role = roleRepository.findByName(RoleName.PATIENT.name()).orElseThrow();
                patient.addRole(role);

                userRepository.save(patient);
            }

            // Seed/sync corresponding Patient entity
            if (patientRepository.existsByEmail("patient@example.com")) {
                Patient pat = patientRepository.findByEmail("patient@example.com").orElseThrow();
                if ("Patient".equalsIgnoreCase(pat.getFirstName())) {
                    pat.setFirstName("Alice");
                    pat.setLastName("Johnson");
                    patientRepository.save(pat);
                }
            } else {
                Patient pat = new Patient();
                pat.setEmail("patient@example.com");
                pat.setFirstName("Alice");
                pat.setLastName("Johnson");
                pat.setDateOfBirth(LocalDate.of(1995, 5, 15));
                pat.setAge(31);
                pat.setGender("Female");
                pat.setBloodGroup("O+");
                pat.setMobileNumber("5566778899");
                pat.setAddress("456 Oak Avenue, Suite 10");
                patientRepository.save(pat);
            }
        };
    }
}