package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.dto.DriverRequest;
import com.ridelink.driver_vehicle_service.dto.DriverResponse;
import com.ridelink.driver_vehicle_service.dto.LocationUpdateRequest;
import com.ridelink.driver_vehicle_service.entity.AvailabilityStatus;

import java.util.List;

public interface DriverService {

    DriverResponse createDriver(DriverRequest request);

    DriverResponse getDriverById(String id);

    List<DriverResponse> getAllDrivers();

    DriverResponse updateAvailability(String id, AvailabilityStatus status);

    DriverResponse updateLocation(String id, LocationUpdateRequest request);

    List<DriverResponse> getAvailableDrivers(String serviceArea);

    void deleteDriver(String id);
}
