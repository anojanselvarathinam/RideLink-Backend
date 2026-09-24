package com.ridelink.fare_payment_service.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "fares")
public class Fare {

	public static final String STATUS_ESTIMATED = "ESTIMATED";
	public static final String STATUS_CONFIRMED = "CONFIRMED";

	/** Where the distance used for the fare came from (see distanceSource field). */
	public static final String SOURCE_REQUEST = "REQUEST";
	public static final String SOURCE_RIDE_MANAGEMENT = "RIDE_MANAGEMENT";
	public static final String SOURCE_ESTIMATE = "ESTIMATE";

	@Id
	private String id;
	private String rideId;
	private String pickupLocation;
	private String destinationLocation;
	private Double distanceKm;
	private BigDecimal amount;
	private String status;
	private String distanceSource;

	public Fare() {
	}

	public Fare(String rideId, BigDecimal amount, String status) {
		this.rideId = rideId;
		this.amount = amount;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Which source supplied the distance of this fare:
	 * REQUEST (client input), RIDE_MANAGEMENT (actual distance of the
	 * completed ride) or ESTIMATE (fallback when ride-management is down).
	 */
	public String getDistanceSource() {
		return distanceSource;
	}

	public void setDistanceSource(String distanceSource) {
		this.distanceSource = distanceSource;
	}
}
