package com.ridelink.fare_payment_service.repository;

import com.ridelink.fare_payment_service.entity.Receipt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReceiptRepository extends MongoRepository<Receipt, String> {

	Optional<Receipt> findByPaymentId(String paymentId);

	List<Receipt> findAllByOrderByIssuedAtDesc();
}
