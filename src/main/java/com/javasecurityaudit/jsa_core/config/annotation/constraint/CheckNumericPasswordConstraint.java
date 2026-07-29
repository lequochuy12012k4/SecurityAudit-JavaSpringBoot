package com.javasecurityaudit.jsa_core.config.annotation.constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.javasecurityaudit.jsa_core.config.annotation.validator.CheckNumericPasswordValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
   validatedBy = { CheckNumericPasswordValidator.class }
)
public @interface CheckNumericPasswordConstraint {
    String message() default "Mật khẩu phải có ít nhất 1 ký tự số";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
