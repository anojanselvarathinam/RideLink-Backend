package com.ridelink.account_service.config;

import com.ridelink.account_service.service.JwtService;
import com.ridelink.account_service.entity.AccountStatus;
import com.ridelink.account_service.entity.User;
import com.ridelink.account_service.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).trim();

            try {
                if (jwtService.validateToken(token)) {
                    String email = jwtService.extractEmail(token);
                    User user = userRepository.findByEmail(email).orElse(null);

                    if (user == null) {
                        SecurityContextHolder.clearContext();
                        if ("PATCH".equals(request.getMethod())
                                && (request.getContextPath() + "/api/accounts/me/deactivate")
                                        .equals(request.getRequestURI())) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Account not found\"}");
                            return;
                        }
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Missing or invalid JWT\"}");
                        return;
                    }

                    if (user.getStatus() != AccountStatus.ACTIVE) {
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Account is not active\"}");
                        return;
                    }

                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    email, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException exception) {
                // A token may expire between validation and subject extraction.
                SecurityContextHolder.clearContext();
            }
        }

        // Invalid or missing tokens remain unauthenticated. Public APIs still work.
        filterChain.doFilter(request, response);
    }
}
