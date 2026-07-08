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

            // Check if any ADMIN user exists (SUPER_ADMIN or CLINIC_ADMIN)
            boolean adminExists = false;
            try {
                long superAdminCount = userRepository.findByRoleName(RoleName.SUPER_ADMIN.name()).size();
                long clinicAdminCount = userRepository.findByRoleName(RoleName.CLINIC_ADMIN.name()).size();
                if (superAdminCount > 0 || clinicAdminCount > 0) {
                    adminExists = true;
                }
            } catch (Exception e) {
                // Ignore / fallback
            }

            if (!adminExists) {
                String rawPassword = "Admin!" + java.util.UUID.randomUUID().toString().substring(0, 8);
                User admin = new User();
                admin.setEmail("admin@aruclinic.com");
                admin.setFirstName("System");
                admin.setLastName("Admin");
                admin.setMobileNumber("9999999999");
                admin.setPassword(passwordEncoder.encode(rawPassword));

                Role role = roleRepository.findByName(RoleName.SUPER_ADMIN.name()).orElseThrow();
                admin.addRole(role);

                userRepository.save(admin);

                System.out.println("==================================================");
                System.out.println("      AruClinic Initial Bootstrap Admin Created   ");
                System.out.println("      Email: admin@aruclinic.com                  ");
                System.out.println("      Temporary Password: " + rawPassword         );
                System.out.println("==================================================");
            }
        };
    }
}