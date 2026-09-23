package com.ridelink.fare_payment_service.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "receipts")
public class Receipt {

	@Id
	private String id;
	private String paymentId;
	private Instant issuedAt;
	private String details;

	public Receipt() {
	}

	public Receipt(String paymentId, Instant issuedAt, String details) {
		this.paymentId = paymentId;
		this.issuedAt = issuedAt;
		this.details = details;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public Instant getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(Instant issuedAt) {
		this.issuedAt = issuedAt;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
}
