package com.javasecurityaudit.jsa_core.repository;

import com.javasecurityaudit.jsa_core.entity.RefreshToken;
import com.javasecurityaudit.jsa_core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    int deleteByUser(User user);
}