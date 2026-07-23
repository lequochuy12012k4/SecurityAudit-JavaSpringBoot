package com.javasecurityaudit.jsa_core.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtResponse {

    String accessToken;

    @Builder.Default
    String tokenType = "Bearer";
}