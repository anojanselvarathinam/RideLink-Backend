package com.ridelink.driver_vehicle_service.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "drivers")
@Data
public class Driver {

    @Id
    private String id;

    private Long userId;

    private String vehicleNumber;

    private VehicleType vehicleType;

    private AvailabilityStatus availabilityStatus = AvailabilityStatus.OFFLINE;

    private Double currentLat;

    private Double currentLng;

    private String serviceArea;
}
