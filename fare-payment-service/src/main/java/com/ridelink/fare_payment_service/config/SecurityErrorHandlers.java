package com.ridelink.fare_payment_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Writes security failures with the SAME uniform error body the REST API uses
 * ({timestamp, status, error, message, path} - the ErrorResponse contract):
 *
 * <ul>
 *   <li>401 - missing, malformed, expired or wrongly signed Bearer token
 *       ({@link AuthenticationEntryPoint})</li>
 *   <li>403 - valid token but its role is not allowed for the operation
 *       ({@link AccessDeniedHandler})</li>
 * </ul>
 *
 * These exceptions are raised inside the security filter chain BEFORE the
 * DispatcherServlet, so the @RestControllerAdvice never sees them - handling
 * them here keeps EVERY error response of the service consistent (brief 6.3).
 * The WWW-Authenticate header is added as required by RFC 6750.
 * The body is written directly to avoid coupling to a specific Jackson major
 * version (this project has both Jackson 2 and 3 on the classpath).
 */
@Component
public class SecurityErrorHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		write(response, HttpStatus.UNAUTHORIZED,
			"Authentication required: provide a valid token as Authorization: Bearer <JWT>", request);
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException exception) throws IOException {
		write(response, HttpStatus.FORBIDDEN,
			"Access denied: the token's role does not allow this operation", request);
	}

	private void write(HttpServletResponse response, HttpStatus status, String message,
			HttpServletRequest request) throws IOException {
		String json = "{"
			+ "\"timestamp\":\"" + Instant.now() + "\","
			+ "\"status\":" + status.value() + ","
			+ "\"error\":\"" + status.getReasonPhrase() + "\","
			+ "\"message\":\"" + escape(message) + "\","
			+ "\"path\":\"" + escape(request.getRequestURI()) + "\""
			+ "}";
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.setHeader("WWW-Authenticate", "Bearer");
		response.getWriter().write(json);
	}

	/** Minimal JSON string escaping (messages and paths are service-generated). */
	private String escape(String value) {
		if (value == null) {
			return "";
		}
		return value
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\n", "\\n")
			.replace("\r", "\\r");
	}
}
