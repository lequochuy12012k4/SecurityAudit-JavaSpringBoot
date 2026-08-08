package com.javasecurityaudit.jsa_core.config.annotation.validator;

import java.util.Objects;

import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckUpperPasswordConstraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CheckUpperPasswordValidator implements ConstraintValidator<CheckUpperPasswordConstraint,String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // Allow null or empty password (means user doesn't want to update password)
        if (Objects.isNull(password) || password.isEmpty()) {
            return true;
        }
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void initialize(CheckUpperPasswordConstraint constraintAnnotation) {
        
    }
}