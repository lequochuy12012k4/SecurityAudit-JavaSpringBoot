package com.javasecurityaudit.jsa_core.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE)
public class ApiErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime timestamp;
    int code;
    int status;
    String error;
    String message;
    String path;
}