package com.javasecurityaudit.jsa_core.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import com.javasecurityaudit.jsa_core.enums.AuditAction;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false)
    String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    AuditAction action;

    String description;

    int status; // HTTP Status code (200, 400, 500, ...)

    String ipAddress;

    @Column(nullable = false)
    LocalDateTime timestamp;
}