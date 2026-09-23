package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.entity.Receipt;
import com.ridelink.fare_payment_service.service.ReceiptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
