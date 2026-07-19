package com.aruclinic.service.impl;

import com.aruclinic.dto.*;
import com.aruclinic.entity.*;
import com.aruclinic.exception.*;
import com.aruclinic.mapper.*;
import com.aruclinic.repository.*;
import com.aruclinic.security.util.JwtTokenProvider;
import com.aruclinic.service.OtpService;
import com.aruclinic.service.UserService;
import com.aruclinic.service.AdminService;
import com.aruclinic.util.EmailUtil;
import com.aruclinic.util.SmsUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of UserService.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final java.security.SecureRandom secureRandom = new java.security.SecureRandom();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailUtil emailUtil;
    private final SmsUtil smsUtil;
    private final OtpService otpService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminService adminService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                          UserMapper userMapper, org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder bCryptPasswordEncoder,
                          JwtTokenProvider jwtTokenProvider, EmailUtil emailUtil,
                          SmsUtil smsUtil, OtpService otpService,
                          PatientRepository patientRepository,
                          DoctorRepository doctorRepository,
                          @org.springframework.context.annotation.Lazy AdminService adminService,
                          org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailUtil = emailUtil;
        this.smsUtil = smsUtil;
        this.otpService = otpService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.adminService = adminService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @PreAuthorize("permitAll()")
    public UserDto registerUser(UserDto userDto) {
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            throw new AruClinicException("Password and confirm password do not match");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UniqueEmailException("Email already exists");
        }
        if (userRepository.existsByMobileNumber(userDto.getMobileNumber())) {
            throw new UniqueMobileException("Mobile number already exists");
        }

        User user = userMapper.toUser(userDto);
        user.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));

        Role role = roleRepository.findByName(RoleName.PATIENT.name())
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(RoleName.PATIENT.name());
                    return roleRepository.save(r);
                });
        user.addRole(role);

        userRepository.save(user);

        // Automatically create corresponding Patient record
        try {
            Patient patient = new Patient();
            patient.setEmail(user.getEmail());
            patient.setFirstName(user.getFirstName());
            patient.setLastName(user.getLastName());
            patient.setMobileNumber(user.getMobileNumber());
            patient.setDateOfBirth(userDto.getDateOfBirth() != null ? userDto.getDateOfBirth() : java.time.LocalDate.of(1995, 1, 1));
            patient.setAge(userDto.getDateOfBirth() != null ? java.time.Period.between(userDto.getDateOfBirth(), java.time.LocalDate.now()).getYears() : 31);
            patient.setGender(userDto.getGender() != null ? userDto.getGender() : "Other");
            patient.setBloodGroup(userDto.getBloodGroup() != null ? userDto.getBloodGroup() : "O+");
            patient.setAddress(userDto.getAddress() != null ? userDto.getAddress() : "Registered via self-signup");
            patient.setCity(userDto.getCity());
            patient.setState(userDto.getState());
            patient.setZipCode(userDto.getZipCode());
            patient.setDistrict(userDto.getDistrict());
            patient.setEmergencyContact(userDto.getEmergencyContactName());
            patient.setEmergencyPhone(userDto.getEmergencyPhone());
            patientRepository.save(patient);
        } catch (Exception e) {
            // Ignore to make sure user registration flow doesn't block
        }

        // Generate and send OTP using SecureRandom
        String otp = String.valueOf(secureRandom.nextInt(900000) + 100000);

        // Save OTP to database
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(userDto.getEmail());
        otpVerification.setMobileNumber(userDto.getMobileNumber());
        otpVerification.setOtpCode(otp);
        otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpVerification.setVerified(false);

        otpService.saveOtp(otpVerification);

        // Log OTP generation without exposing the code
        logger.info("====================================");
        logger.info("OTP GENERATED (Code masked for security)");
        logger.info("Email : {}", userDto.getEmail());
        logger.info("Mobile : {}", userDto.getMobileNumber());
        logger.info("Expires : {}", otpVerification.getExpiresAt());
        logger.info("====================================");

        // Also send via email (existing flow)
        emailUtil.sendEmail(userDto.getEmail(), "Your OTP", "Your OTP is: " + otp);

        UserDto result = userMapper.toUserDto(user);
        result.setPassword(null);
        result.setConfirmPassword(null);
        return result;
    }

    @Override
    @PreAuthorize("permitAll()")
    public UserDto loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!adminService.isUserEnabled(user.getId())) {
            throw new com.aruclinic.exception.UserDisabledException("Your account is disabled");
        }

        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new AruClinicException("Invalid credentials");
        }

        return userMapper.toUserDto(user);
    }

    @Override
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public UserDto updateUser(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        String oldEmail = existingUser.getEmail();

        if (userDto.getFirstName() != null) {
            existingUser.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            existingUser.setLastName(userDto.getLastName());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getMobileNumber() != null) {
            existingUser.setMobileNumber(userDto.getMobileNumber());
        }
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);

        // Sync with linked Patient/Doctor records
        if (oldEmail != null) {
            patientRepository.findByEmail(oldEmail).ifPresent(patient -> {
                if (userDto.getFirstName() != null) {
                    patient.setFirstName(userDto.getFirstName());
                }
                if (userDto.getLastName() != null) {
                    patient.setLastName(userDto.getLastName());
                }
                if (userDto.getEmail() != null) {
                    patient.setEmail(userDto.getEmail());
                }
                if (userDto.getMobileNumber() != null) {
                    patient.setMobileNumber(userDto.getMobileNumber());
                }
                patientRepository.save(patient);
            });
            doctorRepository.findByEmail(oldEmail).ifPresent(doctor -> {
                if (userDto.getFirstName() != null || userDto.getLastName() != null) {
                    String fn = userDto.getFirstName() != null ? userDto.getFirstName() : "";
                    String ln = userDto.getLastName() != null ? userDto.getLastName() : "";
                    if (fn.isEmpty() || ln.isEmpty()) {
                        String[] parts = doctor.getName().split(" ", 2);
                        if (fn.isEmpty()) {
                            fn = parts[0];
                        }
                        if (ln.isEmpty() && parts.length > 1) {
                            ln = parts[1];
                        }
                    }
                    doctor.setName((fn.trim() + " " + ln.trim()).trim());
                }
                if (userDto.getEmail() != null) {
                    doctor.setEmail(userDto.getEmail());
                }
                if (userDto.getMobileNumber() != null) {
                    doctor.setMobileNumber(userDto.getMobileNumber());
                }
                doctorRepository.save(doctor);
            });
        }

        return userMapper.toUserDto(updatedUser);
    }

    @Override
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapper.toUserDto(user);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserDto).toList();
    }

    @Override
    @PreAuthorize("permitAll()")
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @PreAuthorize("permitAll()")
    public boolean existsByMobileNumber(String mobileNumber) {
        return userRepository.existsByMobileNumber(mobileNumber);
    }

    @Override
    @PreAuthorize("permitAll()")
    public boolean verifyOtp(String email, String mobileNumber, String otpCode) {
        Optional<OtpVerification> otpOpt = otpService.findByEmailAndMobileNumber(email, mobileNumber);
        
        // Hash the user-inputted OTP code using SHA-256 for comparison with secure stored hash
        String hashedOtpInput;
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(otpCode.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            hashedOtpInput = hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash user input OTP", e);
        }

        if (otpOpt.isPresent() && otpOpt.get().getOtpCode().equals(hashedOtpInput) && otpOpt.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            OtpVerification otpVerification = otpOpt.get();
            otpVerification.setVerified(true);
            otpService.saveOtp(otpVerification);

            // Enable the user
            userRepository.findByEmail(email).ifPresent(user -> {
                jdbcTemplate.update("DELETE FROM clinic_settings WHERE setting_key = ?", "user_disabled_" + user.getId());
            });

            // Delete OTP verification record
            otpService.deleteOtp(otpVerification);
            return true;
        }
        return false;
    }

    @Override
    @PreAuthorize("permitAll()")
    public boolean requestPasswordReset(String email) {
        forgotPassword(email);
        return true;
    }

    @Override
    @PreAuthorize("permitAll()")
    public boolean resetPassword(String token, String newPassword) {
        String hashedToken;
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            hashedToken = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new AruClinicException("Failed to hash reset token", e);
        }

        OtpVerification otpVerification = otpService.findByOtpCode(hashedToken)
                .orElseThrow(() -> new AruClinicException("Invalid or expired password reset token"));
        
        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpService.deleteOtp(otpVerification);
            throw new AruClinicException("Invalid or expired password reset token");
        }

        User user = userRepository.findByEmail(otpVerification.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete token after use
        otpService.deleteOtp(otpVerification);
        logger.info("Password reset successfully for user: {}", user.getEmail());
        return true;
    }

	@Override
	@PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
	public UserDto getUserByEmail(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
		return userMapper.toUserDto(user);
	}

	@Override
	@PreAuthorize("permitAll()")
	public void forgotPassword(String email) {
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) {
			logger.warn("Password reset requested for non-existing email: {}", email);
			return;
		}

		User user = userOpt.get();

		// Generate a secure random 256-bit token for password reset
		byte[] tokenBytes = new byte[32];
		secureRandom.nextBytes(tokenBytes);
		String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

		// Hash the token using SHA-256
		String hashedToken;
		try {
			java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			hashedToken = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new AruClinicException("Failed to hash reset token", e);
		}

		// Clean up existing reset/OTP token for user to enforce single-use behavior
		Optional<OtpVerification> existing = otpService.findByEmailAndMobileNumber(user.getEmail(), user.getMobileNumber());
		existing.ifPresent(otpService::deleteOtp);

		OtpVerification otpVerification = new OtpVerification();
		otpVerification.setEmail(user.getEmail());
		otpVerification.setMobileNumber(user.getMobileNumber());
		otpVerification.setOtpCode(hashedToken);
		otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(15)); // 15 mins expiry
		otpVerification.setVerified(false);

		otpService.saveOtp(otpVerification);

		logger.info("Password reset token generated and hashed successfully for {}", email);

		String resetLink = "http://localhost:8080/auth/reset-password?token=" + rawToken;
		emailUtil.sendEmail(user.getEmail(), "Password Reset Request", 
				"Dear " + user.getFirstName() + ",\n\n" +
				"To reset your AruClinic account password, please click the link below (valid for 15 minutes):\n" +
				resetLink + "\n\n" +
				"If you did not request this, please ignore this email.\n\n" +
				"Best regards,\n" +
				"AruClinic Team");
	}

	@Override
	@PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN', 'SUPER_ADMIN')")
	public void changePassword(String oldPassword, String newPassword) {
		// Get current user from security context
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		if (!bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
			throw new AruClinicException("Old password is incorrect");
		}

		user.setPassword(bCryptPasswordEncoder.encode(newPassword));
		userRepository.save(user);
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	public User getUserEntityByEmail(String email) {
		return userRepository.findByEmail(email).orElse(null);
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
	public User getUserEntityById(Long id) {
		return userRepository.findById(id).orElse(null);
	}
}