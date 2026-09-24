package com.ridelink.fare_payment_service.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * RideLink fare calculation rule (documented and referenced by the API docs).
 *
 * <pre>
 * fare = baseFare + (ratePerKm x distanceKm)
 *
 *   baseFare  = LKR 300.00   (flag-down charge)
 *   ratePerKm = LKR 120.00   per kilometre
 *
 * The result is rounded to 2 decimal places (HALF_UP).
 * Example: 10 km -> 300 + (120 x 10) = LKR 1500.00
 * </pre>
 */
@Component
public class FareCalculator {

	public static final BigDecimal BASE_FARE = new BigDecimal("300.00");
	public static final BigDecimal RATE_PER_KM = new BigDecimal("120.00");

	public BigDecimal calculate(double distanceKm) {
		BigDecimal distance = BigDecimal.valueOf(distanceKm);
		return BASE_FARE
			.add(RATE_PER_KM.multiply(distance))
			.setScale(2, RoundingMode.HALF_UP);
	}
}
