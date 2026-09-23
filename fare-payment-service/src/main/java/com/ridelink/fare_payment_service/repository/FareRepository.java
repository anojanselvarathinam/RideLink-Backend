package com.ridelink.fare_payment_service.repository;

import com.ridelink.fare_payment_service.entity.Fare;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FareRepository extends MongoRepository<Fare, String> {

	List<Fare> findByRideId(String rideId);
}
