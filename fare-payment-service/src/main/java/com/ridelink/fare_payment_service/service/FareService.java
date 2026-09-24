package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.FareEstimateRequest;
import com.ridelink.fare_payment_service.entity.Fare;
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
	 * Recalculates the fare with the actual travelled distance (or the estimated
	 * distance when none is provided) and marks it as CONFIRMED.
	 */
	public Fare finalizeFare(Fare fare, Double actualDistanceKm) {
		double distance = actualDistanceKm != null ? actualDistanceKm : fare.getDistanceKm();
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
