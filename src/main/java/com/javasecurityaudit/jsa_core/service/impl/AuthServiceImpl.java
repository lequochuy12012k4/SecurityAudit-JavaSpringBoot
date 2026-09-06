package com.javasecurityaudit.jsa_core.service.impl;

import com.javasecurityaudit.jsa_core.dto.request.LoginRequest;
import com.javasecurityaudit.jsa_core.dto.request.RefreshTokenRequest;
import com.javasecurityaudit.jsa_core.dto.response.JwtResponse;
import com.javasecurityaudit.jsa_core.entity.User;
import com.javasecurityaudit.jsa_core.exception.AppException;
import com.javasecurityaudit.jsa_core.exception.ErrorCode;
import com.javasecurityaudit.jsa_core.repository.JPA.UserRepository;
import com.javasecurityaudit.jsa_core.service.AuthService;
import com.javasecurityaudit.jsa_core.service.LoginAttemptService;
import com.javasecurityaudit.jsa_core.service.RefreshTokenService;
import com.javasecurityaudit.jsa_core.service.TokenBlackListService;
import com.javasecurityaudit.jsa_core.util.JwtTokenProvider;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    AuthenticationManager authenticationManager;
    JwtTokenProvider jwtTokenProvider;
    UserRepository userRepository;
    RefreshTokenService refreshTokenService;
    TokenBlackListService tokenBlackListService;
    LoginAttemptService loginAttemptService;

    @Override
    public JwtResponse login(LoginRequest request) {
        String username = request.getUsername();
        if (loginAttemptService.isBlocked(username)) {
            throw new AppException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        }

        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            loginAttemptService.loginSucceeded(username);

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());

            long refreshExpiryMs = jwtTokenProvider.getRefreshExpirationMs();
            refreshTokenService.saveRefreshToken(refreshToken, authentication.getName(), refreshExpiryMs);

            return JwtResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(username);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Override
    public JwtResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (refreshTokenService.isRevoked(refreshToken)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(
                        role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName()))
                .collect(Collectors.toList());

        var authentication = new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(
                        user.getUsername(), user.getPassword(), authorities),
                null,
                authorities);

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

        long refreshExpiryMs = jwtTokenProvider.getRefreshExpirationMs();
        refreshTokenService.saveRefreshToken(newRefreshToken, username, refreshExpiryMs);
        // refreshTokenService.revokeRefreshToken(refreshToken);
        refreshTokenService.deleteRefreshToken(refreshToken);

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void logout(String authHeader, String refreshToken) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            tokenBlackListService.blacklistToken(accessToken);
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            // refreshTokenService.revokeRefreshToken(refreshToken);
            refreshTokenService.deleteRefreshToken(refreshToken);
        }
    }
}