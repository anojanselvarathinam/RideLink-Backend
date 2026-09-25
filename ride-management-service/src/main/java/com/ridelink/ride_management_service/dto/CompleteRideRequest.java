package com.ridelink.ride_management_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompleteRideRequest(
	@NotNull @Positive Double actualDistance
) {
}
