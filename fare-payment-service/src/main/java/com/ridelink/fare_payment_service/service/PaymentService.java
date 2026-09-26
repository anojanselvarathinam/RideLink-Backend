package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.entity.Payment;
import com.ridelink.fare_payment_service.exception.ConflictException;
import com.ridelink.fare_payment_service.exception.ResourceNotFoundException;
import com.ridelink.fare_payment_service.exception.ValidationException;
import com.ridelink.fare_payment_service.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final PaymentSimulator paymentSimulator;
	private final ReceiptService receiptService;
	private final FareService fareService;

	public PaymentService(PaymentRepository paymentRepository, PaymentSimulator paymentSimulator,
			ReceiptService receiptService, FareService fareService) {
		this.paymentRepository = paymentRepository;
		this.paymentSimulator = paymentSimulator;
		this.receiptService = receiptService;
		this.fareService = fareService;
	}

	/**
	 * Records a simulated payment for a confirmed fare.
	 *
	 * Lifecycle: PENDING -> COMPLETED (gateway approved, receipt issued)
	 *         or PENDING -> FAILED    (gateway declined, no receipt)
	 *
	 * (fareId/amount/method field validation happens in the controller via @Valid.)
	 *
	 * @throws ResourceNotFoundException fare does not exist
	 * @throws ConflictException         fare is not CONFIRMED yet
	 * @throws ValidationException       amount does not match the fare
	 */
	public Payment processPayment(PaymentRequest request) {
		Fare fare = fareService.findById(request.getFareId())
			.orElseThrow(() -> new ResourceNotFoundException(
				"Fare not found: " + request.getFareId()));
		if (!Fare.STATUS_CONFIRMED.equals(fare.getStatus())) {
			throw new ConflictException("Fare must be finalized (CONFIRMED) before payment");
		}
		if (request.getAmount().compareTo(fare.getAmount()) != 0) {
			throw new ValidationException(
				"amount must match the confirmed fare amount of LKR " + fare.getAmount());
		}

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
