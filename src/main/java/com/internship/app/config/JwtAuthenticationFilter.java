package com.internship.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final com.internship.app.repository.UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ✅ SKIP AUTH ENDPOINTS
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/")) {
            log.debug("Skipping JWT filter for auth endpoint: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        log.debug("Auth header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid Bearer token found");
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = authHeader.substring(7);
        log.debug("JWT Token length: {}", jwtToken.length());

        try {
            // ✅ CRITICAL: Validate token FIRST
            if (!jwtService.isTokenValid(jwtToken)) {
                log.warn("Invalid JWT token");
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtService.extractEmail(jwtToken);
            log.debug("Extracted email from JWT: '{}'", email);

            // ✅ FIXED: Null check + User exists check
            if (email != null && !email.trim().isEmpty()) {
                var user = userRepository.findByEmail(email).orElse(null);
                log.debug("User found for email '{}': {}", email, user != null ? "YES" : "NO");

                if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                    var authToken = new UsernamePasswordAuthenticationToken(
                            user, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("✅ Authentication set for user: {}", email);
                }
            } else {
                log.warn("JWT email is null or empty");
            }

        } catch (Exception e) {
            log.error("JWT processing failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
