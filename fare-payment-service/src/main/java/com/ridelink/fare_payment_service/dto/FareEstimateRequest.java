package com.ridelink.fare_payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload for POST /fares/estimate.
 * Validated with bean validation annotations (@Valid in the controller);
 * the calculated amount and status are never accepted from the client.
 */
public class FareEstimateRequest {

	@NotBlank(message = "rideId is required")
	private String rideId;

	@NotBlank(message = "pickupLocation is required")
	private String pickupLocation;

	@NotBlank(message = "destinationLocation is required")
	private String destinationLocation;

	@NotNull(message = "distanceKm is required")
	@Positive(message = "distanceKm must be greater than 0")
	private Double distanceKm;

	public FareEstimateRequest() {
	}

	public String getRideId() {
		return rideId;
	}

	public void setRideId(String rideId) {
		this.rideId = rideId;
	}

	public String getPickupLocation() {
		return pickupLocation;
	}

	public void setPickupLocation(String pickupLocation) {
		this.pickupLocation = pickupLocation;
	}

	public String getDestinationLocation() {
		return destinationLocation;
	}

	public void setDestinationLocation(String destinationLocation) {
		this.destinationLocation = destinationLocation;
	}

	public Double getDistanceKm() {
		return distanceKm;
	}

	public void setDistanceKm(Double distanceKm) {
		this.distanceKm = distanceKm;
	}
}
