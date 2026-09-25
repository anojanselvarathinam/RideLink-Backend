package com.ridelink.ride_management_service.service;

import java.util.List;
import java.util.Objects;

import com.ridelink.ride_management_service.dto.CreateRideRequest;
import com.ridelink.ride_management_service.dto.RideMapper;
import com.ridelink.ride_management_service.dto.RideResponse;
import com.ridelink.ride_management_service.entity.Ride;
import com.ridelink.ride_management_service.entity.RideStatus;
import com.ridelink.ride_management_service.exception.RideNotFoundException;
import com.ridelink.ride_management_service.repository.RideRepository;
import org.springframework.stereotype.Service;

@Service
public class RideService {

	private final RideRepository rideRepository;

	public RideService(RideRepository rideRepository) {
		this.rideRepository = rideRepository;
	}

	public RideResponse createRide(CreateRideRequest request) {
		// Temporary integration assumption: passenger IDs are trusted until Account Service contracts are finalized.
		Ride ride = Ride.requested(
			request.passengerId(),
			RideMapper.toLocation(request.pickup()),
			RideMapper.toLocation(request.destination())
		);
		Ride savedRide = rideRepository.save(ride);
		return RideMapper.toResponse(savedRide);
	}

	public RideResponse getRideById(String rideId) {
		return rideRepository.findById(rideId)
			.map(RideMapper::toResponse)
			.orElseThrow(() -> new RideNotFoundException(rideId));
	}

	public List<RideResponse> getRides(String passengerId, String driverId, RideStatus status) {
		List<Ride> rides = findRides(passengerId, driverId, status);
		return rides.stream()
			.map(RideMapper::toResponse)
			.toList();
	}

	private List<Ride> findRides(String passengerId, String driverId, RideStatus status) {
		int filterCount = 0;
		filterCount += hasText(passengerId) ? 1 : 0;
		filterCount += hasText(driverId) ? 1 : 0;
		filterCount += status != null ? 1 : 0;

		if (filterCount == 0) {
			return rideRepository.findAll();
		}
		if (filterCount == 1) {
			if (hasText(passengerId)) {
				return rideRepository.findByPassengerId(passengerId);
			}
			if (hasText(driverId)) {
				return rideRepository.findByDriverId(driverId);
			}
			return rideRepository.findByStatus(status);
		}

		return rideRepository.findAll()
			.stream()
			.filter(ride -> !hasText(passengerId) || Objects.equals(ride.getPassengerId(), passengerId))
			.filter(ride -> !hasText(driverId) || Objects.equals(ride.getDriverId(), driverId))
			.filter(ride -> status == null || ride.getStatus() == status)
			.toList();
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
