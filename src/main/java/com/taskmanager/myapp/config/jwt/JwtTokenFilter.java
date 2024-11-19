package com.taskmanager.myapp.config.jwt;

import com.taskmanager.myapp.config.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String authorizationHeader = request.getHeader("Authorization");

        log.info(">>> Jwt Token Filter <<<");
        log.info(">>> Access URI : {} <<<", requestURI);

        String employeeNumber = null;
        String accessToken = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            log.info(">>> Have Access Token <<<");

            accessToken = authorizationHeader.substring(7);
            employeeNumber = jwtTokenUtil.extractEmployeeNumber(accessToken);
        }

        if (accessToken != null && isTokenBlacklisted(accessToken)) {
            log.warn(">>> Token is blacklisted!!! <<<");

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Don't have access authority");
            return;
        }

        if (employeeNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(employeeNumber);

            if (jwtTokenUtil.validateToken(accessToken, employeeNumber)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTokenBlacklisted(String accessToken) {
        try {
            String hashedAccessToken = jwtTokenUtil.tokenToHash(accessToken);

            return redisTemplate.opsForValue().get(hashedAccessToken) != null;
        } catch (Exception e) {
            log.error("Error Generate Access Token Hash");

            return false;
        }
    }
}
