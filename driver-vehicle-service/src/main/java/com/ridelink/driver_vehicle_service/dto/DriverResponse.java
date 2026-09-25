package com.ridelink.driver_vehicle_service.dto;

import com.ridelink.driver_vehicle_service.entity.AvailabilityStatus;
import com.ridelink.driver_vehicle_service.entity.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {

    private String id;
    private Long userId;
    private String vehicleNumber;
    private VehicleType vehicleType;
    private AvailabilityStatus availabilityStatus;
    private Double currentLat;
    private Double currentLng;
    private String serviceArea;
}
