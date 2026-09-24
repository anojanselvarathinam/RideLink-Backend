package com.ridelink.fare_payment_service.dto;

import java.math.BigDecimal;

/**
 * Request payload for POST /payments.
 * The payment status is never accepted from the client - it is derived by the service.
 */
public class PaymentRequest {

	private String fareId;
	private BigDecimal amount;
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
