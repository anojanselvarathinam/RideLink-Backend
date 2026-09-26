package com.ridelink.ride_management_service.controller;

import com.ridelink.ride_management_service.dto.FareRideResponse;
import com.ridelink.ride_management_service.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Fare & Payment Compatibility", description = "Temporary interservice compatibility endpoints")
public class FareRideCompatibilityController {

	private final RideService rideService;

	public FareRideCompatibilityController(RideService rideService) {
		this.rideService = rideService;
	}

	// This compatibility endpoint exists for the Fare & Payment Service contract.
	// The standard public Ride Management API remains under /api/rides.
	@Operation(
		summary = "Get ride projection for Fare & Payment Service",
		description = "Interservice compatibility endpoint that exposes only ride ID, status, and distance for fare finalization."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Ride projection found"),
		@ApiResponse(responseCode = "404", description = "Ride not found", content = @Content)
	})
	@GetMapping("/rides/{rideId}")
	public ResponseEntity<FareRideResponse> getFareRideById(
		@Parameter(description = "Ride ID", required = true)
		@PathVariable String rideId
	) {
		return ResponseEntity.ok(rideService.getFareRideById(rideId));
	}
}
