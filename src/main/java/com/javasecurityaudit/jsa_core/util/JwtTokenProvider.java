package com.javasecurityaudit.jsa_core.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret-key}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs; // 👈 Thêm thời hạn Refresh Token trong application.properties

    // 1. Sinh Access Token (15 phút)
    public String generateAccessToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String scope = buildScope(authentication);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userPrincipal.getUsername())
                .issueTime(now)
                .expirationTime(expiryDate)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", scope)
                .build();

        return signJWT(claimsSet);
    }

    // 2. Sinh Refresh Token (Tạo JWT Refresh Token sống 7 ngày)
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(username)
                .issueTime(now)
                .expirationTime(expiryDate)
                .jwtID(UUID.randomUUID().toString())
                .claim("type", "REFRESH") // Đánh dấu đây là Refresh Token
                .build();

        return signJWT(claimsSet);
    }

    // Helper ký JWT
    private String signJWT(JWTClaimsSet claimsSet) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        try {
            JWSSigner signer = new MACSigner(jwtSecret.getBytes());
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Lỗi khi ký JWT Token: {}", e.getMessage());
            throw new RuntimeException("Cannot generate JWT token", e);
        }
    }

    // 3. Validate Token tổng quát
    public boolean validateToken(String authToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(authToken);
            JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes());

            if (!signedJWT.verify(verifier)) {
                log.error("Chữ ký JWT không hợp lệ");
                return false;
            }

            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration != null && expiration.before(new Date())) {
                log.error("JWT Token đã hết hạn");
                return false;
            }

            return true;
        } catch (ParseException | JOSEException e) {
            log.error("Token không hợp lệ: {}", e.getMessage());
        }
        return false;
    }

    // 4. Lấy Username từ Token
    public String getUsernameFromJWT(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            return null;
        }
    }

    // 5. Lấy JTI từ Token (Phục vụ Redis Blacklist)
    public String getJtiFromJWT(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getJWTID();
        } catch (ParseException e) {
            return null;
        }
    }

    public long getRemainingExpirationMs(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration == null) return 0;

            long diff = expiration.getTime() - System.currentTimeMillis();
            return Math.max(diff, 0);
        } catch (ParseException e) {
            log.error("Lỗi khi tính thời gian hết hạn còn lại: {}", e.getMessage());
            return 0;
        }
    }

    // 6. Lấy Danh sách Authorities từ Token
    public List<GrantedAuthority> getAuthoritiesFromJWT(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String scope = signedJWT.getJWTClaimsSet().getStringClaim("scope");
            
            if (!StringUtils.hasText(scope)) {
                return Collections.emptyList();
            }

            return Arrays.stream(scope.split(" "))
                    .filter(StringUtils::hasText)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } catch (ParseException e) {
            return Collections.emptyList();
        }
    }

    private String buildScope(Authentication authentication) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String authStr = authority.getAuthority();
            if (authStr != null && authStr.startsWith("ROLE_")) {
                stringJoiner.add(authStr);
            }
        }
        return stringJoiner.toString();
    }
}