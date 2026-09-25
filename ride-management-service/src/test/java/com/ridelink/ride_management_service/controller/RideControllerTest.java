package com.ridelink.ride_management_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.ridelink.ride_management_service.dto.CreateRideRequest;
import com.ridelink.ride_management_service.dto.LocationResponse;
import com.ridelink.ride_management_service.dto.RideResponse;
import com.ridelink.ride_management_service.entity.RideStatus;
import com.ridelink.ride_management_service.exception.InvalidRideStateException;
import com.ridelink.ride_management_service.exception.RideApiExceptionHandler;
import com.ridelink.ride_management_service.exception.RideNotFoundException;
import com.ridelink.ride_management_service.service.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class RideControllerTest {

	private RideService rideService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		rideService = mock(RideService.class);
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders
			.standaloneSetup(new RideController(rideService))
			.setControllerAdvice(new RideApiExceptionHandler())
			.setValidator(validator)
			.build();
	}

	@Test
	void postValidRequestReturnsCreated() throws Exception {
		when(rideService.createRide(any(CreateRideRequest.class))).thenReturn(sampleResponse("ride-1"));

		mockMvc.perform(post("/api/rides")
				.contentType(MediaType.APPLICATION_JSON)
				.content(validCreateJson()))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", "/api/rides/ride-1"))
			.andExpect(jsonPath("$.id").value("ride-1"))
			.andExpect(jsonPath("$.passengerId").value("P001"))
			.andExpect(jsonPath("$.status").value("REQUESTED"))
			.andExpect(jsonPath("$.pickup.placeName").value("Colombo Fort"))
			.andExpect(jsonPath("$.destination.placeName").value("Bambalapitiya"));
	}

	@Test
	void postMissingPassengerIdReturnsBadRequest() throws Exception {
		assertBadCreateRequest("""
			{
			  "pickup": { "placeName": "Colombo Fort" },
			  "destination": { "placeName": "Bambalapitiya" }
			}
			""");
	}

	@Test
	void postMissingPickupReturnsBadRequest() throws Exception {
		assertBadCreateRequest("""
			{
			  "passengerId": "P001",
			  "destination": { "placeName": "Bambalapitiya" }
			}
			""");
	}

	@Test
	void postMissingDestinationReturnsBadRequest() throws Exception {
		assertBadCreateRequest("""
			{
			  "passengerId": "P001",
			  "pickup": { "placeName": "Colombo Fort" }
			}
			""");
	}

	@Test
	void postBlankPlaceNameReturnsBadRequest() throws Exception {
		assertBadCreateRequest("""
			{
			  "passengerId": "P001",
			  "pickup": { "placeName": "" },
			  "destination": { "placeName": "Bambalapitiya" }
			}
			""");
	}

	@Test
	void postInvalidLatitudeReturnsBadRequest() throws Exception {
		assertBadCreateRequest("""
			{
			  "passengerId": "P001",
			  "pickup": { "placeName": "Colombo Fort", "latitude": 91.0 },
			  "destination": { "placeName": "Bambalapitiya" }
			}
			""");
	}

	@Test
	void postInvalidLongitudeReturnsBadRequest() throws Exception {
		assertBadCreateRequest("""
			{
			  "passengerId": "P001",
			  "pickup": { "placeName": "Colombo Fort" },
			  "destination": { "placeName": "Bambalapitiya", "longitude": 181.0 }
			}
			""");
	}

	@Test
	void getExistingRideReturnsOk() throws Exception {
		when(rideService.getRideById("ride-1")).thenReturn(sampleResponse("ride-1"));

		mockMvc.perform(get("/api/rides/ride-1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value("ride-1"))
			.andExpect(jsonPath("$.status").value("REQUESTED"));
	}

	@Test
	void getMissingRideReturnsNotFound() throws Exception {
		when(rideService.getRideById("missing")).thenThrow(new RideNotFoundException("missing"));

		mockMvc.perform(get("/api/rides/missing"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("RIDE_NOT_FOUND"))
			.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void getRideListReturnsOk() throws Exception {
		when(rideService.getRides(null, null, null)).thenReturn(List.of(sampleResponse("ride-1")));

		mockMvc.perform(get("/api/rides"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value("ride-1"));
	}

	@Test
	void invalidStatusQueryReturnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/rides").param("status", "UNKNOWN"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
	}

	@Test
	void assignDriverSuccessReturnsOk() throws Exception {
		when(rideService.assignDriver(any(), any())).thenReturn(sampleResponse("ride-1", RideStatus.ASSIGNED));

		mockMvc.perform(patch("/api/rides/ride-1/assign-driver")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"driverId\":\"D001\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ASSIGNED"));
	}

	@Test
	void assignDriverInvalidStateReturnsConflict() throws Exception {
		when(rideService.assignDriver(any(), any()))
			.thenThrow(new InvalidRideStateException("Invalid ride status transition from ASSIGNED to ASSIGNED"));

		mockMvc.perform(patch("/api/rides/ride-1/assign-driver")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"driverId\":\"D001\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error").value("INVALID_RIDE_STATE"));
	}

	@Test
	void acceptSuccessReturnsOk() throws Exception {
		when(rideService.acceptRide("ride-1")).thenReturn(sampleResponse("ride-1", RideStatus.ACCEPTED));

		mockMvc.perform(patch("/api/rides/ride-1/accept"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ACCEPTED"));
	}

	@Test
	void startSuccessReturnsOk() throws Exception {
		when(rideService.startRide("ride-1")).thenReturn(sampleResponse("ride-1", RideStatus.IN_PROGRESS));

		mockMvc.perform(patch("/api/rides/ride-1/start"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"));
	}

	@Test
	void completeSuccessReturnsOk() throws Exception {
		when(rideService.completeRide(any(), any())).thenReturn(sampleResponse("ride-1", RideStatus.COMPLETED));

		mockMvc.perform(patch("/api/rides/ride-1/complete")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"actualDistance\":8.5}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("COMPLETED"))
			.andExpect(jsonPath("$.actualDistance").value(8.5));
	}

	@Test
	void cancelSuccessReturnsOk() throws Exception {
		when(rideService.cancelRide(any(), any())).thenReturn(sampleResponse("ride-1", RideStatus.CANCELLED));

		mockMvc.perform(patch("/api/rides/ride-1/cancel")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"reason\":\"Passenger changed plans\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CANCELLED"));
	}

	@Test
	void invalidLifecycleOperationReturnsConflict() throws Exception {
		when(rideService.startRide("ride-1"))
			.thenThrow(new InvalidRideStateException("Invalid ride status transition from REQUESTED to IN_PROGRESS"));

		mockMvc.perform(patch("/api/rides/ride-1/start"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error").value("INVALID_RIDE_STATE"))
			.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void actionOnMissingRideReturnsNotFound() throws Exception {
		when(rideService.startRide("missing")).thenThrow(new RideNotFoundException("missing"));

		mockMvc.perform(patch("/api/rides/missing/start"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("RIDE_NOT_FOUND"));
	}

	@Test
	void lifecycleInvalidRequestBodyReturnsBadRequest() throws Exception {
		mockMvc.perform(patch("/api/rides/ride-1/complete")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"actualDistance\":0}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
	}

	private void assertBadCreateRequest(String json) throws Exception {
		mockMvc.perform(post("/api/rides")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.status").value(400));
		verifyNoInteractions(rideService);
	}

	private static String validCreateJson() {
		return """
			{
			  "passengerId": "P001",
			  "pickup": {
			    "placeName": "Colombo Fort",
			    "latitude": 6.9344,
			    "longitude": 79.8428
			  },
			  "destination": {
			    "placeName": "Bambalapitiya",
			    "latitude": 6.8936,
			    "longitude": 79.8563
			  }
			}
			""";
	}

	private static RideResponse sampleResponse(String id) {
		return sampleResponse(id, RideStatus.REQUESTED);
	}

	private static RideResponse sampleResponse(String id, RideStatus status) {
		Instant requestedAt = Instant.parse("2026-09-25T04:00:00Z");
		return new RideResponse(
			id,
			"P001",
			status == RideStatus.REQUESTED ? null : "D001",
			new LocationResponse("Colombo Fort", 6.9344, 79.8428),
			new LocationResponse("Bambalapitiya", 6.8936, 79.8563),
			status,
			new BigDecimal("350.00"),
			null,
			status == RideStatus.COMPLETED ? 8.5 : null,
			status == RideStatus.CANCELLED ? "Passenger changed plans" : null,
			requestedAt,
			status.ordinal() >= RideStatus.ASSIGNED.ordinal() ? requestedAt : null,
			status.ordinal() >= RideStatus.ACCEPTED.ordinal() ? requestedAt : null,
			status == RideStatus.IN_PROGRESS || status == RideStatus.COMPLETED ? requestedAt : null,
			status == RideStatus.COMPLETED ? requestedAt : null,
			status == RideStatus.CANCELLED ? requestedAt : null,
			requestedAt,
			requestedAt
		);
	}
}
