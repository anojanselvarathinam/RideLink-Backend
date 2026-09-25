package com.ridelink.ride_management_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.ridelink.ride_management_service.dto.CreateRideRequest;
import com.ridelink.ride_management_service.dto.LocationRequest;
import com.ridelink.ride_management_service.dto.RideResponse;
import com.ridelink.ride_management_service.entity.Ride;
import com.ridelink.ride_management_service.entity.RideLocation;
import com.ridelink.ride_management_service.entity.RideStatus;
import com.ridelink.ride_management_service.exception.RideNotFoundException;
import com.ridelink.ride_management_service.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RideServiceTest {

	private RideRepository rideRepository;

	private RideService rideService;

	@BeforeEach
	void setUp() {
		rideRepository = mock(RideRepository.class);
		rideService = new RideService(rideRepository);
	}

	@Test
	void createsRideAsRequestedWithoutDriverAndMapsLocations() {
		when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
			Ride ride = invocation.getArgument(0);
			ride.setId("ride-1");
			ride.setCreatedAt(Instant.parse("2026-09-25T04:00:00Z"));
			ride.setUpdatedAt(Instant.parse("2026-09-25T04:00:00Z"));
			return ride;
		});

		RideResponse response = rideService.createRide(createRequest());

		assertThat(response.id()).isEqualTo("ride-1");
		assertThat(response.passengerId()).isEqualTo("P001");
		assertThat(response.status()).isEqualTo(RideStatus.REQUESTED);
		assertThat(response.driverId()).isNull();
		assertThat(response.pickup().placeName()).isEqualTo("Colombo Fort");
		assertThat(response.pickup().latitude()).isEqualTo(6.9344);
		assertThat(response.destination().placeName()).isEqualTo("Bambalapitiya");
		assertThat(response.destination().longitude()).isEqualTo(79.8563);
		assertThat(response.requestedAt()).isNotNull();

		ArgumentCaptor<Ride> captor = ArgumentCaptor.forClass(Ride.class);
		verify(rideRepository).save(captor.capture());
		Ride savedRide = captor.getValue();
		assertThat(savedRide.getStatus()).isEqualTo(RideStatus.REQUESTED);
		assertThat(savedRide.getDriverId()).isNull();
		assertThat(savedRide.getPickup().getPlaceName()).isEqualTo("Colombo Fort");
		assertThat(savedRide.getDestination().getPlaceName()).isEqualTo("Bambalapitiya");
	}

	@Test
	void retrievesExistingRideById() {
		Ride ride = sampleRide("ride-1", "P001", null, RideStatus.REQUESTED);
		when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));

		RideResponse response = rideService.getRideById("ride-1");

		assertThat(response.id()).isEqualTo("ride-1");
		assertThat(response.passengerId()).isEqualTo("P001");
	}

	@Test
	void missingRideByIdThrowsException() {
		when(rideRepository.findById("missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> rideService.getRideById("missing"))
			.isInstanceOf(RideNotFoundException.class)
			.hasMessageContaining("missing");
	}

	@Test
	void retrievesRidesByPassengerId() {
		when(rideRepository.findByPassengerId("P001"))
			.thenReturn(List.of(sampleRide("ride-1", "P001", null, RideStatus.REQUESTED)));

		List<RideResponse> responses = rideService.getRides("P001", null, null);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).passengerId()).isEqualTo("P001");
		verify(rideRepository).findByPassengerId("P001");
	}

	@Test
	void retrievesRidesByDriverId() {
		when(rideRepository.findByDriverId("D001"))
			.thenReturn(List.of(sampleRide("ride-2", "P002", "D001", RideStatus.ASSIGNED)));

		List<RideResponse> responses = rideService.getRides(null, "D001", null);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).driverId()).isEqualTo("D001");
		verify(rideRepository).findByDriverId("D001");
	}

	@Test
	void retrievesRidesByStatus() {
		when(rideRepository.findByStatus(RideStatus.REQUESTED))
			.thenReturn(List.of(sampleRide("ride-3", "P003", null, RideStatus.REQUESTED)));

		List<RideResponse> responses = rideService.getRides(null, null, RideStatus.REQUESTED);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).status()).isEqualTo(RideStatus.REQUESTED);
		verify(rideRepository).findByStatus(RideStatus.REQUESTED);
	}

	private static CreateRideRequest createRequest() {
		return new CreateRideRequest(
			"P001",
			new LocationRequest("Colombo Fort", 6.9344, 79.8428),
			new LocationRequest("Bambalapitiya", 6.8936, 79.8563)
		);
	}

	private static Ride sampleRide(String id, String passengerId, String driverId, RideStatus status) {
		Ride ride = Ride.requested(
			passengerId,
			new RideLocation("Colombo Fort", 6.9344, 79.8428),
			new RideLocation("Bambalapitiya", 6.8936, 79.8563)
		);
		ride.setId(id);
		ride.setDriverId(driverId);
		ride.setStatus(status);
		ride.setCreatedAt(Instant.parse("2026-09-25T04:00:00Z"));
		ride.setUpdatedAt(Instant.parse("2026-09-25T04:00:00Z"));
		return ride;
	}
}
