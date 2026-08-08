package com.javasecurityaudit.jsa_core.config.annotation.validator;

import java.util.Objects;

import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckSpecialPasswordConstraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CheckSpecialPasswordValidator implements ConstraintValidator<CheckSpecialPasswordConstraint,String> {
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (Objects.isNull(password) || password.isEmpty()) {
            return true;
        }
        return password.matches(".*[^a-zA-Z0-9].*");
    }
    @Override
    public void initialize(CheckSpecialPasswordConstraint constraintAnnotation) {
        
    }
}
