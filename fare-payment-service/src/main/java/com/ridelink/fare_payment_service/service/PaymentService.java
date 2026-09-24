package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.entity.Payment;
import com.ridelink.fare_payment_service.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PaymentService {

	public static final Set<String> VALID_METHODS = Set.of("CASH", "CARD", "WALLET");

	private final PaymentRepository paymentRepository;
	private final PaymentSimulator paymentSimulator;
	private final ReceiptService receiptService;

	public PaymentService(PaymentRepository paymentRepository, PaymentSimulator paymentSimulator,
			ReceiptService receiptService) {
		this.paymentRepository = paymentRepository;
		this.paymentSimulator = paymentSimulator;
		this.receiptService = receiptService;
	}

	/**
	 * Records a simulated payment for a confirmed fare.
	 *
	 * Lifecycle: PENDING -> COMPLETED (gateway approved, receipt issued)
	 *         or PENDING -> FAILED    (gateway declined, no receipt)
	 */
	public Payment processPayment(PaymentRequest request, Fare fare) {
		Payment payment = new Payment();
		payment.setFareId(fare.getId());
		payment.setAmount(request.getAmount());
		payment.setMethod(request.getMethod().toUpperCase());
		payment.setStatus(Payment.STATUS_PENDING);
		payment = paymentRepository.save(payment);

		boolean approved = paymentSimulator.authorize(payment.getAmount(), payment.getMethod());
		if (approved) {
			payment.setStatus(Payment.STATUS_COMPLETED);
			payment = paymentRepository.save(payment);
			receiptService.issue(payment, fare);
		} else {
			payment.setStatus(Payment.STATUS_FAILED);
			payment = paymentRepository.save(payment);
		}
		return payment;
	}

	public List<Payment> findAll() {
		return paymentRepository.findAll();
	}

	public Optional<Payment> findById(String id) {
		return paymentRepository.findById(id);
	}

	public List<Payment> findByFareId(String fareId) {
		return paymentRepository.findByFareId(fareId);
	}

	public Payment update(Payment payment) {
		return paymentRepository.save(payment);
	}

	public void delete(String id) {
		paymentRepository.deleteById(id);
	}
}
