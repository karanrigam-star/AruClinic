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

            // Ensure Super Admin user account (theinvisiblemask800@gmail.com) exists with password David@1234
            String adminEmail = "theinvisiblemask800@gmail.com";
            String adminPassword = "David@1234";

            User admin = userRepository.findByEmail(adminEmail).orElseGet(() -> {
                User newAdmin = new User();
                newAdmin.setEmail(adminEmail);
                newAdmin.setFirstName("David");
                newAdmin.setLastName("Admin");
                newAdmin.setMobileNumber("9999999999");
                Role role = roleRepository.findByName(RoleName.SUPER_ADMIN.name()).orElseThrow();
                newAdmin.addRole(role);
                return newAdmin;
            });
            admin.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);

            System.out.println("==================================================");
            System.out.println("      AruClinic Super Admin Configured            ");
            System.out.println("      Email: " + adminEmail                        );
            System.out.println("      Password: " + adminPassword                   );
            System.out.println("==================================================");
        };
    }
}