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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailUtil emailUtil;
    private final SmsUtil smsUtil;
    private final OtpService otpService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminService adminService;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                          UserMapper userMapper, BCryptPasswordEncoder bCryptPasswordEncoder,
                          JwtTokenProvider jwtTokenProvider, EmailUtil emailUtil,
                          SmsUtil smsUtil, OtpService otpService,
                          PatientRepository patientRepository,
                          DoctorRepository doctorRepository,
                          @org.springframework.context.annotation.Lazy AdminService adminService) {
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
    }

    @Override
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

        // Generate and send OTP
        String otp = String.valueOf(new Random().nextInt(899999) + 100000);

        // Save OTP to database
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(userDto.getEmail());
        otpVerification.setMobileNumber(userDto.getMobileNumber());
        otpVerification.setOtpCode(otp);
        otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpVerification.setVerified(false);

        otpService.saveOtp(otpVerification);

        // Log OTP for development
        logger.info("====================================");
        logger.info("OTP GENERATED");
        logger.info("Email : {}", userDto.getEmail());
        logger.info("Mobile : {}", userDto.getMobileNumber());
        logger.info("OTP : {}", otp);
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
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserDto).toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByMobileNumber(String mobileNumber) {
        return userRepository.existsByMobileNumber(mobileNumber);
    }

    @Override
    public boolean verifyOtp(String email, String mobileNumber, String otpCode) {
        return true; // Stub
    }

    @Override
    public boolean requestPasswordReset(String email) {
        return true; // Stub
    }

    @Override
    public boolean resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

	@Override
	public UserDto getUserByEmail(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
		return userMapper.toUserDto(user);
	}

	@Override
	public void forgotPassword(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		// Generate and send OTP for password reset
		String otp = String.valueOf(new Random().nextInt(899999) + 100000);

		OtpVerification otpVerification = new OtpVerification();
		otpVerification.setEmail(user.getEmail());
		otpVerification.setMobileNumber(user.getMobileNumber());
		otpVerification.setOtpCode(otp);
		otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
		otpVerification.setVerified(false);

		otpService.saveOtp(otpVerification);

		logger.info("Password reset OTP for {}: {}", email, otp);
		emailUtil.sendEmail(user.getEmail(), "Password Reset", "Your password reset OTP is: " + otp);
	}

	@Override
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
	public User getUserEntityByEmail(String email) {
		return userRepository.findByEmail(email).orElse(null);
	}

	@Override
	public User getUserEntityById(Long id) {
		return userRepository.findById(id).orElse(null);
	}
}