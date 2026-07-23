package com.javasecurityaudit.jsa_core.service;

import com.javasecurityaudit.jsa_core.dto.request.LoginRequest;
import com.javasecurityaudit.jsa_core.dto.response.JwtResponse;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    void logout(String bearerToken);
}