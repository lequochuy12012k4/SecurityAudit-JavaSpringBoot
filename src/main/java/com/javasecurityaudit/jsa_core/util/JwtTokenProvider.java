package com.javasecurityaudit.jsa_core.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret-key}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // Tạo Payload (JWT Claims)
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userPrincipal.getUsername())
                .issueTime(now)
                .expirationTime(expiryDate)
                .jwtID(UUID.randomUUID().toString()) // JTI unique
                .build();

        // Tạo Header chỉ định thuật toán HS256
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        // Ký JWT với Secret Key
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

    // 2. Lấy Username từ Token
    public String getUsernameFromJWT(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            log.error("Không thể đọc Username từ Token: {}", e.getMessage());
            return null;
        }
    }

    // 3. Lấy JTI từ Token (Phục vụ Redis Blacklist)
    public String getJtiFromJWT(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getJWTID();
        } catch (ParseException e) {
            log.error("Không thể đọc JTI từ Token: {}", e.getMessage());
            return null;
        }
    }

    // 4. Lấy thời gian sống còn lại của Token (Ms)
    public long getRemainingExpirationMs(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration == null) return 0;

            long diff = expiration.getTime() - System.currentTimeMillis();
            return Math.max(diff, 0);
        } catch (ParseException e) {
            return 0;
        }
    }

    // 5. Check Token hợp lệ (Chữ ký & Thời hạn)
    public boolean validateToken(String authToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(authToken);
            JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes());

            // 1. Kiểm tra chữ ký
            if (!signedJWT.verify(verifier)) {
                log.error("Chữ ký JWT không hợp lệ");
                return false;
            }

            // 2. Kiểm tra thời hạn hết hạn
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration != null && expiration.before(new Date())) {
                log.error("JWT Token đã hết hạn");
                return false;
            }

            return true;
        } catch (ParseException e) {
            log.error("Định dạng JWT không hợp lệ: {}", e.getMessage());
        } catch (JOSEException e) {
            log.error("Lỗi khi verify chữ ký JWT: {}", e.getMessage());
        }
        return false;
    }
}