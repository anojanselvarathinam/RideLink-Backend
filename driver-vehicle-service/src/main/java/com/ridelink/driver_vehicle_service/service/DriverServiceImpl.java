package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.dto.DriverRequest;
import com.ridelink.driver_vehicle_service.dto.DriverResponse;
import com.ridelink.driver_vehicle_service.dto.LocationUpdateRequest;
import com.ridelink.driver_vehicle_service.entity.AvailabilityStatus;
import com.ridelink.driver_vehicle_service.entity.Driver;
import com.ridelink.driver_vehicle_service.exception.ResourceNotFoundException;
import com.ridelink.driver_vehicle_service.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;

    @Override
    public DriverResponse createDriver(DriverRequest request) {
        Driver driver = new Driver();
        driver.setUserId(request.getUserId());
        driver.setVehicleNumber(request.getVehicleNumber());
        driver.setVehicleType(request.getVehicleType());
        driver.setServiceArea(request.getServiceArea());
        driver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);

        Driver saved = driverRepository.save(driver);
        return mapToResponse(saved);
    }

    @Override
    public DriverResponse getDriverById(String id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
        return mapToResponse(driver);
    }

    @Override
    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DriverResponse updateAvailability(String id, AvailabilityStatus status) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
        driver.setAvailabilityStatus(status);
        Driver updated = driverRepository.save(driver);
        return mapToResponse(updated);
    }

    @Override
    public DriverResponse updateLocation(String id, LocationUpdateRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
        driver.setCurrentLat(request.getCurrentLat());
        driver.setCurrentLng(request.getCurrentLng());
        Driver updated = driverRepository.save(driver);
        return mapToResponse(updated);
    }

    @Override
    public List<DriverResponse> getAvailableDrivers(String serviceArea) {
        return driverRepository.findByServiceAreaAndAvailabilityStatus(serviceArea, AvailabilityStatus.ONLINE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDriver(String id) {
        if (!driverRepository.existsById(id)) {
            throw new ResourceNotFoundException("Driver not found with id: " + id);
        }
        driverRepository.deleteById(id);
    }

    private DriverResponse mapToResponse(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getUserId(),
                driver.getVehicleNumber(),
                driver.getVehicleType(),
                driver.getAvailabilityStatus(),
                driver.getCurrentLat(),
                driver.getCurrentLng(),
                driver.getServiceArea()
        );
    }
}
