package com.ridelink.account_service.config;

import com.ridelink.account_service.controller.AccountController;
import com.ridelink.account_service.entity.AccountStatus;
import com.ridelink.account_service.entity.Role;
import com.ridelink.account_service.entity.User;
import com.ridelink.account_service.repository.UserRepository;
import com.ridelink.account_service.service.JwtService;
import com.ridelink.account_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(DeactivateAccountTests.TestConfig.class)
@WebAppConfiguration
class DeactivateAccountTests {

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, AccountController.class, UserService.class})
    static class TestConfig {
        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        JwtService jwtService() {
            // Ephemeral test key; no application credentials or database are used.
            return new JwtService(Base64.getEncoder().encodeToString(new byte[32]), 60000);
        }
    }

    @Autowired private WebApplicationContext context;
    @Autowired private UserRepository repository;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JwtService jwtService;
    private MockMvc mvc;
    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        reset(repository);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        user = new User();
        user.setId("test-account");
        user.setFullName("Test Passenger");
        user.setEmail("passenger@example.test");
        user.setUsername("test-passenger");
        user.setPhoneNumber("0771234567");
        user.setPassword(encoder.encode("CurrentPassword@20"));
        user.setRole(Role.PASSENGER);
        user.setStatus(AccountStatus.ACTIVE);
        when(repository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(repository.findById(user.getId())).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        token = jwtService.generateToken(user);
    }

    @Test
    void passengerDeactivationPreservesFieldsAndRejectsOldTokenUntilAdminReactivates() throws Exception {
        String originalHash = user.getPassword();
        mvc.perform(post("/api/accounts/login").contentType("application/json")
                .content("{\"identifier\":\"passenger@example.test\",\"password\":\"CurrentPassword@20\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("token").isNotEmpty());

        deactivate("{\"password\":\"CurrentPassword@20\"}")
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Account deactivated successfully\"}"));
        assertEquals(AccountStatus.INACTIVE, user.getStatus());
        assertEquals("test-account", user.getId());
        assertEquals("Test Passenger", user.getFullName());
        assertEquals("passenger@example.test", user.getEmail());
        assertEquals("test-passenger", user.getUsername());
        assertEquals("0771234567", user.getPhoneNumber());
        assertEquals(originalHash, user.getPassword());
        assertEquals(Role.PASSENGER, user.getRole());
        verify(repository).save(user);
        verify(repository, never()).delete(any(User.class));

        mvc.perform(get("/api/accounts/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/accounts/me").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{\"fullName\":\"Updated Name\",\"phoneNumber\":\"0771234567\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/accounts/me/password").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        deactivate("{\"password\":\"CurrentPassword@20\"}").andExpect(status().isForbidden());
        mvc.perform(post("/api/accounts/login").contentType("application/json")
                .content("{\"identifier\":\"passenger@example.test\",\"password\":\"CurrentPassword@20\"}"))
                .andExpect(status().isForbidden());

        User admin = new User();
        admin.setEmail("admin@example.test");
        admin.setRole(Role.ADMIN);
        admin.setStatus(AccountStatus.ACTIVE);
        when(repository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        mvc.perform(patch("/api/accounts/admin/users/test-account/status")
                .header("Authorization", "Bearer " + jwtService.generateToken(admin))
                .contentType("application/json").content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk());
        assertEquals(AccountStatus.ACTIVE, user.getStatus());
    }

    @Test
    void driverCanDeactivate() throws Exception {
        user.setRole(Role.DRIVER);
        deactivate("{\"password\":\"CurrentPassword@20\"}").andExpect(status().isOk());
        assertEquals(AccountStatus.INACTIVE, user.getStatus());
    }

    @Test
    void incorrectPasswordIsUnauthorizedWithoutSaving() throws Exception {
        deactivate("{\"password\":\"IncorrectPassword@20\"}").andExpect(status().isUnauthorized());
        assertEquals(AccountStatus.ACTIVE, user.getStatus());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void blankMissingAndNullPasswordsAreBadRequests() throws Exception {
        deactivate("{\"password\":\"   \"}").andExpect(status().isBadRequest());
        deactivate("{}").andExpect(status().isBadRequest());
        deactivate("{\"password\":null}").andExpect(status().isBadRequest());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void missingAndInvalidTokensAreUnauthorized() throws Exception {
        mvc.perform(patch("/api/accounts/me/deactivate").contentType("application/json")
                .content("{\"password\":\"CurrentPassword@20\"}"))
                .andExpect(status().isUnauthorized());
        token = "invalid-test-token";
        deactivate("{\"password\":\"CurrentPassword@20\"}").andExpect(status().isUnauthorized());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void adminAndBlockedAccountsAreForbidden() throws Exception {
        user.setRole(Role.ADMIN);
        deactivate("{\"password\":\"CurrentPassword@20\"}").andExpect(status().isForbidden());
        user.setRole(Role.PASSENGER);
        user.setStatus(AccountStatus.BLOCKED);
        deactivate("{\"password\":\"CurrentPassword@20\"}").andExpect(status().isForbidden());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void missingAccountIsNotFoundOnlyForNewEndpoint() throws Exception {
        when(repository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        deactivate("{\"password\":\"CurrentPassword@20\"}").andExpect(status().isNotFound());
        mvc.perform(get("/api/accounts/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        verify(repository, never()).save(any(User.class));
    }

    private org.springframework.test.web.servlet.ResultActions deactivate(String body) throws Exception {
        return mvc.perform(patch("/api/accounts/me/deactivate")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json").content(body));
    }
}
