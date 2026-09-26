package com.ridelink.fare_payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request payload for POST /payments.
 * The payment status is never accepted from the client - it is derived by
 * the service (PENDING -> COMPLETED or FAILED).
 */
public class PaymentRequest {

	@NotBlank(message = "fareId is required")
	private String fareId;

	@NotNull(message = "amount is required")
	@Positive(message = "amount must be greater than 0")
	private BigDecimal amount;

	@NotBlank(message = "method is required")
	@Pattern(regexp = "(?i)(CASH|CARD|WALLET)", message = "method must be one of CASH, CARD, WALLET")
	private String method;

	public PaymentRequest() {
	}

	public String getFareId() {
		return fareId;
	}

	public void setFareId(String fareId) {
		this.fareId = fareId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
}
