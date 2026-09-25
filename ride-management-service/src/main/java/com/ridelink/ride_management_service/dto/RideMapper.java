package com.ridelink.ride_management_service.dto;

import com.ridelink.ride_management_service.entity.Ride;
import com.ridelink.ride_management_service.entity.RideLocation;

public final class RideMapper {

	private RideMapper() {
	}

	public static RideResponse toResponse(Ride ride) {
		return new RideResponse(
			ride.getId(),
			ride.getPassengerId(),
			ride.getDriverId(),
			toLocationResponse(ride.getPickup()),
			toLocationResponse(ride.getDestination()),
			ride.getStatus(),
			ride.getEstimatedFare(),
			ride.getFinalFare(),
			ride.getActualDistance(),
			ride.getCancellationReason(),
			ride.getRequestedAt(),
			ride.getAssignedAt(),
			ride.getAcceptedAt(),
			ride.getStartedAt(),
			ride.getCompletedAt(),
			ride.getCancelledAt(),
			ride.getCreatedAt(),
			ride.getUpdatedAt()
		);
	}

	public static RideLocation toLocation(LocationRequest request) {
		return new RideLocation(request.placeName(), request.latitude(), request.longitude());
	}

	private static LocationResponse toLocationResponse(RideLocation location) {
		if (location == null) {
			return null;
		}
		return new LocationResponse(location.getPlaceName(), location.getLatitude(), location.getLongitude());
	}
}
