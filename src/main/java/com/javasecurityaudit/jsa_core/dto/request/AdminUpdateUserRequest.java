package com.javasecurityaudit.jsa_core.dto.request;

import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUpdateUserRequest {

    String fullName;

    @Email(message = "Email không đúng định dạng")
    String email;

    String password; // Nếu truyền lên thì sẽ mã hóa và đổi mật khẩu mới cho user
    Boolean enabled;
    Boolean accountNonLocked;
    Set<String> roles; // Danh sách vai trò mới, VD: ["ROLE_ADMIN", "ROLE_USER"]
}