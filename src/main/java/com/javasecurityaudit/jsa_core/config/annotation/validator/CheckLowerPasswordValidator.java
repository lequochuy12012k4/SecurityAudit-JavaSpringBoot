package com.javasecurityaudit.jsa_core.config.annotation.validator;

import java.util.Objects;

import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckLowerPasswordConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CheckLowerPasswordValidator implements ConstraintValidator<CheckLowerPasswordConstraint,String> {
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (Objects.isNull(password) || password.isEmpty()) {
            return false;
        }
        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void initialize(CheckLowerPasswordConstraint constraintAnnotation) {
        
    }
}
