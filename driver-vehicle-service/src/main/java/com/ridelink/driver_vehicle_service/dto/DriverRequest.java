package com.ridelink.driver_vehicle_service.dto;

import com.ridelink.driver_vehicle_service.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    private String serviceArea;
}
