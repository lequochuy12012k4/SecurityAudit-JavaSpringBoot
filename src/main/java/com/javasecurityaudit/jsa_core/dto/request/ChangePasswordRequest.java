package com.javasecurityaudit.jsa_core.dto.request;

import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckLowerPasswordConstraint;
import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckNumericPasswordConstraint;
import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckSpecialPasswordConstraint;
import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckUpperPasswordConstraint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {

    @NotBlank(message = "validation.password.required")
    String oldPassword;

    @NotBlank(message = "validation.password.required")
    @Size(min = 6, message = "validation.password.min.length")
    @CheckUpperPasswordConstraint
    @CheckLowerPasswordConstraint
    @CheckNumericPasswordConstraint
    @CheckSpecialPasswordConstraint
    String newPassword;
}