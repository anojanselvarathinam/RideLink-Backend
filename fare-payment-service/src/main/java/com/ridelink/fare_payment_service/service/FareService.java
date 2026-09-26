package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.client.RideManagementClient;
import com.ridelink.fare_payment_service.dto.FareEstimateRequest;
import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.exception.ConflictException;
import com.ridelink.fare_payment_service.exception.ResourceNotFoundException;
import com.ridelink.fare_payment_service.exception.ValidationException;
import com.ridelink.fare_payment_service.repository.FareRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FareService {

	private final FareRepository fareRepository;
	private final FareCalculator fareCalculator;
	private final RideManagementClient rideManagementClient;

	public FareService(FareRepository fareRepository, FareCalculator fareCalculator,
			RideManagementClient rideManagementClient) {
		this.fareRepository = fareRepository;
		this.fareCalculator = fareCalculator;
		this.rideManagementClient = rideManagementClient;
	}

	/**
	 * Creates a fare estimate: the amount is always produced by the documented
	 * calculation rule, never accepted directly from the client.
	 * (Field validation happens in the controller via @Valid.)
	 */
	public Fare estimate(FareEstimateRequest request) {
		Fare fare = new Fare();
		fare.setRideId(request.getRideId());
		fare.setPickupLocation(request.getPickupLocation());
		fare.setDestinationLocation(request.getDestinationLocation());
		fare.setDistanceKm(request.getDistanceKm());
		fare.setAmount(fareCalculator.calculate(request.getDistanceKm()));
		fare.setStatus(Fare.STATUS_ESTIMATED);
		fare.setDistanceSource(Fare.SOURCE_REQUEST);
		return fareRepository.save(fare);
	}

	/**
	 * Finalizes a fare with the final distance and sets status CONFIRMED.
	 *
	 * Distance precedence (recorded in the fare's distanceSource field):
	 * 1. explicit distanceKm query parameter (REQUEST)
	 * 2. ACTUAL distance fetched from ride-management via synchronous REST
	 *    call by rideId (RIDE_MANAGEMENT) - authoritative source
	 * 3. the estimated distance, when ride-management is unreachable or the
	 *    ride is unknown (ESTIMATE) - graceful degradation
	 *
	 * @throws ResourceNotFoundException fare does not exist
	 * @throws ConflictException         fare was already finalized
	 * @throws ValidationException       no valid distance available
	 */
	public Fare finalizeFare(String id, Double actualDistanceKm) {
		Fare fare = fareRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Fare not found: " + id));
		if (!Fare.STATUS_ESTIMATED.equals(fare.getStatus())) {
			throw new ConflictException("Fare has already been finalized");
		}

		Double distance;
		String source;
		if (actualDistanceKm != null) {
			distance = actualDistanceKm;
			source = Fare.SOURCE_REQUEST;
		} else {
			Optional<Double> actualFromRide = rideManagementClient.fetchActualDistanceKm(fare.getRideId());
			if (actualFromRide.isPresent()) {
				distance = actualFromRide.get();
				source = Fare.SOURCE_RIDE_MANAGEMENT;
			} else {
				distance = fare.getDistanceKm();
				source = Fare.SOURCE_ESTIMATE;
			}
		}
		if (distance == null || distance <= 0) {
			throw new ValidationException("distanceKm must be greater than 0");
		}
		fare.setDistanceKm(distance);
		fare.setAmount(fareCalculator.calculate(distance));
		fare.setStatus(Fare.STATUS_CONFIRMED);
		fare.setDistanceSource(source);
		return fareRepository.save(fare);
	}

	public List<Fare> findAll() {
		return fareRepository.findAll();
	}

	public Optional<Fare> findById(String id) {
		return fareRepository.findById(id);
	}

	public List<Fare> findByRideId(String rideId) {
		return fareRepository.findByRideId(rideId);
	}

	public Fare update(Fare fare) {
		return fareRepository.save(fare);
	}

	public void delete(String id) {
		fareRepository.deleteById(id);
	}
}
