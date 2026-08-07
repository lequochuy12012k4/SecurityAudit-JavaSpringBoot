package com.javasecurityaudit.jsa_core.dto.request;

import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckLowerPasswordConstraint;
import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckNumericPasswordConstraint;
import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckSpecialPasswordConstraint;
import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckUpperPasswordConstraint;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateMyInfoRequest {
    @NotBlank(message = "validation.fullname.required")
    String fullName;

    @Email(message = "validation.email")
    String email;

    @NotBlank(message = "validation.password.required")
    @CheckUpperPasswordConstraint
    @CheckLowerPasswordConstraint
    @CheckNumericPasswordConstraint
    @CheckSpecialPasswordConstraint
    String password;
}