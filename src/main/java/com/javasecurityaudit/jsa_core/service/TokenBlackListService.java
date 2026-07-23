package com.javasecurityaudit.jsa_core.service;

public interface TokenBlackListService {
    void blacklistToken(String token);
    boolean isBlacklisted(String token);
}