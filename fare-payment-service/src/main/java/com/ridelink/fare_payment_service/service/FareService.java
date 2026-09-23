package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.repository.FareRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FareService {

	private final FareRepository fareRepository;

	public FareService(FareRepository fareRepository) {
		this.fareRepository = fareRepository;
	}

	public Fare create(Fare fare) {
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
