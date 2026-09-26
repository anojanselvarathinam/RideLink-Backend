package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.entity.Payment;
import com.ridelink.fare_payment_service.entity.Receipt;
import com.ridelink.fare_payment_service.repository.ReceiptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReceiptService} - receipts are generated automatically
 * from a COMPLETED payment (there is no creation endpoint, so this service is
 * the single source of receipts).
 */
@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

	@Mock
	private ReceiptRepository receiptRepository;

	@InjectMocks
	private ReceiptService receiptService;

	@Test
	void issue_completedPayment_createsReceiptWithPaymentIdAndDetails() {
		Payment payment = new Payment("RIDE-IT4-RCPT-F1", new BigDecimal("1500.00"),
			"CASH", Payment.STATUS_COMPLETED);
		payment.setId("PAY-IT4-1");
		Fare fare = new Fare("RIDE-IT4-RCPT-1", new BigDecimal("1500.00"), Fare.STATUS_CONFIRMED);
		fare.setPickupLocation("Colombo Fort");
		fare.setDestinationLocation("Kandy");
		when(receiptRepository.save(any(Receipt.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		Receipt receipt = receiptService.issue(payment, fare);

		assertEquals("PAY-IT4-1", receipt.getPaymentId());
		assertNotNull(receipt.getIssuedAt());
		assertEquals("Ride RIDE-IT4-RCPT-1 | Colombo Fort -> Kandy | CASH payment of LKR 1500.00",
			receipt.getDetails());
		verify(receiptRepository).save(any(Receipt.class));
	}
}
