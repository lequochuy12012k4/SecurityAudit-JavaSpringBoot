package com.javasecurityaudit.jsa_core.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserStatusRequest {
    Boolean enabled;
    Boolean accountNonLocked;
}