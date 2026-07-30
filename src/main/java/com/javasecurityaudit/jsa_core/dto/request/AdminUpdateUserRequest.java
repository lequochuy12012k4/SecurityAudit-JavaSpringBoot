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

    @NotBlank(message = "Họ tên không được để trống")
    String fullName;

    @NotBlank(message = "Email mới không được để trống")
    @Email(message = "Email không đúng định dạng")
    String email;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Mật khẩu tối thiểu phải từ 6 ký tự trở lên")
    @CheckUpperPasswordConstraint
    @CheckLowerPasswordConstraint
    @CheckNumericPasswordConstraint
    @CheckSpecialPasswordConstraint
    String password;
    
    Boolean enabled;
    Boolean accountNonLocked;
    Set<String> roles;
}