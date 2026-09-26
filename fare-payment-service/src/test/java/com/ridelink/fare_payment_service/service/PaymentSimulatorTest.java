package com.ridelink.fare_payment_service.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the documented simulation rule:
 * <pre>
 *   CASH / WALLET always approve;
 *   CARD approves up to and including LKR 50,000.00, declines above it.
 * </pre>
 * The boundary between "exactly at the limit" and "one cent above" is the
 * key case. Method validation (@Pattern) happens at the API layer.
 */
class PaymentSimulatorTest {

	private final PaymentSimulator simulator = new PaymentSimulator();

	// --- normal behaviour ---

	@Test
	void authorize_cash_alwaysApproves() {
		assertTrue(simulator.authorize(new BigDecimal("1500.00"), "CASH"));
	}

	@Test
	void authorize_wallet_alwaysApproves() {
		assertTrue(simulator.authorize(new BigDecimal("500.00"), "WALLET"));
	}

	// --- boundary behaviour ---

	@Test
	void authorize_cardExactlyAtLimit_approves() {
		// The rule is "amounts ABOVE 50,000.00 decline" - the limit itself passes.
		assertTrue(simulator.authorize(new BigDecimal("50000.00"), "CARD"));
	}

	@Test
	void authorize_oneCentAboveLimit_declines() {
		assertFalse(simulator.authorize(new BigDecimal("50000.01"), "CARD"));
	}

	// --- failure / decline path ---

	@Test
	void authorize_cardFarAboveLimit_declines() {
		assertFalse(simulator.authorize(new BigDecimal("99999.99"), "CARD"));
	}
}
