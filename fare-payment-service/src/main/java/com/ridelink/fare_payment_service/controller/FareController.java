package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.service.FareService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fares")
public class FareController {

	private final FareService fareService;

	public FareController(FareService fareService) {
		this.fareService = fareService;
	}

	@PostMapping
	public ResponseEntity<Fare> createFare(@RequestBody Fare fare) {
		Fare created = fareService.create(fare);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping
	public List<Fare> getAllFares() {
		return fareService.findAll();
	}
}
