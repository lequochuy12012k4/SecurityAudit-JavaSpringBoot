package com.javasecurityaudit.jsa_core.repository.JPA;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javasecurityaudit.jsa_core.entity.Role;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(String name);
}
