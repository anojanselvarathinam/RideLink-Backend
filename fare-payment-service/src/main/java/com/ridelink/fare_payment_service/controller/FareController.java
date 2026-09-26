package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.FareEstimateRequest;
import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.exception.ResourceNotFoundException;
import com.ridelink.fare_payment_service.service.FareService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fares")
public class FareController {

	private final FareService fareService;

	public FareController(FareService fareService) {
		this.fareService = fareService;
	}

	@PostMapping("/estimate")
	@Operation(summary = "Estimate a fare", description = "Calculates the fare using the documented rule: LKR 300 base fare + LKR 120 per km. The amount is never taken from the client. Status starts as ESTIMATED. Validation errors return HTTP 400 with the standard error body.")
	public ResponseEntity<Fare> estimateFare(@Valid @RequestBody FareEstimateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(fareService.estimate(request));
	}

	@PostMapping("/{id}/finalize")
	@Operation(summary = "Finalize a fare", description = "Sets the final distance (precedence: distanceKm query param, then the ACTUAL distance fetched from ride-management via synchronous REST by rideId, then the estimated distance as fallback) and recalculates the amount with the documented rule. The fare's distanceSource field records which source was used (REQUEST / RIDE_MANAGEMENT / ESTIMATE). Returns 404 if the fare does not exist and 409 if it was already finalized - both with the standard error body.")
	public ResponseEntity<Fare> finalizeFare(@PathVariable String id,
			@RequestParam(name = "distanceKm", required = false) Double actualDistanceKm) {
		return ResponseEntity.ok(fareService.finalizeFare(id, actualDistanceKm));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a fare by id", description = "Returns 404 with the standard error body if the fare does not exist.")
	public ResponseEntity<Fare> getFareById(@PathVariable String id) {
		Fare fare = fareService.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Fare not found: " + id));
		return ResponseEntity.ok(fare);
	}

	@GetMapping("/ride/{rideId}")
	@Operation(summary = "Get all fares of a ride", description = "Used by other services (e.g. ride-management) to fetch the fares of a ride. Returns an empty list when the ride has no fares.")
	public List<Fare> getFaresByRideId(@PathVariable String rideId) {
		return fareService.findByRideId(rideId);
	}

	@GetMapping
	public List<Fare> getAllFares() {
		return fareService.findAll();
	}
}
