package com.ridelink.fare_payment_service.exception;

/**
 * Thrown when a requested resource (fare, payment, receipt) does not exist.
 * Mapped to HTTP 404 by the GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
