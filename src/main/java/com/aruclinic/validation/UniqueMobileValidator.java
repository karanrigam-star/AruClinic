package com.aruclinic.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.aruclinic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validator for @UniqueMobile annotation.
 */
public class UniqueMobileValidator implements ConstraintValidator<UniqueMobile, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void initialize(UniqueMobile constraintAnnotation) {
        // Initialization logic if needed
    }

    @Override
    public boolean isValid(String mobileNumber, ConstraintValidatorContext context) {
        if (mobileNumber == null || mobileNumber.isBlank()) {
            return true;
        }
        return !userRepository.existsByMobileNumber(mobileNumber);
    }
}