package com.ridelink.ride_management_service.dto;

import com.ridelink.ride_management_service.entity.RideStatus;

public record FareRideResponse(
	String rideId,
	RideStatus status,
	Double distanceKm
) {
}
