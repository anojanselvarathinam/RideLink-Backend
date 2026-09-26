package com.ridelink.ride_management_service;

import com.ridelink.ride_management_service.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest
class RideManagementServiceApplicationTests {

	@MockitoBean
	private RideRepository rideRepository;

	@MockitoBean(name = "mongoMappingContext")
	private MongoMappingContext mongoMappingContext;

	@Test
	void contextLoads() {
	}

}
