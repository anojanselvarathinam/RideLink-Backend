package com.ridelink.fare_payment_service.exception;

import com.ridelink.fare_payment_service.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Maps every error to the SAME JSON response shape:
 *
 * <pre>
 * {
 *   "timestamp": "...",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "human readable reason",
 *   "path": "/fares/estimate"
 * }
 * </pre>
 *
 * Services throw typed exceptions (ResourceNotFoundException,
 * ConflictException, ValidationException); this class decides the HTTP status.
 * Framework errors (bean validation, malformed JSON, wrong method, unknown
 * path) are translated into the same shape as well.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private ResponseEntity<ErrorResponse> body(HttpStatus status, String message, HttpServletRequest request) {
		return ResponseEntity.status(status)
			.body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(),
				message, request.getRequestURI()));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
			HttpServletRequest request) {
		return body(HttpStatus.NOT_FOUND, ex.getMessage(), request);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex,
			HttpServletRequest request) {
		return body(HttpStatus.CONFLICT, ex.getMessage(), request);
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ErrorResponse> handleBusinessValidation(ValidationException ex,
			HttpServletRequest request) {
		return body(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		String fields = ex.getBindingResult().getFieldErrors().stream()
			.map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
			.collect(Collectors.joining(", "));
		return body(HttpStatus.BAD_REQUEST,
			fields.isEmpty() ? "Validation failed" : "Validation failed - " + fields, request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleMalformedBody(HttpMessageNotReadableException ex,
			HttpServletRequest request) {
		return body(HttpStatus.BAD_REQUEST, "Malformed JSON request body", request);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleParameterTypeMismatch(
			MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
		return body(HttpStatus.BAD_REQUEST,
			"Invalid value for parameter '" + ex.getName() + "'", request);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupported(
			HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
		return body(HttpStatus.METHOD_NOT_ALLOWED,
			"Method " + ex.getMethod() + " is not supported by this endpoint", request);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleUnknownPath(NoResourceFoundException ex,
			HttpServletRequest request) {
		return body(HttpStatus.NOT_FOUND,
			"No endpoint found for " + request.getRequestURI(), request);
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
			HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
		return body(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
			"Unsupported content type - expected application/json", request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
		log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
		return body(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request);
	}
}
