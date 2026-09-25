package com.ridelink.ride_management_service.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.ridelink.ride_management_service.service.RideLifecycleValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rides")
public class Ride {

	@Id
	private String id;

	@NotBlank
	private String passengerId;

	private String driverId;

	@NotNull
	@Valid
	private RideLocation pickup;

	@NotNull
	@Valid
	private RideLocation destination;

	@NotNull
	private RideStatus status = RideStatus.REQUESTED;

	private BigDecimal estimatedFare;

	private BigDecimal finalFare;

	private Double actualDistance;

	private String cancellationReason;

	private Instant requestedAt;

	private Instant assignedAt;

	private Instant acceptedAt;

	private Instant startedAt;

	private Instant completedAt;

	private Instant cancelledAt;

	@CreatedDate
	private Instant createdAt;

	@LastModifiedDate
	private Instant updatedAt;

	protected Ride() {
	}

	private Ride(String passengerId, RideLocation pickup, RideLocation destination) {
		if (passengerId == null || passengerId.isBlank()) {
			throw new IllegalArgumentException("passengerId is required");
		}
		if (pickup == null) {
			throw new IllegalArgumentException("pickup is required");
		}
		if (destination == null) {
			throw new IllegalArgumentException("destination is required");
		}
		this.passengerId = passengerId;
		this.pickup = pickup;
		this.destination = destination;
		this.status = RideStatus.REQUESTED;
		this.requestedAt = Instant.now();
	}

	public static Ride requested(String passengerId, RideLocation pickup, RideLocation destination) {
		return new Ride(passengerId, pickup, destination);
	}

	public boolean canTransitionTo(RideStatus nextStatus) {
		return RideLifecycleValidator.canTransition(status, nextStatus);
	}

	public void transitionTo(RideStatus nextStatus) {
		transitionTo(nextStatus, null);
	}

	public void transitionTo(RideStatus nextStatus, String cancellationReason) {
		RideLifecycleValidator.validateTransition(status, nextStatus);
		Instant now = Instant.now();
		this.status = nextStatus;
		this.updatedAt = now;

		switch (nextStatus) {
			case ASSIGNED -> this.assignedAt = now;
			case ACCEPTED -> this.acceptedAt = now;
			case IN_PROGRESS -> this.startedAt = now;
			case COMPLETED -> this.completedAt = now;
			case CANCELLED -> {
				this.cancelledAt = now;
				this.cancellationReason = cancellationReason;
			}
			case REQUESTED -> {
			}
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassengerId() {
		return passengerId;
	}

	public void setPassengerId(String passengerId) {
		this.passengerId = passengerId;
	}

	public String getDriverId() {
		return driverId;
	}

	public void setDriverId(String driverId) {
		this.driverId = driverId;
	}

	public RideLocation getPickup() {
		return pickup;
	}

	public void setPickup(RideLocation pickup) {
		this.pickup = pickup;
	}

	public RideLocation getDestination() {
		return destination;
	}

	public void setDestination(RideLocation destination) {
		this.destination = destination;
	}

	public RideStatus getStatus() {
		return status;
	}

	public void setStatus(RideStatus status) {
		this.status = status;
	}

	public BigDecimal getEstimatedFare() {
		return estimatedFare;
	}

	public void setEstimatedFare(BigDecimal estimatedFare) {
		this.estimatedFare = estimatedFare;
	}

	public BigDecimal getFinalFare() {
		return finalFare;
	}

	public void setFinalFare(BigDecimal finalFare) {
		this.finalFare = finalFare;
	}

	public Double getActualDistance() {
		return actualDistance;
	}

	public void setActualDistance(Double actualDistance) {
		this.actualDistance = actualDistance;
	}

	public String getCancellationReason() {
		return cancellationReason;
	}

	public void setCancellationReason(String cancellationReason) {
		this.cancellationReason = cancellationReason;
	}

	public Instant getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(Instant requestedAt) {
		this.requestedAt = requestedAt;
	}

	public Instant getAssignedAt() {
		return assignedAt;
	}

	public void setAssignedAt(Instant assignedAt) {
		this.assignedAt = assignedAt;
	}

	public Instant getAcceptedAt() {
		return acceptedAt;
	}

	public void setAcceptedAt(Instant acceptedAt) {
		this.acceptedAt = acceptedAt;
	}

	public Instant getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Instant startedAt) {
		this.startedAt = startedAt;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Instant completedAt) {
		this.completedAt = completedAt;
	}

	public Instant getCancelledAt() {
		return cancelledAt;
	}

	public void setCancelledAt(Instant cancelledAt) {
		this.cancelledAt = cancelledAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
