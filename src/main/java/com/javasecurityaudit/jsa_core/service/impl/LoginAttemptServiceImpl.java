package com.javasecurityaudit.jsa_core.service.impl;

import com.javasecurityaudit.jsa_core.service.LoginAttemptService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoginAttemptServiceImpl implements LoginAttemptService {

    StringRedisTemplate redisTemplate;

    @Value("${login.attempt-max}")
    static int MAX_ATTEMPTS; 
    @Value("${login.block-time-minutes}")       
    static long BLOCK_TIME_MINUTES;  
    @Value("${login.attempt-window-minutes}") 
    static long ATTEMPT_WINDOW_MINUTES; 

    private String getAttemptKey(String username) {
        return "login:attempt:" + username;
    }

    private String getBlockKey(String username) {
        return "login:block:" + username;
    }

    @Override
    public boolean isBlocked(String username) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(getBlockKey(username)));
    }

    @Override
    public void loginFailed(String username) {
        String attemptKey = getAttemptKey(username);
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptKey, ATTEMPT_WINDOW_MINUTES, TimeUnit.MINUTES);
        }

        if (attempts != null && attempts >= MAX_ATTEMPTS) {

            redisTemplate.opsForValue().set(getBlockKey(username), "blocked", BLOCK_TIME_MINUTES, TimeUnit.MINUTES);
            redisTemplate.delete(attemptKey);
        }
    }

    @Override
    public void loginSucceeded(String username) {
        redisTemplate.delete(getAttemptKey(username));
        redisTemplate.delete(getBlockKey(username));
    }
}