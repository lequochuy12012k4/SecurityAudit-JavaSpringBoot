package com.javasecurityaudit.jsa_core.dto.response;

import com.javasecurityaudit.jsa_core.dto.response.audit.AuditResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse extends AuditResponse {

    String id;
    String username;
    String email;
    String fullName;
    boolean enabled;
    boolean accountNonLocked;
    Set<String> roles;
}