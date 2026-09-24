package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.FareEstimateRequest;
import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.service.FareService;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/fares")
public class FareController {

	private final FareService fareService;

	public FareController(FareService fareService) {
		this.fareService = fareService;
	}

	@PostMapping("/estimate")
	@Operation(summary = "Estimate a fare", description = "Calculates the fare using the documented rule: LKR 300 base fare + LKR 120 per km. The amount is never taken from the client. Status starts as ESTIMATED.")
	public ResponseEntity<?> estimateFare(@RequestBody FareEstimateRequest request) {
		if (request.getDistanceKm() == null || request.getDistanceKm() <= 0) {
			return ResponseEntity.badRequest()
				.body(Map.of("message", "distanceKm must be provided and greater than 0"));
		}
		Fare created = fareService.estimate(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PostMapping("/{id}/finalize")
	@Operation(summary = "Finalize a fare", description = "Recalculates the fare with the actual distance (optional query param distanceKm) and sets status to CONFIRMED. Returns 404 if the fare does not exist and 409 if it was already finalized.")
	public ResponseEntity<?> finalizeFare(@PathVariable String id,
			@RequestParam(name = "distanceKm", required = false) Double actualDistanceKm) {
		Optional<Fare> found = fareService.findById(id);
		if (found.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Fare fare = found.get();
		if (!Fare.STATUS_ESTIMATED.equals(fare.getStatus())) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("message", "Fare has already been finalized"));
		}
		Double distance = actualDistanceKm != null ? actualDistanceKm : fare.getDistanceKm();
		if (distance == null || distance <= 0) {
			return ResponseEntity.badRequest()
				.body(Map.of("message", "distanceKm must be provided and greater than 0"));
		}
		return ResponseEntity.ok(fareService.finalizeFare(fare, distance));
	}

	@GetMapping
	public List<Fare> getAllFares() {
		return fareService.findAll();
	}
}
