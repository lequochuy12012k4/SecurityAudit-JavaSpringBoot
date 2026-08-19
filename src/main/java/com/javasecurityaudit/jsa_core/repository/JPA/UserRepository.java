package com.javasecurityaudit.jsa_core.repository.JPA;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javasecurityaudit.jsa_core.entity.User;

public interface UserRepository extends JpaRepository<User,String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username); 
}
