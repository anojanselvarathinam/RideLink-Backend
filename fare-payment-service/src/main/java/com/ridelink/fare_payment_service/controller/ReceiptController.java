package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.entity.Receipt;
import com.ridelink.fare_payment_service.exception.ResourceNotFoundException;
import com.ridelink.fare_payment_service.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/receipts")
public class ReceiptController {

	private final ReceiptService receiptService;

	public ReceiptController(ReceiptService receiptService) {
		this.receiptService = receiptService;
	}

	@PostMapping
	public ResponseEntity<Receipt> createReceipt(@RequestBody Receipt receipt) {
		Receipt created = receiptService.create(receipt);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping
	public List<Receipt> getAllReceipts() {
		return receiptService.findAll();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a receipt by id", description = "Returns 404 with the standard error body if the receipt does not exist.")
	public ResponseEntity<Receipt> getReceiptById(@PathVariable String id) {
		Receipt receipt = receiptService.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Receipt not found: " + id));
		return ResponseEntity.ok(receipt);
	}

	@GetMapping("/payment/{paymentId}")
	@Operation(summary = "Get the receipt of a payment", description = "Returns the automatically generated receipt for the given payment id. Returns 404 with the standard error body if the payment has no receipt (e.g. the payment FAILED).")
	public ResponseEntity<Receipt> getReceiptByPaymentId(@PathVariable String paymentId) {
		Receipt receipt = receiptService.findByPaymentId(paymentId)
			.orElseThrow(() -> new ResourceNotFoundException(
				"No receipt for payment: " + paymentId));
		return ResponseEntity.ok(receipt);
	}
}
