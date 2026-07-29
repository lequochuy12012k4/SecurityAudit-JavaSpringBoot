package com.javasecurityaudit.jsa_core.dto.request;

import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckLowerPasswordConstraint;
import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckNumericPasswordConstraint;
import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckSpecialPasswordConstraint;
import com.javasecurityaudit.jsa_core.config.annotation.constraint.CheckUpperPasswordConstraint;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateUserRequest {

    @Size(min = 4, max = 10, message = "Username phải từ 4 đến 10 ký tự")
    String username;
    
    @NotBlank(message = "Mật khẩu cũ không được để trống")
    @Size(min = 6, message = "Password phải từ 6 ký tự trở lên")
    @CheckUpperPasswordConstraint
    @CheckLowerPasswordConstraint
    @CheckNumericPasswordConstraint
    @CheckSpecialPasswordConstraint
    String password;

    @Email(message = "Email không đúng định dạng")
    String email;

    String fullName;
}