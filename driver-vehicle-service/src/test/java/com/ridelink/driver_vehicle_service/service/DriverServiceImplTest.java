package com.ridelink.driver_vehicle_service.service;

import com.ridelink.driver_vehicle_service.dto.DriverRequest;
import com.ridelink.driver_vehicle_service.dto.DriverResponse;
import com.ridelink.driver_vehicle_service.entity.AvailabilityStatus;
import com.ridelink.driver_vehicle_service.entity.Driver;
import com.ridelink.driver_vehicle_service.entity.VehicleType;
import com.ridelink.driver_vehicle_service.exception.ResourceNotFoundException;
import com.ridelink.driver_vehicle_service.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverServiceImpl driverService;

    private Driver driver;

    @BeforeEach
    void setUp() {
        driver = new Driver();
        driver.setId("670a1b2c3d4e5f6789012345");
        driver.setUserId(1L);
        driver.setVehicleNumber("WP-CAB-1234");
        driver.setVehicleType(VehicleType.CAR);
        driver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        driver.setServiceArea("Jaffna");
    }

    @Test
    void createDriver_ShouldReturnSavedDriver() {
        DriverRequest request = new DriverRequest();
        request.setUserId(1L);
        request.setVehicleNumber("WP-CAB-1234");
        request.setVehicleType(VehicleType.CAR);
        request.setServiceArea("Jaffna");

        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        DriverResponse response = driverService.createDriver(request);

        assertNotNull(response);
        assertEquals("WP-CAB-1234", response.getVehicleNumber());
        assertEquals(AvailabilityStatus.OFFLINE, response.getAvailabilityStatus());
        verify(driverRepository, times(1)).save(any(Driver.class));
    }

    @Test
    void getDriverById_WhenDriverExists_ShouldReturnDriver() {
        when(driverRepository.findById("670a1b2c3d4e5f6789012345")).thenReturn(Optional.of(driver));

        DriverResponse response = driverService.getDriverById("670a1b2c3d4e5f6789012345");

        assertNotNull(response);
        assertEquals("670a1b2c3d4e5f6789012345", response.getId());
    }

    @Test
    void getDriverById_WhenDriverNotExists_ShouldThrowException() {
        when(driverRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            driverService.getDriverById("nonexistent");
        });
    }

    @Test
    void updateAvailability_ShouldUpdateStatus() {
        when(driverRepository.findById("670a1b2c3d4e5f6789012345")).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        DriverResponse response = driverService.updateAvailability("670a1b2c3d4e5f6789012345", AvailabilityStatus.ONLINE);

        assertNotNull(response);
        verify(driverRepository, times(1)).save(any(Driver.class));
    }

    @Test
    void deleteDriver_WhenDriverNotExists_ShouldThrowException() {
        when(driverRepository.existsById("nonexistent")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            driverService.deleteDriver("nonexistent");
        });
    }
}
