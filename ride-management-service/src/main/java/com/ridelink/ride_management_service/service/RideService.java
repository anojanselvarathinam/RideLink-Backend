package com.ridelink.ride_management_service.service;

import java.util.List;
import java.util.Objects;

import com.ridelink.ride_management_service.dto.AssignDriverRequest;
import com.ridelink.ride_management_service.dto.CancelRideRequest;
import com.ridelink.ride_management_service.dto.CompleteRideRequest;
import com.ridelink.ride_management_service.dto.CreateRideRequest;
import com.ridelink.ride_management_service.dto.FareRideResponse;
import com.ridelink.ride_management_service.dto.RideMapper;
import com.ridelink.ride_management_service.dto.RideResponse;
import com.ridelink.ride_management_service.entity.Ride;
import com.ridelink.ride_management_service.entity.RideStatus;
import com.ridelink.ride_management_service.exception.InvalidRideStateException;
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

	public FareRideResponse getFareRideById(String rideId) {
		return RideMapper.toFareResponse(findRideOrThrow(rideId));
	}

	public List<RideResponse> getRides(String passengerId, String driverId, RideStatus status) {
		List<Ride> rides = findRides(passengerId, driverId, status);
		return rides.stream()
			.map(RideMapper::toResponse)
			.toList();
	}

	public RideResponse assignDriver(String rideId, AssignDriverRequest request) {
		if (!hasText(request.driverId())) {
			throw new IllegalArgumentException("driverId is required");
		}
		Ride ride = findRideOrThrow(rideId);
		// TODO: Validate driver eligibility and availability through Driver & Vehicle Service before assignment.
		transitionRide(ride, RideStatus.ASSIGNED);
		ride.setDriverId(request.driverId());
		return saveAndMap(ride);
	}

	public RideResponse acceptRide(String rideId) {
		Ride ride = findRideOrThrow(rideId);
		if (!hasText(ride.getDriverId())) {
			throw new InvalidRideStateException("Ride must have an assigned driver before it can be accepted");
		}
		transitionRide(ride, RideStatus.ACCEPTED);
		return saveAndMap(ride);
	}

	public RideResponse startRide(String rideId) {
		Ride ride = findRideOrThrow(rideId);
		transitionRide(ride, RideStatus.IN_PROGRESS);
		return saveAndMap(ride);
	}

	public RideResponse completeRide(String rideId, CompleteRideRequest request) {
		if (request.actualDistance() == null || request.actualDistance() <= 0) {
			throw new IllegalArgumentException("actualDistance must be greater than 0");
		}
		Ride ride = findRideOrThrow(rideId);
		transitionRide(ride, RideStatus.COMPLETED);
		ride.setActualDistance(request.actualDistance());
		return saveAndMap(ride);
	}

	public RideResponse cancelRide(String rideId, CancelRideRequest request) {
		if (!hasText(request.reason())) {
			throw new IllegalArgumentException("cancellation reason is required");
		}
		Ride ride = findRideOrThrow(rideId);
		transitionRide(ride, RideStatus.CANCELLED, request.reason());
		return saveAndMap(ride);
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

	private Ride findRideOrThrow(String rideId) {
		return rideRepository.findById(rideId)
			.orElseThrow(() -> new RideNotFoundException(rideId));
	}

	private RideResponse saveAndMap(Ride ride) {
		return RideMapper.toResponse(rideRepository.save(ride));
	}

	private void transitionRide(Ride ride, RideStatus nextStatus) {
		transitionRide(ride, nextStatus, null);
	}

	private void transitionRide(Ride ride, RideStatus nextStatus, String cancellationReason) {
		try {
			ride.transitionTo(nextStatus, cancellationReason);
		} catch (IllegalStateException exception) {
			throw new InvalidRideStateException(exception.getMessage());
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
