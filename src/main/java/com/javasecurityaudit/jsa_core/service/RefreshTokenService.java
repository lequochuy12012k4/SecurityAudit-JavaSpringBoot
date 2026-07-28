package com.javasecurityaudit.jsa_core.service;

import com.javasecurityaudit.jsa_core.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken saveRefreshToken(String token, String username, long expiryMs);

    void revokeRefreshToken(String token);

    boolean isRevoked(String token);

    void deleteByUserUsername(String username);
}