package com.ridelink.fare_payment_service.service;

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

	public FareService(FareRepository fareRepository, FareCalculator fareCalculator) {
		this.fareRepository = fareRepository;
		this.fareCalculator = fareCalculator;
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
		return fareRepository.save(fare);
	}

	/**
	 * Finalizes a fare: recalculates with the actual travelled distance
	 * (query param) or the estimated distance, then sets status CONFIRMED.
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
		Double distance = actualDistanceKm != null ? actualDistanceKm : fare.getDistanceKm();
		if (distance == null || distance <= 0) {
			throw new ValidationException("distanceKm must be greater than 0");
		}
		fare.setDistanceKm(distance);
		fare.setAmount(fareCalculator.calculate(distance));
		fare.setStatus(Fare.STATUS_CONFIRMED);
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
