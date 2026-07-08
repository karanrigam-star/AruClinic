package com.aruclinic.service;

import com.aruclinic.dto.*;
import com.aruclinic.entity.*;
import java.util.List;

/**
 * Service interface for user management.
 */
public interface UserService {

    UserDto registerUser(UserDto userDto);

    UserDto loginUser(String email, String password);

    UserDto updateUser(Long id, UserDto userDto);

    UserDto getUserById(Long id);

    UserDto getUserByEmail(String email);

    void deleteUser(Long id);

    List<UserDto> getAllUsers();

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    boolean verifyOtp(String email, String mobileNumber, String otpCode);

    boolean requestPasswordReset(String email);

    boolean resetPassword(String email, String newPassword);

    void forgotPassword(String email);

    void changePassword(String oldPassword, String newPassword);

    User getUserEntityByEmail(String email);

    User getUserEntityById(Long id);
}