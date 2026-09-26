package com.ridelink.ride_management_service.entity;

import jakarta.validation.constraints.NotBlank;

public class RideLocation {

	@NotBlank
	private String placeName;

	private Double latitude;

	private Double longitude;

	protected RideLocation() {
	}

	public RideLocation(String placeName, Double latitude, Double longitude) {
		if (placeName == null || placeName.isBlank()) {
			throw new IllegalArgumentException("placeName is required");
		}
		this.placeName = placeName;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getPlaceName() {
		return placeName;
	}

	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
}
