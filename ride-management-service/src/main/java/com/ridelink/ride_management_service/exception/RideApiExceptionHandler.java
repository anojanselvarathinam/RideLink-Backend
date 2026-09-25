package com.ridelink.ride_management_service.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class RideApiExceptionHandler {

	@ExceptionHandler(RideNotFoundException.class)
	public ResponseEntity<ApiError> handleRideNotFound(RideNotFoundException exception, HttpServletRequest request) {
		return buildError(HttpStatus.NOT_FOUND, "RIDE_NOT_FOUND", exception.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		String message = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> error.getField() + " " + error.getDefaultMessage())
			.collect(Collectors.joining("; "));
		return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
		String message = "Invalid value for parameter '" + exception.getName() + "'";
		return buildError(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message, request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleUnreadableMessage(HttpMessageNotReadableException exception, HttpServletRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Malformed request body", request);
	}

	private ResponseEntity<ApiError> buildError(HttpStatus status, String error, String message, HttpServletRequest request) {
		ApiError body = new ApiError(
			Instant.now(),
			status.value(),
			error,
			message,
			request.getRequestURI()
		);
		return ResponseEntity.status(status).body(body);
	}
}
