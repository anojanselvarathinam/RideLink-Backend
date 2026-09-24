package com.ridelink.fare_payment_service.exception;

/**
 * Thrown when the request is well-formed but conflicts with the current
 * state of the resource (e.g. finalizing an already finalized fare,
 * paying a fare that is not CONFIRMED yet).
 * Mapped to HTTP 409 by the GlobalExceptionHandler.
 */
public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}
}
