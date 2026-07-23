package com.javasecurityaudit.jsa_core.controller;

import com.javasecurityaudit.jsa_core.base.response.BaseResponse;
import com.javasecurityaudit.jsa_core.config.annotation.LogActivity;
import com.javasecurityaudit.jsa_core.dto.request.LoginRequest;
import com.javasecurityaudit.jsa_core.dto.response.JwtResponse;
import com.javasecurityaudit.jsa_core.enums.AuditAction;
import com.javasecurityaudit.jsa_core.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @LogActivity(action = AuditAction.USER_LOGIN, description = "Người dùng đăng nhập vào hệ thống")
    public BaseResponse<JwtResponse> login(@RequestBody LoginRequest request) {
        return BaseResponse.<JwtResponse>builder()
                .result(authService.login(request))
                .build();
    }
    
    @PostMapping("/logout")
    @LogActivity(action = AuditAction.USER_LOGOUT, description = "Người dùng đăng xuất khỏi hệ thống")
    public BaseResponse<String> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return BaseResponse.<String>builder()
                .result("Logout successfully!")
                .build();
    }
}