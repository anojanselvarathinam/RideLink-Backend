package com.ridelink.driver_vehicle_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationUpdateRequest {

    @NotNull(message = "Latitude is required")
    private Double currentLat;

    @NotNull(message = "Longitude is required")
    private Double currentLng;
}
