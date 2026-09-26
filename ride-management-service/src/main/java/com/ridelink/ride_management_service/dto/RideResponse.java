package com.ridelink.ride_management_service.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.ridelink.ride_management_service.entity.RideStatus;

public record RideResponse(
	String id,
	String passengerId,
	String driverId,
	LocationResponse pickup,
	LocationResponse destination,
	RideStatus status,
	BigDecimal estimatedFare,
	BigDecimal finalFare,
	Double actualDistance,
	String cancellationReason,
	Instant requestedAt,
	Instant assignedAt,
	Instant acceptedAt,
	Instant startedAt,
	Instant completedAt,
	Instant cancelledAt,
	Instant createdAt,
	Instant updatedAt
) {
}
