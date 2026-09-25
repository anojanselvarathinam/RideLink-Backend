package com.ridelink.driver_vehicle_service.repository;

import com.ridelink.driver_vehicle_service.entity.AvailabilityStatus;
import com.ridelink.driver_vehicle_service.entity.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DriverRepository extends MongoRepository<Driver, String> {

    List<Driver> findByAvailabilityStatus(AvailabilityStatus status);

    List<Driver> findByServiceAreaAndAvailabilityStatus(String serviceArea, AvailabilityStatus status);

    Driver findByUserId(Long userId);
}
