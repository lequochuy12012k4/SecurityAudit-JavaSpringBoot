package com.javasecurityaudit.jsa_core.service.impl;

import com.javasecurityaudit.jsa_core.dto.request.LoginRequest;
import com.javasecurityaudit.jsa_core.dto.request.RefreshTokenRequest;
import com.javasecurityaudit.jsa_core.dto.response.JwtResponse;
import com.javasecurityaudit.jsa_core.entity.User;
import com.javasecurityaudit.jsa_core.exception.AppException;
import com.javasecurityaudit.jsa_core.exception.ErrorCode;
import com.javasecurityaudit.jsa_core.repository.UserRepository;
import com.javasecurityaudit.jsa_core.service.AuthService;
import com.javasecurityaudit.jsa_core.service.RefreshTokenService;
import com.javasecurityaudit.jsa_core.service.TokenBlackListService;
import com.javasecurityaudit.jsa_core.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlackListService tokenBlackListService;

    @Override
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());

        // Save refresh token to database
        long refreshExpiryMs = jwtTokenProvider.getRefreshExpirationMs();
        refreshTokenService.saveRefreshToken(refreshToken, authentication.getName(), refreshExpiryMs);

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public JwtResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Check if refresh token is revoked in database
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

        // Revoke old refresh token and save new one
        refreshTokenService.revokeRefreshToken(refreshToken);
        long refreshExpiryMs = jwtTokenProvider.getRefreshExpirationMs();
        refreshTokenService.saveRefreshToken(newRefreshToken, username, refreshExpiryMs);

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void logout(String authHeader, String refreshToken) {
        // Blacklist access token in Redis
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            tokenBlackListService.blacklistToken(accessToken);
        }

        // Revoke refresh token in database
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }
    }
}