package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.entity.Payment;
import com.ridelink.fare_payment_service.service.FareService;
import com.ridelink.fare_payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/payments")
public class PaymentController {

	private final PaymentService paymentService;
	private final FareService fareService;

	public PaymentController(PaymentService paymentService, FareService fareService) {
		this.paymentService = paymentService;
		this.fareService = fareService;
	}

	@PostMapping
	@Operation(summary = "Record a simulated payment", description = "Records a payment for a CONFIRMED fare. Lifecycle: PENDING -> COMPLETED or FAILED. The amount must match the fare and the method must be CASH, CARD or WALLET. Simulated rule: CARD payments above LKR 50,000 are declined (FAILED, no receipt). A receipt is issued automatically on success.")
	public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request) {
		if (request.getFareId() == null || request.getFareId().isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("message", "fareId is required"));
		}
		if (request.getAmount() == null || request.getAmount().signum() <= 0) {
			return ResponseEntity.badRequest().body(Map.of("message", "amount must be greater than 0"));
		}
		if (request.getMethod() == null
				|| !PaymentService.VALID_METHODS.contains(request.getMethod().toUpperCase())) {
			return ResponseEntity.badRequest()
				.body(Map.of("message", "method must be one of CASH, CARD, WALLET"));
		}
		Optional<Fare> found = fareService.findById(request.getFareId());
		if (found.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Fare not found"));
		}
		Fare fare = found.get();
		if (!Fare.STATUS_CONFIRMED.equals(fare.getStatus())) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("message", "Fare must be finalized (CONFIRMED) before payment"));
		}
		if (request.getAmount().compareTo(fare.getAmount()) != 0) {
			return ResponseEntity.badRequest().body(Map.of("message",
				"amount must match the confirmed fare amount of LKR " + fare.getAmount()));
		}
		Payment created = paymentService.processPayment(request, fare);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping
	public List<Payment> getAllPayments() {
		return paymentService.findAll();
	}
}
