package com.ridelink.ride_management_service.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignDriverRequest(
	@NotBlank String driverId
) {
}
