package com.ridelink.fare_payment_service.dto;

import java.time.Instant;

/**
 * Uniform error body returned for EVERY failed request (validation errors,
 * 404, 409, 405, malformed JSON, ...), so clients always receive the same
 * JSON shape regardless of what went wrong.
 */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
}
