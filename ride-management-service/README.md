# Ride Management Service

## Overview

Ride Management Service handles RideLink ride requests, pickup and destination details, driver assignment, ride lifecycle updates, cancellation, retrieval, and a compatibility endpoint for Fare & Payment Service.

## Technology Stack

- Java 21
- Spring Boot 4.1.1
- Maven
- MongoDB

## Configuration

Default service port:

```text
8082
```

Default MongoDB database:

```text
ridelink_ride_db
```

MongoDB connection property:

```properties
spring.data.mongodb.uri=${RIDE_MANAGEMENT_MONGODB_URI:mongodb://localhost:27017/ridelink_ride_db}
```

Set `RIDE_MANAGEMENT_MONGODB_URI` to override the local MongoDB URI.

## Prerequisites

- Java 21
- MongoDB Community Edition

## Run Locally

From this directory on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Run Tests

From this directory on Windows:

```powershell
.\mvnw.cmd test
```

The current test suite has 49 tests covering domain lifecycle rules, service logic, controller APIs, validation, negative scenarios, Fare & Payment compatibility, and Mongo-independent Spring context loading.

## API Documentation

Swagger UI:

[http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)

OpenAPI JSON:

[http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)

## Postman

Postman collection and local environment:

- `../postman/RideLink.postman_collection.json`
- `../postman/RideLink.local.postman_environment.json`

The collection includes a Ride Management happy path, negative scenarios, and Fare & Payment compatibility checks.

## Ride Lifecycle

Standard lifecycle:

```text
REQUESTED
-> ASSIGNED
-> ACCEPTED
-> IN_PROGRESS
-> COMPLETED
```

Cancellation is allowed from:

- `REQUESTED`
- `ASSIGNED`
- `ACCEPTED`

Cancellation is rejected after a ride reaches `IN_PROGRESS`, `COMPLETED`, or `CANCELLED`.

## Endpoint Summary

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/rides` | Create a ride request |
| `GET` | `/api/rides` | List rides, optionally filtered by passenger, driver, or status |
| `GET` | `/api/rides/{rideId}` | Retrieve one ride |
| `PATCH` | `/api/rides/{rideId}/assign-driver` | Assign a driver ID to a requested ride |
| `PATCH` | `/api/rides/{rideId}/accept` | Move an assigned ride to accepted |
| `PATCH` | `/api/rides/{rideId}/start` | Move an accepted ride to in progress |
| `PATCH` | `/api/rides/{rideId}/complete` | Complete an in-progress ride and store actual distance |
| `PATCH` | `/api/rides/{rideId}/cancel` | Cancel a ride when cancellation is allowed |
| `GET` | `/rides/{rideId}` | Fare & Payment compatibility endpoint |

`GET /rides/{rideId}` is a compatibility endpoint for Fare & Payment Service. It returns:

- `rideId`
- `status`
- `distanceKm`

For completed rides, `distanceKm` maps from the stored `actualDistance`.

## Validation and Errors

The service returns structured error responses for common API failures:

- `400` validation or malformed request errors
- `404` ride not found
- `409` invalid lifecycle transition

Validation covers required passenger ID, pickup and destination details, driver ID, cancellation reason, valid coordinates, and positive actual distance.

## Security Status

Current Ride Management APIs use a temporary permit-all security configuration because the Account Service JWT contract is not finalized.

Before final integrated submission, this should be replaced with JWT authentication and role-based authorization once the Account Service contract is available.

## Integration Dependencies

Driver & Vehicle integration is pending because no usable Driver & Vehicle API contract currently exists.

The current `assign-driver` endpoint accepts an externally supplied `driverId`. Future integration should validate:

- driver existence
- availability
- eligibility or service area
- busy or available state

No Driver & Vehicle API paths are assumed by this service yet.

## Fare & Payment Integration

Fare & Payment Service can call:

```http
GET /rides/{rideId}
```

This returns the ride projection needed for fare finalization. The response maps Ride Management `actualDistance` to `distanceKm`.

## Known Limitations

- Driver availability and eligibility are not validated until the Driver & Vehicle Service contract is available.
- Authentication and role-based authorization are pending the Account Service JWT contract.
- Fare calculation and payment processing are owned by Fare & Payment Service; Ride Management only exposes ride distance/status data for compatibility.
