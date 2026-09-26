package com.ridelink.fare_payment_service.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the documented fare rule:
 * <pre>
 *   fare = 300.00 + (120.00 x distanceKm), rounded to 2dp HALF_UP
 * </pre>
 *
 * FareCalculator has no dependencies, so it is created directly - no Spring
 * context, no MongoDB, no HTTP. Input validation (distanceKm &gt; 0) lives at
 * the API layer via @Positive and is covered by the e2e suite.
 */
class FareCalculatorTest {

	private final FareCalculator calculator = new FareCalculator();

	// --- normal behaviour ---

	@Test
	void calculate_10km_returnsBasePlusRate() {
		// 300.00 + (120.00 x 10) = 1500.00
		assertEquals(new BigDecimal("1500.00"), calculator.calculate(10));
	}

	@Test
	void calculate_fractionalDistance_returnsExactAmount() {
		// 300.00 + (120.00 x 7.7) = 1224.00 (same value the e2e suite asserts)
		assertEquals(new BigDecimal("1224.00"), calculator.calculate(7.7));
	}

	// --- boundary behaviour ---

	@Test
	void calculate_zeroDistance_returnsBaseFareOnly() {
		// The pure rule has no minimum: 0 km -> flag-down fare only.
		// (The API rejects distanceKm = 0 with HTTP 400 via @Positive - e2e suite.)
		assertEquals(new BigDecimal("300.00"), calculator.calculate(0));
	}

	@Test
	void calculate_resultWithThirdDecimal_roundsHalfUpAtTwoDecimals() {
		// 300.00 + (120.00 x 0.0004) = 300.048 -> HALF_UP at 2dp -> 300.05
		assertEquals(new BigDecimal("300.05"), calculator.calculate(0.0004));
	}

	@Test
	void calculate_recurringDecimal_roundsHalfUpToTwoDecimals() {
		// 300.00 + (120.00 x 3.3333) = 699.996 -> HALF_UP at 2dp -> 700.00
		assertEquals(new BigDecimal("700.00"), calculator.calculate(3.3333));
	}
}
