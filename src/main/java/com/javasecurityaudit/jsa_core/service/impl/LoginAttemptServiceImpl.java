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

    // @Value("${login.attempt-max}")
    static int MAX_ATTEMPTS=5 ;            // Tối đa 5 lần thử sai
    static long BLOCK_TIME_MINUTES = 15;     // Khóa 15 phút nếu vượt ngưỡng
    static long ATTEMPT_WINDOW_MINUTES = 5;  // Đếm số lần sai trong vòng 5 phút

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
            // Đặt thời gian tự xóa key đếm sau 5 phút
            redisTemplate.expire(attemptKey, ATTEMPT_WINDOW_MINUTES, TimeUnit.MINUTES);
        }

        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            // Đạt ngưỡng 5 lần -> Tạm khóa username 15 phút
            redisTemplate.opsForValue().set(getBlockKey(username), "blocked", BLOCK_TIME_MINUTES, TimeUnit.MINUTES);
            redisTemplate.delete(attemptKey); // Xóa key đếm
        }
    }

    @Override
    public void loginSucceeded(String username) {
        redisTemplate.delete(getAttemptKey(username));
        redisTemplate.delete(getBlockKey(username));
    }
}