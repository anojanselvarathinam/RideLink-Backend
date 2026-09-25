package com.ridelink.ride_management_service.exception;

public class InvalidRideStateException extends RuntimeException {

	public InvalidRideStateException(String message) {
		super(message);
	}
}
