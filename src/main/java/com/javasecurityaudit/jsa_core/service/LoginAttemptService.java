package com.javasecurityaudit.jsa_core.service;

public interface LoginAttemptService {
    void loginFailed(String username);
    boolean isBlocked(String username);
    void loginSucceeded(String username);
}
