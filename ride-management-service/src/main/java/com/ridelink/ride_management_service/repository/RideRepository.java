package com.ridelink.ride_management_service.repository;

import java.util.List;

import com.ridelink.ride_management_service.entity.Ride;
import com.ridelink.ride_management_service.entity.RideStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RideRepository extends MongoRepository<Ride, String> {

	List<Ride> findByPassengerId(String passengerId);

	List<Ride> findByDriverId(String driverId);

	List<Ride> findByStatus(RideStatus status);
}
