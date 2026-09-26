package com.ridelink.fare_payment_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.security.SecureRandom;
import java.util.Base64;

@SpringBootTest
class FarePaymentServiceApplicationTests {

	/**
	 * Supplies a RANDOM HS256 signing secret to the test context so that
	 * {@code mvn test} (and the CI pipeline) pass WITHOUT any environment
	 * variable being set and WITHOUT committing a secret to the repository
	 * (brief 6.3). The running application still requires the real
	 * RIDELINK_JWT_SECRET environment variable at startup (fail-fast).
	 */
	@DynamicPropertySource
	static void jwtSecret(DynamicPropertyRegistry registry) {
		byte[] key = new byte[32];
		new SecureRandom().nextBytes(key);
		registry.add("RIDELINK_JWT_SECRET", () -> Base64.getEncoder().encodeToString(key));
		// Never auto-open the Swagger browser tab from the test suite / CI.
		registry.add("ridelink.swagger.auto-open", () -> "false");
	}

	@Test
	void contextLoads() {
	}

}
