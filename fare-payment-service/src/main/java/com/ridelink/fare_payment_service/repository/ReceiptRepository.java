package com.ridelink.fare_payment_service.repository;

import com.ridelink.fare_payment_service.entity.Receipt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReceiptRepository extends MongoRepository<Receipt, String> {

	List<Receipt> findByPaymentId(String paymentId);
}
