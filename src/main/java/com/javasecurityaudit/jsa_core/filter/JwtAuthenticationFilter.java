package com.javasecurityaudit.jsa_core.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    JwtTokenProvider tokenProvider;
    UserDetailsService userDetailsService;
    TokenBlackListService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) 
                    && tokenProvider.validateToken(jwt) 
                    && !tokenBlacklistService.isBlacklisted(jwt)) {

                String username = tokenProvider.getUsernameFromJWT(jwt);
                List<GrantedAuthority> authorities = tokenProvider.getAuthoritiesFromJWT(jwt);
      
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
                    log.warn("Tài khoản {} đã bị khóa hoặc vô hiệu hóa!", username);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
     
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Không thể thiết lập thông tin xác thực vào Security Context: {}", ex.getMessage());
        }


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