package com.aruclinic;

import com.aruclinic.entity.Role;
import com.aruclinic.entity.RoleName;
import com.aruclinic.entity.User;
import com.aruclinic.repository.RoleRepository;
import com.aruclinic.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class AruClinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(AruClinicApplication.class, args);
    }

    @Bean
    public CommandLineRunner init(RoleRepository roleRepository,
                                  UserRepository userRepository,
                                  BCryptPasswordEncoder passwordEncoder) {

        return args -> {

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
        };
    }
}