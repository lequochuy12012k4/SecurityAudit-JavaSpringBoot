package com.javasecurityaudit.jsa_core.controller;

import com.javasecurityaudit.jsa_core.base.response.BaseResponse;
import com.javasecurityaudit.jsa_core.config.annotation.LogActivity;
import com.javasecurityaudit.jsa_core.dto.request.LoginRequest;
import com.javasecurityaudit.jsa_core.dto.request.RefreshTokenRequest;
import com.javasecurityaudit.jsa_core.dto.response.JwtResponse;
import com.javasecurityaudit.jsa_core.enums.AuditAction;
import com.javasecurityaudit.jsa_core.service.AuthService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    @PostMapping("/login")
    @LogActivity(action = AuditAction.USER_LOGIN, description = "Người dùng đăng nhập vào hệ thống")
    public BaseResponse<JwtResponse> login(@RequestBody LoginRequest request) {
        return BaseResponse.<JwtResponse>builder()
                .result(authService.login(request))
                .build();
    }

    @PostMapping("/logout")
    @LogActivity(action = AuditAction.USER_LOGOUT, description = "Người dùng đăng xuất khỏi hệ thống")
    public BaseResponse<String> logout(@RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) RefreshTokenRequest request) {
        String refreshToken = request != null ? request.getRefreshToken() : null;
        authService.logout(authHeader, refreshToken);
        return BaseResponse.<String>builder()
                .result("Logout successfully!")
                .build();
    }

    @PostMapping("/refresh-token")
    @LogActivity(action = AuditAction.USER_REFRESH_TOKEN, description = "Người dùng làm mới token")
    public BaseResponse<JwtResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return BaseResponse.success(authService.refreshToken(request));
    }
}