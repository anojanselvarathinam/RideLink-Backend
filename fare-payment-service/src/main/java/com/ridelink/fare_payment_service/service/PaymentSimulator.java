package com.ridelink.fare_payment_service.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Simulated payment gateway (no real money is involved).
 *
 * Documented simulation rule:
 *   - CASH and WALLET payments are always approved.
 *   - CARD payments are declined when the amount exceeds the simulated
 *     per-transaction limit of LKR 50,000.00 (the payment then ends as FAILED
 *     and no receipt is issued).
 *
 * Unknown payment methods are rejected earlier by the API (HTTP 400) and
 * never reach this simulator.
 */
@Component
public class PaymentSimulator {

	public static final BigDecimal CARD_TRANSACTION_LIMIT = new BigDecimal("50000.00");
	public static final String METHOD_CARD = "CARD";

	public boolean authorize(BigDecimal amount, String method) {
		if (METHOD_CARD.equals(method)) {
			return amount.compareTo(CARD_TRANSACTION_LIMIT) <= 0;
		}
		return true;
	}
}
