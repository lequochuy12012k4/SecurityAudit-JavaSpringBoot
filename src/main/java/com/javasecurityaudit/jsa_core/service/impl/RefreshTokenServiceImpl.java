package com.javasecurityaudit.jsa_core.service.impl;

import com.javasecurityaudit.jsa_core.entity.RefreshToken;
import com.javasecurityaudit.jsa_core.entity.User;
import com.javasecurityaudit.jsa_core.repository.RefreshTokenRepository;
import com.javasecurityaudit.jsa_core.repository.UserRepository;
import com.javasecurityaudit.jsa_core.service.RefreshTokenService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {

    RefreshTokenRepository refreshTokenRepository;
    UserRepository userRepository;

    @Override
    @Transactional
    public RefreshToken saveRefreshToken(String token, String username, long expiryMs) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .username(username)
                .expiryDate(Instant.now().plusMillis(expiryMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        if (refreshTokenOpt.isPresent()) {
            RefreshToken refreshToken = refreshTokenOpt.get();
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        }
    }

    @Override
    public boolean isRevoked(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        return refreshTokenOpt.map(RefreshToken::isRevoked).orElse(true);
    }

    @Override
    @Transactional
    public void deleteByUserUsername(String username) {
        refreshTokenRepository.deleteByUserUsername(username);
    }
}