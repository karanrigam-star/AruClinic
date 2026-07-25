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
            // Database schema migrations are handled by Flyway (src/main/resources/db/migration)

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