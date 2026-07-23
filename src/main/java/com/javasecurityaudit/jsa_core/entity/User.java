package com.javasecurityaudit.jsa_core.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

import com.javasecurityaudit.jsa_core.base.audit.BaseAuditTrail;

@Entity
@Table(name = "users")
@Getter // 👈 Đảm bảo sinh ra getRoles(), getUsername(), ... giải quyết triệt để lỗi "undefined method getRoles()"
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseAuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true, length = 50)
    String username;

    @Column(nullable = false)
    String password;

    @Column(nullable = false, unique = true, length = 100)
    String email;

    String fullName;

    @Builder.Default
    boolean enabled = true;

    @Builder.Default
    boolean accountNonLocked = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    Set<Role> roles = new HashSet<>();
}