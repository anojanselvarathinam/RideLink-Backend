package com.ridelink.ride_management_service.service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.ridelink.ride_management_service.entity.RideStatus;

public final class RideLifecycleValidator {

	private static final Map<RideStatus, Set<RideStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(RideStatus.class);

	static {
		ALLOWED_TRANSITIONS.put(RideStatus.REQUESTED, EnumSet.of(RideStatus.ASSIGNED, RideStatus.CANCELLED));
		ALLOWED_TRANSITIONS.put(RideStatus.ASSIGNED, EnumSet.of(RideStatus.ACCEPTED, RideStatus.CANCELLED));
		ALLOWED_TRANSITIONS.put(RideStatus.ACCEPTED, EnumSet.of(RideStatus.IN_PROGRESS, RideStatus.CANCELLED));
		ALLOWED_TRANSITIONS.put(RideStatus.IN_PROGRESS, EnumSet.of(RideStatus.COMPLETED));
		ALLOWED_TRANSITIONS.put(RideStatus.COMPLETED, EnumSet.noneOf(RideStatus.class));
		ALLOWED_TRANSITIONS.put(RideStatus.CANCELLED, EnumSet.noneOf(RideStatus.class));
	}

	private RideLifecycleValidator() {
	}

	public static boolean canTransition(RideStatus currentStatus, RideStatus nextStatus) {
		if (currentStatus == null || nextStatus == null) {
			return false;
		}
		return ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(nextStatus);
	}

	public static void validateTransition(RideStatus currentStatus, RideStatus nextStatus) {
		if (!canTransition(currentStatus, nextStatus)) {
			throw new IllegalStateException("Invalid ride status transition from " + currentStatus + " to " + nextStatus);
		}
	}
}
