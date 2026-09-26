package com.ridelink.account_service;

import com.ridelink.account_service.entity.Role;
import com.ridelink.account_service.entity.User;
import com.ridelink.account_service.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class JwtContractTests {
    @Test
    void actualAccountIssuerProducesCrossServiceContractFixtures() throws Exception {
        // Test-only random key. Fixtures stay in Maven's ignored target directory and are never logged.
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        String secret = Base64.getEncoder().encodeToString(key);
        JwtService issuer = new JwtService(secret, 3600000);
        Properties fixtures = new Properties();
        fixtures.setProperty("JWT_SECRET", secret);
        User user = new User();
        user.setId("account-1");
        user.setEmail("contract@example.test");
        for (Role role : Role.values()) {
            user.setRole(role);
            String token = issuer.generateToken(user);
            assertTrue(issuer.validateToken(token));
            assertEquals(role.name(), issuer.extractRole(token));
            assertEquals("HS256", Jwts.parser().verifyWith(Keys.hmacShaKeyFor(key)).build()
                    .parseSignedClaims(token).getHeader().getAlgorithm());
            fixtures.setProperty(role.name(), token);
        }
        user.setRole(Role.ADMIN);
        fixtures.setProperty("expired", new JwtService(secret, -120000).generateToken(user));
        byte[] otherKey = new byte[64];
        new SecureRandom().nextBytes(otherKey);
        fixtures.setProperty("wrongSignature", new JwtService(Base64.getEncoder().encodeToString(otherKey), 3600000).generateToken(user));
        fixtures.setProperty("wrongAlgorithm", Jwts.builder().subject(user.getEmail()).claim("role", "ADMIN")
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(key), Jwts.SIG.HS512).compact());
        fixtures.setProperty("missingExpiry", Jwts.builder().subject(user.getEmail()).claim("role", "ADMIN")
                .signWith(Keys.hmacShaKeyFor(key), Jwts.SIG.HS256).compact());
        fixtures.setProperty("oldRolesClaim", Jwts.builder().subject(user.getEmail()).claim("roles", java.util.List.of("ADMIN"))
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(key), Jwts.SIG.HS256).compact());
        for (String invalid : new String[]{"expired", "wrongSignature", "wrongAlgorithm", "missingExpiry", "oldRolesClaim"}) {
            assertFalse(issuer.validateToken(fixtures.getProperty(invalid)), invalid);
        }
        try (var output = Files.newOutputStream(Path.of("target", "jwt-contract.properties"))) {
            fixtures.store(output, "Ephemeral test fixtures from the real Account JwtService; never deploy or commit");
        }
    }
}
