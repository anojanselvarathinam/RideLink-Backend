package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.entity.Payment;
import com.ridelink.fare_payment_service.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

	private final PaymentRepository paymentRepository;

	public PaymentService(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	public Payment create(Payment payment) {
		return paymentRepository.save(payment);
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
