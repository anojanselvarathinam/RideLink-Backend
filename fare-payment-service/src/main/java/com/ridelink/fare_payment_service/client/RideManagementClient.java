package com.ridelink.fare_payment_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Synchronous REST client for the Ride Management Service (Member 3).
 *
 * Used when a fare is finalized: a completed ride knows its ACTUAL travelled
 * distance, which is the authoritative input for the final fare calculation
 * (workflow 5 -&gt; 6 boundary). The fare is recalculated from that value.
 *
 * Communication choice: synchronous REST (JSON over HTTP).
 * Justification: the final distance is required BEFORE the finalize response
 * can be produced, so an immediate request/response fits the context; an
 * asynchronous queue would only add latency and infrastructure for a single
 * read of an existing resource.
 *
 * Expected contract (documented for the ride-management team):
 * <pre>
 * GET {base-url}/rides/{rideId}
 * 200 -&gt; { "id": "...", "rideId": "...", "status": "COMPLETED", "distanceKm": 7.7 }
 * 404 -&gt; ride does not exist
 * </pre>
 *
 * Failure behaviour: every failure (service unreachable, ride unknown,
 * unusable payload) is logged and swallowed - the caller falls back to the
 * estimated distance, so a ride-management outage never blocks finalization.
 * No cross-service database access: the data is obtained through the public API.
 */
@Component
public class RideManagementClient {

	private static final Logger log = LoggerFactory.getLogger(RideManagementClient.class);

	private final RestClient restClient;
	private final String baseUrl;

	public RideManagementClient(@Value("${ride-management.base-url}") String baseUrl) {
		this.baseUrl = baseUrl;
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
	}

	/**
	 * Fetches the actual distance of a completed ride.
	 *
	 * @return the distance in km, or empty when it could not be obtained
	 *         (service down, ride unknown, unusable data)
	 */
	public Optional<Double> fetchActualDistanceKm(String rideId) {
		if (rideId == null || rideId.isBlank()) {
			return Optional.empty();
		}
		try {
			RideResponse ride = restClient.get()
				.uri("/rides/{rideId}", rideId)
				.retrieve()
				.body(RideResponse.class);
			if (ride == null || ride.distanceKm() == null || ride.distanceKm() <= 0) {
				log.warn("ride-management returned no usable distanceKm for ride {}", rideId);
				return Optional.empty();
			}
			return Optional.of(ride.distanceKm());
		} catch (RestClientException e) {
			log.warn("Could not fetch ride {} from ride-management at {} - falling back to estimated distance: {}",
				rideId, baseUrl, e.getMessage());
			return Optional.empty();
		}
	}

	/** Minimal projection of the ride-management /rides/{id} response. */
	public record RideResponse(String id, String rideId, String status, Double distanceKm) {
	}
}
