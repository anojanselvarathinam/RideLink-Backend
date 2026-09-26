package com.ridelink.ride_management_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI rideManagementOpenApi() {
		return new OpenAPI()
			.info(new Info()
				.title("RideLink - Ride Management Service API")
				.description("Handles ride requests, driver assignment, ride lifecycle, cancellation, and ride retrieval.")
				.version("v1"));
	}
}
