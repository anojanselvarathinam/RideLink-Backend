package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.entity.Receipt;
import com.ridelink.fare_payment_service.repository.ReceiptRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ReceiptService {

	private final ReceiptRepository receiptRepository;

	public ReceiptService(ReceiptRepository receiptRepository) {
		this.receiptRepository = receiptRepository;
	}

	public Receipt create(Receipt receipt) {
		if (receipt.getIssuedAt() == null) {
			receipt.setIssuedAt(Instant.now());
		}
		return receiptRepository.save(receipt);
	}

	public List<Receipt> findAll() {
		return receiptRepository.findAll();
	}

	public Optional<Receipt> findById(String id) {
		return receiptRepository.findById(id);
	}

	public List<Receipt> findByPaymentId(String paymentId) {
		return receiptRepository.findByPaymentId(paymentId);
	}

	public Receipt update(Receipt receipt) {
		return receiptRepository.save(receipt);
	}

	public void delete(String id) {
		receiptRepository.deleteById(id);
	}
}
