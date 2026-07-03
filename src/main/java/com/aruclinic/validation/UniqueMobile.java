package com.aruclinic.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueMobileValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueMobile {
    String message() default "Mobile number already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}