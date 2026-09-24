package com.ridelink.fare_payment_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT (HS256) resource-server security - stateless, no sessions, CSRF off.
 *
 * Token contract (issued by account-service after login; documented here
 * because their implementation is not final yet):
 * <pre>
 * header  { "alg": "HS256", "typ": "JWT" }
 * payload { "sub": "&lt;userId&gt;",
 *           "roles": ["PASSENGER" | "DRIVER" | "ADMIN", ...],
 *           "iat": &lt;epoch-seconds&gt;, "exp": &lt;epoch-seconds&gt; }
 * </pre>
 * The signing secret is read ONLY from the environment variable
 * RIDELINK_JWT_SECRET (at least 32 bytes) and is never stored in the
 * repository (brief 6.3). The service fails fast at startup without it.
 *
 * RBAC rules:
 * <pre>
 * POST /fares/estimate      -> PASSENGER, DRIVER, ADMIN  (quoting a price)
 * POST /fares/{id}/finalize -> DRIVER, ADMIN             (driver/ops confirms the final fare)
 * POST /payments            -> PASSENGER, ADMIN          (the passenger pays)
 * POST /receipts            -> ADMIN                     (manual receipt creation)
 * all other endpoints (reads) -> any authenticated role
 * Swagger UI / OpenAPI docs    -> public (keeps the API discoverable)
 * </pre>
 * Missing/invalid/expired/wrongly-signed tokens -> 401, wrong role -> 403,
 * both with the same uniform error body as the rest of the API
 * (see SecurityErrorHandlers).
 */
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityErrorHandlers securityErrorHandlers)
			throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/fares/estimate")
					.hasAnyRole("PASSENGER", "DRIVER", "ADMIN")
				.requestMatchers(HttpMethod.POST, "/fares/*/finalize")
					.hasAnyRole("DRIVER", "ADMIN")
				.requestMatchers(HttpMethod.POST, "/payments")
					.hasAnyRole("PASSENGER", "ADMIN")
				.requestMatchers(HttpMethod.POST, "/receipts")
					.hasRole("ADMIN")
				.anyRequest().authenticated())
			.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
				.authenticationEntryPoint(securityErrorHandlers)
				.accessDeniedHandler(securityErrorHandlers));
		return http.build();
	}

	/**
	 * Verifies HS256 signatures with the shared secret from the environment.
	 * Expiration (exp) and not-before (iat/nbf) are validated by default.
	 */
	@Bean
	JwtDecoder jwtDecoder(@Value("${RIDELINK_JWT_SECRET}") String secret) {
		byte[] key = secret.getBytes(StandardCharsets.UTF_8);
		if (key.length < 32) {
			throw new IllegalStateException(
				"RIDELINK_JWT_SECRET must be at least 32 bytes for HS256 (got " + key.length + ")");
		}
		return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(key, "HmacSHA256")).build();
	}

	/** Maps the JWT "roles" claim to Spring Security ROLE_* authorities. */
	private JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			List<String> roles = jwt.getClaimAsStringList("roles");
			if (roles == null) {
				return List.of();
			}
			return roles.stream()
				.map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
				.toList();
		});
		return converter;
	}
}
