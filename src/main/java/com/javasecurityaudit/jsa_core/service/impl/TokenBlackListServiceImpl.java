package com.javasecurityaudit.jsa_core.service.impl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.javasecurityaudit.jsa_core.service.TokenBlackListService;
import com.javasecurityaudit.jsa_core.util.JwtTokenProvider;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenBlackListServiceImpl implements TokenBlackListService {

    StringRedisTemplate redisTemplate;
    JwtTokenProvider jwtTokenProvider;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Override
    public void blacklistToken(String token) {
        String jti =jwtTokenProvider.getJtiFromJWT(token);
        long remainingMs = jwtTokenProvider.getRemainingExpirationMs(token);

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
            String jti = jwtTokenProvider.getJtiFromJWT(token);
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
        } catch (Exception e) {
            return false;
        }
    }
}