package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.entity.Payment;
import com.ridelink.fare_payment_service.exception.ConflictException;
import com.ridelink.fare_payment_service.exception.ResourceNotFoundException;
import com.ridelink.fare_payment_service.exception.ValidationException;
import com.ridelink.fare_payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PaymentService} with MOCKED collaborators.
 *
 * Covers the documented payment lifecycle (PENDING -&gt; COMPLETED | FAILED),
 * the three validation rules before money moves, and the golden rule:
 * a FAILED payment never receives a receipt.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private PaymentSimulator paymentSimulator;

	@Mock
	private ReceiptService receiptService;

	@Mock
	private FareService fareService;

	@InjectMocks
	private PaymentService paymentService;

	// --- normal behaviour ---

	@Test
	void processPayment_gatewayApproves_completesPaymentAndIssuesReceipt() {
		Fare fare = confirmedFare("p1", "RIDE-IT4-TEST-PAY-OK", new BigDecimal("1500.00"));
		when(fareService.findById("p1")).thenReturn(Optional.of(fare));
		when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(paymentSimulator.authorize(any(), any())).thenReturn(true);

		PaymentRequest request = new PaymentRequest();
		request.setFareId("p1");
		request.setAmount(new BigDecimal("1500.00"));
		request.setMethod("cash"); // lower case input must be normalised

		Payment result = paymentService.processPayment(request);

		assertEquals(Payment.STATUS_COMPLETED, result.getStatus());
		assertEquals("p1", result.getFareId());
		assertEquals(new BigDecimal("1500.00"), result.getAmount());
		// gateway was asked with the normalised method
		verify(paymentSimulator).authorize(new BigDecimal("1500.00"), "CASH");
		// approved payment -> receipt IS issued for that exact fare
		verify(receiptService).issue(any(Payment.class), eq(fare));
	}

	// --- failure behaviour ---

	@Test
	void processPayment_gatewayDeclines_marksFailedAndIssuesNoReceipt() {
		Fare fare = confirmedFare("p2", "RIDE-IT4-TEST-PAY-FAIL", new BigDecimal("60000.00"));
		when(fareService.findById("p2")).thenReturn(Optional.of(fare));
		when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(paymentSimulator.authorize(any(), any())).thenReturn(false);

		PaymentRequest request = new PaymentRequest();
		request.setFareId("p2");
		request.setAmount(new BigDecimal("60000.00"));
		request.setMethod("CARD");

		Payment result = paymentService.processPayment(request);

		assertEquals(Payment.STATUS_FAILED, result.getStatus());
		// documented rule: no receipt for failed payments (receipts are
		// only ever issued by a successful payment - they cannot be forged)
		verify(receiptService, never()).issue(any(), any());
	}

	@Test
	void processPayment_unknownFare_throwsResourceNotFoundAndSavesNothing() {
		when(fareService.findById("missing")).thenReturn(Optional.empty());
		PaymentRequest request = paymentRequest("missing", new BigDecimal("1500.00"), "CASH");

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
			() -> paymentService.processPayment(request));

		assertEquals("Fare not found: missing", exception.getMessage());
		verify(paymentRepository, never()).save(any(Payment.class));
	}

	@Test
	void processPayment_fareNotConfirmedYet_throwsConflictAndSavesNothing() {
		Fare fare = new Fare("RIDE-IT4-TEST-PAY-EARLY", new BigDecimal("1500.00"), Fare.STATUS_ESTIMATED);
		fare.setId("p3");
		when(fareService.findById("p3")).thenReturn(Optional.of(fare));
		PaymentRequest request = paymentRequest("p3", new BigDecimal("1500.00"), "CASH");

		ConflictException exception = assertThrows(ConflictException.class,
			() -> paymentService.processPayment(request));

		assertEquals("Fare must be finalized (CONFIRMED) before payment", exception.getMessage());
		verify(paymentRepository, never()).save(any(Payment.class));
	}

	@Test
	void processPayment_amountDoesNotMatchFare_throwsValidationAndSavesNothing() {
		Fare fare = confirmedFare("p4", "RIDE-IT4-TEST-PAY-MISMATCH", new BigDecimal("1500.00"));
		when(fareService.findById("p4")).thenReturn(Optional.of(fare));
		PaymentRequest request = paymentRequest("p4", new BigDecimal("1499.99"), "CASH");

		ValidationException exception = assertThrows(ValidationException.class,
			() -> paymentService.processPayment(request));

		assertEquals("amount must match the confirmed fare amount of LKR 1500.00",
			exception.getMessage());
		verify(paymentRepository, never()).save(any(Payment.class));
	}

	// --- helpers ---

	private Fare confirmedFare(String id, String rideId, BigDecimal amount) {
		Fare fare = new Fare(rideId, amount, Fare.STATUS_CONFIRMED);
		fare.setId(id);
		return fare;
	}

	private PaymentRequest paymentRequest(String fareId, BigDecimal amount, String method) {
		PaymentRequest request = new PaymentRequest();
		request.setFareId(fareId);
		request.setAmount(amount);
		request.setMethod(method);
		return request;
	}
}
