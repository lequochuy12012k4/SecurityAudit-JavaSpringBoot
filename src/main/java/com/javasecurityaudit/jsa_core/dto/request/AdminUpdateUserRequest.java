package com.javasecurityaudit.jsa_core.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckLowerPasswordConstraint;
import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckNumericPasswordConstraint;
import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckSpecialPasswordConstraint;
import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckUpperPasswordConstraint;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUpdateUserRequest {

    @NotBlank(message = "validation.fullname.required")
    String fullName;

    @NotBlank(message = "validation.email.required")
    @Email(message = "validation.email")
    String email;

    @NotBlank(message = "validation.password.required")
    @Size(min = 6, message = "validation.password.min.length")
    @CheckUpperPasswordConstraint
    @CheckLowerPasswordConstraint
    @CheckNumericPasswordConstraint
    @CheckSpecialPasswordConstraint
    String password;

    Boolean enabled;
    Boolean accountNonLocked;
    Set<String> roles;
}