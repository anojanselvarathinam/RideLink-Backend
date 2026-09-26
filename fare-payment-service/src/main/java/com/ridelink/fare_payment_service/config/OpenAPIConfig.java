package com.ridelink.fare_payment_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

	@Bean
	public OpenAPI farePaymentServiceOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Fare & Payment Service API")
				.description("REST API for ride fares, payments and receipts in the RideLink system")
				.version("1.0.0"));
	}
}
