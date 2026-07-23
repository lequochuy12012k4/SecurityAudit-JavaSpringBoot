package com.javasecurityaudit.jsa_core.service.impl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.javasecurityaudit.jsa_core.service.TokenBlackListService;
import com.javasecurityaudit.jsa_core.util.JwtTokenProvider;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlackListServiceImpl implements TokenBlackListService {

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider tokenProvider;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Override
    public void blacklistToken(String token) {
        String jti = tokenProvider.getJtiFromJWT(token);
        long remainingMs = tokenProvider.getRemainingExpirationMs(token);

        if (remainingMs > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + jti,
                    "revoked",
                    remainingMs,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            String jti = tokenProvider.getJtiFromJWT(token);
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
        } catch (Exception e) {
            return false;
        }
    }
}