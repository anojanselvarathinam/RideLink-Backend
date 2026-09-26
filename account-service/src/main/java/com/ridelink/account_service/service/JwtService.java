package com.ridelink.account_service.service;

import java.util.Date;
import java.util.Base64;
import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ridelink.account_service.entity.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMilliseconds;

    public JwtService(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms}") long expirationMilliseconds) {

        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMilliseconds = expirationMilliseconds;
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .sig().clear().add(Jwts.SIG.HS256).and()
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaims(token).getExpiration();
            return expiration == null || !expiration.after(new Date());
        } catch (ExpiredJwtException exception) {
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            String email = claims.getSubject();
            Date expiration = claims.getExpiration();
            return email != null && !email.isBlank()
                    && claims.get("role") instanceof String role
                    && Set.of("ADMIN", "PASSENGER", "DRIVER").contains(role)
                    && expiration != null && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public String generateToken(User user) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expirationMilliseconds);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("accountId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }
}
