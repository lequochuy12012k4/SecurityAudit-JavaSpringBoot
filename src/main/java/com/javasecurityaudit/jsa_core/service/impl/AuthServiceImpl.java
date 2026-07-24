package com.javasecurityaudit.jsa_core.service.impl;

import com.javasecurityaudit.jsa_core.dto.request.LoginRequest;
import com.javasecurityaudit.jsa_core.dto.request.RefreshTokenRequest;
import com.javasecurityaudit.jsa_core.dto.response.JwtResponse;
import com.javasecurityaudit.jsa_core.entity.User;
import com.javasecurityaudit.jsa_core.exception.AppException;
import com.javasecurityaudit.jsa_core.exception.ErrorCode;
import com.javasecurityaudit.jsa_core.repository.UserRepository;
import com.javasecurityaudit.jsa_core.service.AuthService;
import com.javasecurityaudit.jsa_core.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate; // 👈 Import StringRedisTemplate
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
    private final StringRedisTemplate redisTemplate; // 👈 Thêm field này (Lombok @RequiredArgsConstructor sẽ tự inject)

    @Override
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());

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

        String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(
                        role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName()
                ))
                .collect(Collectors.toList());

        var authentication = new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(
                        user.getUsername(), user.getPassword(), authorities),
                null,
                authorities
        );

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String token = authHeader.substring(7);
        String jti = jwtTokenProvider.getJtiFromJWT(token);
        long remainingMs = jwtTokenProvider.getRemainingExpirationMs(token);

        if (jti != null && remainingMs > 0) {
            String blacklistKey = "jwt:blacklist:" + jti;
            redisTemplate.opsForValue().set(
                    blacklistKey,
                    "revoked",
                    remainingMs,
                    TimeUnit.MILLISECONDS
            );
        }
    }
}