package com.ridelink.fare_payment_service.exception;

/**
 * Thrown when input breaks a business rule that field-level validation
 * cannot express (e.g. payment amount does not match the fare).
 * Mapped to HTTP 400 by the GlobalExceptionHandler.
 */
public class ValidationException extends RuntimeException {

	public ValidationException(String message) {
		super(message);
	}
}
