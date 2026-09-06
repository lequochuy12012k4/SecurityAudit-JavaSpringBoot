package com.javasecurityaudit.jsa_core.service;

public interface RefreshTokenService {
    void saveRefreshToken(String token, String username, long expiryMs);

    void revokeRefreshToken(String token);

    void deleteRefreshToken(String token);

    boolean isRevoked(String token);

    void deleteByUserUsername(String username);
}