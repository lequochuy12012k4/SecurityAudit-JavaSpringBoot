package com.javasecurityaudit.jsa_core.config.aspect;

import com.javasecurityaudit.jsa_core.config.annotation.LogActivity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityLogAspect {

    private final StringRedisTemplate redisTemplate;

    @Value("${audit.redis-stream-key:audit:activity:stream}")
    private String streamKey;

    @Around("@annotation(logActivity)")
    public Object logUserActivity(ProceedingJoinPoint joinPoint, LogActivity logActivity) throws Throwable {
        Object result = null;
        int httpStatus = 200;

        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            httpStatus = 500;
            throw throwable;
        } finally {
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "ANONYMOUS";

                Map<String, String> body = new HashMap<>();
                body.put("username", username);
                body.put("action", logActivity.action().name());
                body.put("description", logActivity.description());
                body.put("status", String.valueOf(httpStatus));
                body.put("timestamp", LocalDateTime.now().toString());

                if (request != null) {
                    body.put("ipAddress", getClientIp(request));
                    body.put("userAgent", request.getHeader("User-Agent"));
                }

                MapRecord<String, String, String> record = MapRecord.create(streamKey, body);
                redisTemplate.opsForStream().add(record);

            } catch (Exception e) {
                log.error("Lỗi khi đẩy Log vào Redis Stream: {}", e.getMessage());
            }
        }
        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}