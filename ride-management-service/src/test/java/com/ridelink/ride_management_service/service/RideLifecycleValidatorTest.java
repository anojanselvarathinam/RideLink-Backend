package com.ridelink.ride_management_service.service;

import static com.ridelink.ride_management_service.entity.RideStatus.ACCEPTED;
import static com.ridelink.ride_management_service.entity.RideStatus.ASSIGNED;
import static com.ridelink.ride_management_service.entity.RideStatus.CANCELLED;
import static com.ridelink.ride_management_service.entity.RideStatus.COMPLETED;
import static com.ridelink.ride_management_service.entity.RideStatus.IN_PROGRESS;
import static com.ridelink.ride_management_service.entity.RideStatus.REQUESTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ridelink.ride_management_service.entity.RideStatus;
import org.junit.jupiter.api.Test;

class RideLifecycleValidatorTest {

	@Test
	void allowsMainLifecycleTransitions() {
		assertThat(RideLifecycleValidator.canTransition(REQUESTED, ASSIGNED)).isTrue();
		assertThat(RideLifecycleValidator.canTransition(ASSIGNED, ACCEPTED)).isTrue();
		assertThat(RideLifecycleValidator.canTransition(ACCEPTED, IN_PROGRESS)).isTrue();
		assertThat(RideLifecycleValidator.canTransition(IN_PROGRESS, COMPLETED)).isTrue();
	}

	@Test
	void allowsCancellationBeforeRideStarts() {
		assertThat(RideLifecycleValidator.canTransition(REQUESTED, CANCELLED)).isTrue();
		assertThat(RideLifecycleValidator.canTransition(ASSIGNED, CANCELLED)).isTrue();
		assertThat(RideLifecycleValidator.canTransition(ACCEPTED, CANCELLED)).isTrue();
	}

	@Test
	void rejectsInvalidTransitions() {
		assertInvalid(REQUESTED, COMPLETED);
		assertInvalid(REQUESTED, IN_PROGRESS);
		assertInvalid(ASSIGNED, COMPLETED);
		assertInvalid(ACCEPTED, COMPLETED);
		assertInvalid(IN_PROGRESS, ACCEPTED);
		assertInvalid(IN_PROGRESS, CANCELLED);
		assertInvalid(COMPLETED, CANCELLED);
		assertInvalid(CANCELLED, REQUESTED);
	}

	@Test
	void completedRideRejectsFurtherNormalTransitions() {
		for (RideStatus nextStatus : new RideStatus[] { ASSIGNED, ACCEPTED, IN_PROGRESS, COMPLETED }) {
			assertInvalid(COMPLETED, nextStatus);
		}
	}

	private static void assertInvalid(RideStatus currentStatus, RideStatus nextStatus) {
		assertThat(RideLifecycleValidator.canTransition(currentStatus, nextStatus)).isFalse();
		assertThatThrownBy(() -> RideLifecycleValidator.validateTransition(currentStatus, nextStatus))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Invalid ride status transition from " + currentStatus + " to " + nextStatus);
	}
}
