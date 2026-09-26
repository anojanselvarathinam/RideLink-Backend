package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.entity.Payment;
import com.ridelink.fare_payment_service.exception.ResourceNotFoundException;
import com.ridelink.fare_payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping
	@Operation(summary = "Record a simulated payment", description = "Records a payment for a CONFIRMED fare. Lifecycle: PENDING -> COMPLETED or FAILED. Validation: fare must exist (404) and be CONFIRMED (409), amount must match the fare (400), method must be CASH, CARD or WALLET (400). Simulated rule: CARD payments above LKR 50,000 are declined (FAILED, no receipt). A receipt is issued automatically on success. All errors use the standard error body.")
	public ResponseEntity<Payment> createPayment(@Valid @RequestBody PaymentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(request));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a payment by id", description = "Returns 404 with the standard error body if the payment does not exist.")
	public ResponseEntity<Payment> getPaymentById(@PathVariable String id) {
		Payment payment = paymentService.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
		return ResponseEntity.ok(payment);
	}

	@GetMapping("/fare/{fareId}")
	@Operation(summary = "Get all payments of a fare", description = "Returns an empty list when the fare has no payments.")
	public List<Payment> getPaymentsByFareId(@PathVariable String fareId) {
		return paymentService.findByFareId(fareId);
	}

	@GetMapping
	public List<Payment> getAllPayments() {
		return paymentService.findAll();
	}
}
