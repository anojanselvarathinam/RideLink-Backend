package com.ridelink.account_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AccountServiceApplicationTests {
    @org.springframework.test.context.DynamicPropertySource
    static void jwtTestSecret(org.springframework.test.context.DynamicPropertyRegistry registry) {
        byte[] key = new byte[32];
        new java.security.SecureRandom().nextBytes(key);
        registry.add("JWT_SECRET", () -> java.util.Base64.getEncoder().encodeToString(key));
    }
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.ridelink.account_service.config.AdminInitializer initializer;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.ridelink.account_service.config.SwaggerBrowserLauncher browserLauncher;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.ridelink.account_service.repository.UserRepository repository;

	@Test
	void contextLoads() {
	}

}
