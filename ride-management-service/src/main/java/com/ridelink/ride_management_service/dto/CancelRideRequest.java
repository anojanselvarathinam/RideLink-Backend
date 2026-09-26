package com.ridelink.ride_management_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelRideRequest(
	@NotBlank @Size(max = 500) String reason
) {
}
