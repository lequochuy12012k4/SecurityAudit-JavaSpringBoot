package com.javasecurityaudit.jsa_core.service.impl;

import com.javasecurityaudit.jsa_core.service.RefreshTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenRedisServiceImpl implements RefreshTokenService {

    StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_REFRESH_TOKENS_PREFIX = "user_refresh_tokens:";

    @Override
    public void saveRefreshToken(String token, String username, long expiryMs) {
        String key = REFRESH_TOKEN_PREFIX + token;
        String userKey = USER_REFRESH_TOKENS_PREFIX + username;

        // Store token with username and expiry
        redisTemplate.opsForValue().set(key, username, expiryMs, TimeUnit.MILLISECONDS);

        // Add token to user's token set for easy deletion
        redisTemplate.opsForSet().add(userKey, token);
        redisTemplate.expire(userKey, expiryMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void revokeRefreshToken(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        String username = redisTemplate.opsForValue().get(key);

        if (username != null) {
            // Mark as revoked by setting a special value
            redisTemplate.opsForValue().set(key, "REVOKED:" + username);

            // Remove from user's token set
            String userKey = USER_REFRESH_TOKENS_PREFIX + username;
            redisTemplate.opsForSet().remove(userKey, token);
        }
    }

    @Override
    public boolean isRevoked(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        String value = redisTemplate.opsForValue().get(key);

        // Token doesn't exist or is marked as revoked
        return value == null || value.startsWith("REVOKED:");
    }

    @Override
    public void deleteByUserUsername(String username) {
        String userKey = USER_REFRESH_TOKENS_PREFIX + username;
        Set<String> tokens = redisTemplate.opsForSet().members(userKey);

        if (tokens != null && !tokens.isEmpty()) {
            // Delete all refresh tokens for this user
            for (String token : tokens) {
                redisTemplate.delete(REFRESH_TOKEN_PREFIX + token);
            }
            // Delete the user's token set
            redisTemplate.delete(userKey);
        }
    }
}