package com.javasecurityaudit.jsa_core.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.javasecurityaudit.jsa_core.service.TokenBlackListService;
import com.javasecurityaudit.jsa_core.util.JwtTokenProvider;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final TokenBlackListService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Rút Bearer Token từ Header Authorization
            String jwt = getJwtFromRequest(request);

            // 2. Validate Token: Kiểm tra format + Chữ ký + Hạn dùng + Check Redis Blacklist
            if (StringUtils.hasText(jwt) 
                    && tokenProvider.validateToken(jwt) 
                    && !tokenBlacklistService.isBlacklisted(jwt)) {

                // 3. Đọc Username từ Token
                String username = tokenProvider.getUsernameFromJWT(jwt);
                List<GrantedAuthority> authorities = tokenProvider.getAuthoritiesFromJWT(jwt);
                // 4. Load UserDetails từ Database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 5. Thiết lập Authentication vào SecurityContext
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Không thể thiết lập thông tin xác thực vào Security Context: {}", ex.getMessage());
        }

        // Chuyển tiếp Request sang Filter tiếp theo trong Filter Chain
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}