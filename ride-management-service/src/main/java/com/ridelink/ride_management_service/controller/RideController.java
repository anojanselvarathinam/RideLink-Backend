package com.ridelink.ride_management_service.controller;

import java.net.URI;
import java.util.List;

import com.ridelink.ride_management_service.dto.AssignDriverRequest;
import com.ridelink.ride_management_service.dto.CancelRideRequest;
import com.ridelink.ride_management_service.dto.CompleteRideRequest;
import com.ridelink.ride_management_service.dto.CreateRideRequest;
import com.ridelink.ride_management_service.dto.RideResponse;
import com.ridelink.ride_management_service.entity.RideStatus;
import com.ridelink.ride_management_service.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rides")
@Tag(name = "Ride Management", description = "Ride request, retrieval, assignment, lifecycle, and cancellation APIs")
public class RideController {

	private final RideService rideService;

	public RideController(RideService rideService) {
		this.rideService = rideService;
	}

	@Operation(summary = "Create a ride request", description = "Creates a new ride request in REQUESTED status.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "Ride request created"),
		@ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
	})
	@PostMapping
	public ResponseEntity<RideResponse> createRide(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Ride request details",
			required = true,
			content = @Content(schema = @Schema(implementation = CreateRideRequest.class))
		)
		@Valid @RequestBody CreateRideRequest request
	) {
		RideResponse response = rideService.createRide(request);
		return ResponseEntity.created(URI.create("/api/rides/" + response.id())).body(response);
	}

	@Operation(summary = "Get a ride by ID", description = "Retrieves a single ride by its Ride Management identifier.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Ride found"),
		@ApiResponse(responseCode = "404", description = "Ride not found", content = @Content)
	})
	@GetMapping("/{rideId}")
	public ResponseEntity<RideResponse> getRideById(
		@Parameter(description = "Ride ID", required = true)
		@PathVariable String rideId
	) {
		return ResponseEntity.ok(rideService.getRideById(rideId));
	}

	@Operation(summary = "List rides", description = "Retrieves rides, optionally filtered by passenger, driver, or status.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Rides retrieved"),
		@ApiResponse(responseCode = "400", description = "Invalid filter value", content = @Content)
	})
	@GetMapping
	public ResponseEntity<List<RideResponse>> getRides(
		@Parameter(description = "Passenger ID filter")
		@RequestParam(required = false) String passengerId,
		@Parameter(description = "Driver ID filter")
		@RequestParam(required = false) String driverId,
		@Parameter(description = "Ride status filter")
		@RequestParam(required = false) RideStatus status
	) {
		return ResponseEntity.ok(rideService.getRides(passengerId, driverId, status));
	}

	@Operation(summary = "Assign a driver", description = "Assigns a driver to a requested ride.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Driver assigned"),
		@ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
		@ApiResponse(responseCode = "404", description = "Ride not found", content = @Content),
		@ApiResponse(responseCode = "409", description = "Invalid ride state transition", content = @Content)
	})
	@PatchMapping("/{rideId}/assign-driver")
	public ResponseEntity<RideResponse> assignDriver(
		@Parameter(description = "Ride ID", required = true)
		@PathVariable String rideId,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Driver assignment request",
			required = true,
			content = @Content(schema = @Schema(implementation = AssignDriverRequest.class))
		)
		@Valid @RequestBody AssignDriverRequest request
	) {
		return ResponseEntity.ok(rideService.assignDriver(rideId, request));
	}

	@Operation(summary = "Accept an assigned ride", description = "Moves an assigned ride to ACCEPTED status.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Ride accepted"),
		@ApiResponse(responseCode = "404", description = "Ride not found", content = @Content),
		@ApiResponse(responseCode = "409", description = "Invalid ride state transition", content = @Content)
	})
	@PatchMapping("/{rideId}/accept")
	public ResponseEntity<RideResponse> acceptRide(
		@Parameter(description = "Ride ID", required = true)
		@PathVariable String rideId
	) {
		return ResponseEntity.ok(rideService.acceptRide(rideId));
	}

	@Operation(summary = "Start an accepted ride", description = "Moves an accepted ride to IN_PROGRESS status.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Ride started"),
		@ApiResponse(responseCode = "404", description = "Ride not found", content = @Content),
		@ApiResponse(responseCode = "409", description = "Invalid ride state transition", content = @Content)
	})
	@PatchMapping("/{rideId}/start")
	public ResponseEntity<RideResponse> startRide(
		@Parameter(description = "Ride ID", required = true)
		@PathVariable String rideId
	) {
		return ResponseEntity.ok(rideService.startRide(rideId));
	}

	@Operation(summary = "Complete an in-progress ride", description = "Completes an in-progress ride and stores the actual distance.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Ride completed"),
		@ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
		@ApiResponse(responseCode = "404", description = "Ride not found", content = @Content),
		@ApiResponse(responseCode = "409", description = "Invalid ride state transition", content = @Content)
	})
	@PatchMapping("/{rideId}/complete")
	public ResponseEntity<RideResponse> completeRide(
		@Parameter(description = "Ride ID", required = true)
		@PathVariable String rideId,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Completion details",
			required = true,
			content = @Content(schema = @Schema(implementation = CompleteRideRequest.class))
		)
		@Valid @RequestBody CompleteRideRequest request
	) {
		return ResponseEntity.ok(rideService.completeRide(rideId, request));
	}

	@Operation(summary = "Cancel a ride", description = "Cancels a ride when the current lifecycle state allows cancellation.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Ride cancelled"),
		@ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
		@ApiResponse(responseCode = "404", description = "Ride not found", content = @Content),
		@ApiResponse(responseCode = "409", description = "Invalid ride state transition", content = @Content)
	})
	@PatchMapping("/{rideId}/cancel")
	public ResponseEntity<RideResponse> cancelRide(
		@Parameter(description = "Ride ID", required = true)
		@PathVariable String rideId,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "Cancellation reason",
			required = true,
			content = @Content(schema = @Schema(implementation = CancelRideRequest.class))
		)
		@Valid @RequestBody CancelRideRequest request
	) {
		return ResponseEntity.ok(rideService.cancelRide(rideId, request));
	}
}
