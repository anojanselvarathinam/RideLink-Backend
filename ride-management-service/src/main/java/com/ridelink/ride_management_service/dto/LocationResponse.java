package com.ridelink.ride_management_service.dto;

public record LocationResponse(
	String placeName,
	Double latitude,
	Double longitude
) {
}
