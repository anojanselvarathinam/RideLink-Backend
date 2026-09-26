package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.client.RideManagementClient;
import com.ridelink.fare_payment_service.dto.FareEstimateRequest;
import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.exception.ConflictException;
import com.ridelink.fare_payment_service.exception.ResourceNotFoundException;
import com.ridelink.fare_payment_service.exception.ValidationException;
import com.ridelink.fare_payment_service.repository.FareRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FareService} with MOCKED collaborators
 * (repository, calculator, ride-management client) - the service's business
 * rules are verified without Spring, MongoDB or HTTP.
 *
 * Coverage per rubric: normal (estimate, all three distance sources),
 * boundary (fallback precedence), failure (not found, conflict, no distance).
 */
@ExtendWith(MockitoExtension.class)
class FareServiceTest {

	@Mock
	private FareRepository fareRepository;

	@Mock
	private FareCalculator fareCalculator;

	@Mock
	private RideManagementClient rideManagementClient;

	@InjectMocks
	private FareService fareService;

	// --- normal behaviour ---

	@Test
	void estimate_validRequest_savesEstimatedFareWithCalculatedAmount() {
		FareEstimateRequest request = new FareEstimateRequest();
		request.setRideId("RIDE-IT4-TEST-EST");
		request.setPickupLocation("Colombo Fort");
		request.setDestinationLocation("Kandy");
		request.setDistanceKm(10.0);
		when(fareCalculator.calculate(10.0)).thenReturn(new BigDecimal("1500.00"));
		when(fareRepository.save(any(Fare.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Fare result = fareService.estimate(request);

		assertEquals(Fare.STATUS_ESTIMATED, result.getStatus());
		assertEquals(Fare.SOURCE_REQUEST, result.getDistanceSource());
		assertEquals(new BigDecimal("1500.00"), result.getAmount());
		assertEquals(10.0, result.getDistanceKm());
		assertEquals("RIDE-IT4-TEST-EST", result.getRideId());
		verify(fareRepository).save(any(Fare.class));
	}

	@Test
	void finalizeFare_withExplicitDistance_usesRequestDistanceAndSkipsRideService() {
		Fare fare = estimatedFare("f1", "RIDE-IT4-TEST-PARAM", 10.0);
		when(fareRepository.findById("f1")).thenReturn(Optional.of(fare));
		when(fareRepository.save(any(Fare.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(fareCalculator.calculate(7.5)).thenReturn(new BigDecimal("1200.00"));

		Fare result = fareService.finalizeFare("f1", 7.5);

		assertEquals(Fare.STATUS_CONFIRMED, result.getStatus());
		assertEquals(Fare.SOURCE_REQUEST, result.getDistanceSource());
		assertEquals(7.5, result.getDistanceKm());
		assertEquals(new BigDecimal("1200.00"), result.getAmount());
		// explicit parameter wins - ride-management must NOT be called
		verify(rideManagementClient, never()).fetchActualDistanceKm(any());
	}

	// --- Step 4 interaction: distance source precedence ---

	@Test
	void finalizeFare_rideManagementHasActualDistance_usesItAsAuthoritativeSource() {
		Fare fare = estimatedFare("f2", "RIDE-IT4-TEST-RIDE", 10.0);
		when(fareRepository.findById("f2")).thenReturn(Optional.of(fare));
		when(fareRepository.save(any(Fare.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(rideManagementClient.fetchActualDistanceKm("RIDE-IT4-TEST-RIDE"))
			.thenReturn(Optional.of(7.7));
		when(fareCalculator.calculate(7.7)).thenReturn(new BigDecimal("1224.00"));

		Fare result = fareService.finalizeFare("f2", null);

		assertEquals(Fare.STATUS_CONFIRMED, result.getStatus());
		assertEquals(Fare.SOURCE_RIDE_MANAGEMENT, result.getDistanceSource());
		assertEquals(7.7, result.getDistanceKm());
		assertEquals(new BigDecimal("1224.00"), result.getAmount());
	}

	@Test
	void finalizeFare_rideServiceUnreachable_fallsBackToEstimatedDistance() {
		Fare fare = estimatedFare("f3", "RIDE-IT4-TEST-DOWN", 10.0);
		when(fareRepository.findById("f3")).thenReturn(Optional.of(fare));
		when(fareRepository.save(any(Fare.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(rideManagementClient.fetchActualDistanceKm("RIDE-IT4-TEST-DOWN"))
			.thenReturn(Optional.empty());
		when(fareCalculator.calculate(10.0)).thenReturn(new BigDecimal("1500.00"));

		Fare result = fareService.finalizeFare("f3", null);

		assertEquals(Fare.STATUS_CONFIRMED, result.getStatus());
		assertEquals(Fare.SOURCE_ESTIMATE, result.getDistanceSource());
		assertEquals(10.0, result.getDistanceKm());
		assertEquals(new BigDecimal("1500.00"), result.getAmount());
	}

	// --- failure behaviour ---

	@Test
	void finalizeFare_unknownFare_throwsResourceNotFoundAndSavesNothing() {
		when(fareRepository.findById("missing")).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
			() -> fareService.finalizeFare("missing", 5.0));

		assertEquals("Fare not found: missing", exception.getMessage());
		verify(fareRepository, never()).save(any(Fare.class));
	}

	@Test
	void finalizeFare_alreadyConfirmed_throwsConflictAndSavesNothing() {
		Fare fare = new Fare("RIDE-IT4-TEST-DUP", new BigDecimal("1500.00"), Fare.STATUS_CONFIRMED);
		fare.setId("f4");
		when(fareRepository.findById("f4")).thenReturn(Optional.of(fare));

		ConflictException exception = assertThrows(ConflictException.class,
			() -> fareService.finalizeFare("f4", 5.0));

		assertEquals("Fare has already been finalized", exception.getMessage());
		verify(fareRepository, never()).save(any(Fare.class));
	}

	@Test
	void finalizeFare_noDistanceAnywhere_throwsValidation() {
		// estimate distance missing (ride never produced one), ride-service empty,
		// no explicit parameter -> there is nothing to charge for
		Fare fare = estimatedFare("f5", "RIDE-IT4-TEST-NODIST", null);
		when(fareRepository.findById("f5")).thenReturn(Optional.of(fare));
		when(rideManagementClient.fetchActualDistanceKm("RIDE-IT4-TEST-NODIST"))
			.thenReturn(Optional.empty());

		ValidationException exception = assertThrows(ValidationException.class,
			() -> fareService.finalizeFare("f5", null));

		assertEquals("distanceKm must be greater than 0", exception.getMessage());
		verify(fareRepository, never()).save(any(Fare.class));
	}

	// --- helper ---

	private Fare estimatedFare(String id, String rideId, Double distanceKm) {
		Fare fare = new Fare(rideId, new BigDecimal("1500.00"), Fare.STATUS_ESTIMATED);
		fare.setId(id);
		fare.setDistanceKm(distanceKm);
		return fare;
	}
}
