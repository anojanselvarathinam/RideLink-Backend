package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.entity.Payment;
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

	/** Generates a receipt automatically for an approved (COMPLETED) payment. No manual creation endpoint exists (receipts cannot be forged). */
	public Receipt issue(Payment payment, Fare fare) {
		Receipt receipt = new Receipt();
		receipt.setPaymentId(payment.getId());
		receipt.setIssuedAt(Instant.now());
		receipt.setDetails(String.format("Ride %s | %s -> %s | %s payment of LKR %s",
			fare.getRideId(), fare.getPickupLocation(), fare.getDestinationLocation(),
			payment.getMethod(), payment.getAmount()));
		return receiptRepository.save(receipt);
	}

	public List<Receipt> findAll() {
		return receiptRepository.findAll();
	}

	public Optional<Receipt> findById(String id) {
		return receiptRepository.findById(id);
	}

	public Optional<Receipt> findByPaymentId(String paymentId) {
		return receiptRepository.findByPaymentId(paymentId);
	}

	public Receipt update(Receipt receipt) {
		return receiptRepository.save(receipt);
	}

	public void delete(String id) {
		receiptRepository.deleteById(id);
	}
}
