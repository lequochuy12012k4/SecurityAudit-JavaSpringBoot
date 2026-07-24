package com.javasecurityaudit.jsa_core.dto.request;

import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateMyInfoRequest {
    String fullName;

    @Email(message = "Email không đúng định dạng")
    String email;

    String password;
}