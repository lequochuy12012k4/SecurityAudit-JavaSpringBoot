package com.javasecurityaudit.jsa_core.config.annotation.validator;

import java.util.Objects;

import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckNumericPasswordConstraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CheckNumericPasswordValidator implements ConstraintValidator<CheckNumericPasswordConstraint,String> {
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (Objects.isNull(password) || password.isEmpty()) {
            return true;
        }
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void initialize(CheckNumericPasswordConstraint constraintAnnotation) {
        
    }
}
