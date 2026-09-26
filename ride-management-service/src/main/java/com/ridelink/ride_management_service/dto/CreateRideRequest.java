package com.ridelink.ride_management_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRideRequest(
	@NotBlank String passengerId,
	@NotNull @Valid LocationRequest pickup,
	@NotNull @Valid LocationRequest destination
) {
}
