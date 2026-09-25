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
public class RideController {

	private final RideService rideService;

	public RideController(RideService rideService) {
		this.rideService = rideService;
	}

	@PostMapping
	public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request) {
		RideResponse response = rideService.createRide(request);
		return ResponseEntity.created(URI.create("/api/rides/" + response.id())).body(response);
	}

	@GetMapping("/{rideId}")
	public ResponseEntity<RideResponse> getRideById(@PathVariable String rideId) {
		return ResponseEntity.ok(rideService.getRideById(rideId));
	}

	@GetMapping
	public ResponseEntity<List<RideResponse>> getRides(
		@RequestParam(required = false) String passengerId,
		@RequestParam(required = false) String driverId,
		@RequestParam(required = false) RideStatus status
	) {
		return ResponseEntity.ok(rideService.getRides(passengerId, driverId, status));
	}

	@PatchMapping("/{rideId}/assign-driver")
	public ResponseEntity<RideResponse> assignDriver(
		@PathVariable String rideId,
		@Valid @RequestBody AssignDriverRequest request
	) {
		return ResponseEntity.ok(rideService.assignDriver(rideId, request));
	}

	@PatchMapping("/{rideId}/accept")
	public ResponseEntity<RideResponse> acceptRide(@PathVariable String rideId) {
		return ResponseEntity.ok(rideService.acceptRide(rideId));
	}

	@PatchMapping("/{rideId}/start")
	public ResponseEntity<RideResponse> startRide(@PathVariable String rideId) {
		return ResponseEntity.ok(rideService.startRide(rideId));
	}

	@PatchMapping("/{rideId}/complete")
	public ResponseEntity<RideResponse> completeRide(
		@PathVariable String rideId,
		@Valid @RequestBody CompleteRideRequest request
	) {
		return ResponseEntity.ok(rideService.completeRide(rideId, request));
	}

	@PatchMapping("/{rideId}/cancel")
	public ResponseEntity<RideResponse> cancelRide(
		@PathVariable String rideId,
		@Valid @RequestBody CancelRideRequest request
	) {
		return ResponseEntity.ok(rideService.cancelRide(rideId, request));
	}
}
