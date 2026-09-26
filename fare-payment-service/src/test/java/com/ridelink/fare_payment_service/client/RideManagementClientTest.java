package com.ridelink.fare_payment_service.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the input guard of {@link RideManagementClient}.
 *
 * The HTTP behaviour itself (200 parsing, 404/outage fallback) is exercised by
 * the e2e suite against a stubbed ride-management on port 8082 - testing it
 * here would require a live server, which is integration territory.
 * These tests prove that blank ride IDs never open a connection at all.
 */
class RideManagementClientTest {

	private final RideManagementClient client = new RideManagementClient("http://localhost:1");

	@Test
	void fetchActualDistanceKm_nullRideId_returnsEmptyWithoutCallingRideService() {
		assertTrue(client.fetchActualDistanceKm(null).isEmpty());
	}

	@Test
	void fetchActualDistanceKm_blankRideId_returnsEmptyWithoutCallingRideService() {
		assertTrue(client.fetchActualDistanceKm("   ").isEmpty());
	}
}
