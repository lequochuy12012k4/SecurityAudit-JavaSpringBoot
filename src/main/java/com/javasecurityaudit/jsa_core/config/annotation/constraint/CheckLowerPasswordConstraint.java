package com.javasecurityaudit.jsa_core.config.annotation.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import com.javasecurityaudit.jsa_core.config.annotation.validator.CheckLowerPasswordValidator;


@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
   validatedBy = { CheckLowerPasswordValidator.class}
)
public @interface CheckLowerPasswordConstraint {
    String message() default "Mật khẩu phải có ít nhất 1 ký tự in thường";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
