package com.ridelink.ride_management_service.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RideTest {

	@Test
	void newRideStartsAsRequestedWithoutDriver() {
		Ride ride = sampleRide();

		assertThat(ride.getPassengerId()).isEqualTo("passenger-1");
		assertThat(ride.getStatus()).isEqualTo(RideStatus.REQUESTED);
		assertThat(ride.getDriverId()).isNull();
		assertThat(ride.getPickup()).isNotNull();
		assertThat(ride.getDestination()).isNotNull();
		assertThat(ride.getRequestedAt()).isNotNull();
	}

	@Test
	void transitionUpdatesStatusAndMatchingTimestamp() {
		Ride ride = sampleRide();

		ride.transitionTo(RideStatus.ASSIGNED);

		assertThat(ride.getStatus()).isEqualTo(RideStatus.ASSIGNED);
		assertThat(ride.getAssignedAt()).isNotNull();
		assertThat(ride.getUpdatedAt()).isNotNull();
	}

	@Test
	void cancellationStoresReasonAndTimestamp() {
		Ride ride = sampleRide();

		ride.transitionTo(RideStatus.CANCELLED, "Passenger changed plans");

		assertThat(ride.getStatus()).isEqualTo(RideStatus.CANCELLED);
		assertThat(ride.getCancellationReason()).isEqualTo("Passenger changed plans");
		assertThat(ride.getCancelledAt()).isNotNull();
		assertThat(ride.getUpdatedAt()).isNotNull();
	}

	@Test
	void invalidTransitionDoesNotUpdateStatus() {
		Ride ride = sampleRide();

		assertThatThrownBy(() -> ride.transitionTo(RideStatus.COMPLETED))
			.isInstanceOf(IllegalStateException.class);
		assertThat(ride.getStatus()).isEqualTo(RideStatus.REQUESTED);
	}

	private static Ride sampleRide() {
		return Ride.requested(
			"passenger-1",
			new RideLocation("Campus Gate", 6.9271, 79.8612),
			new RideLocation("Library", 6.902, 79.86)
		);
	}
}
