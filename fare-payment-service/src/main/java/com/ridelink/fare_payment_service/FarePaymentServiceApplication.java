package com.ridelink.fare_payment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SpringBootApplication
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
	description = "JWT issued by account-service (HS256). Send as: Authorization: Bearer <token>. "
		+ "Claim roles: PASSENGER, DRIVER or ADMIN. Obtained from account-service after login.")
public class FarePaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FarePaymentServiceApplication.class, args);
	}

}
