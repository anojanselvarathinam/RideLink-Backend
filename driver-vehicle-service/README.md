# Driver & Vehicle Service

Part of the RideLink microservices system. Owned by: Sujany George Stanislas.

## Responsibilities
- Driver operational profile and vehicle details
- Availability status management (ONLINE/OFFLINE)
- Simulated current location tracking
- Retrieval of eligible available drivers for ride assignment

## Tech Stack
- Java 21, Spring Boot 4.1.1
- Spring Data MongoDB
- Spring Security (temporary open config; JWT integration pending)
- Springdoc OpenAPI (Swagger UI)
- JUnit 5 + Mockito for unit testing

## Prerequisites
- Java 21
- Maven (wrapper included)
- MongoDB running on localhost:27017

## Configuration
Database config in src/main/resources/application.properties. URI: mongodb://localhost:27017/driver_db

## Running MongoDB (if not running as a Windows service)
& "C:\Program Files\MongoDB\Server\8.2\bin\mongod.exe" --dbpath "C:\Program Files\MongoDB\Server\8.2\data\db"

## Running the Service
.\mvnw.cmd spring-boot:run
Service runs on port 8082.

## API Documentation
Swagger UI: http://localhost:8082/swagger-ui.html

## Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/drivers | Create a driver |
| GET | /api/drivers | List all drivers |
| GET | /api/drivers/{id} | Get driver by ID |
| PATCH | /api/drivers/{id}/availability | Update availability status |
| PATCH | /api/drivers/{id}/location | Update current location |
| GET | /api/drivers/available | Get available drivers by service area |
| DELETE | /api/drivers/{id} | Delete a driver |

## Running Tests
.\mvnw.cmd test

## Sample Data
userId: 1, vehicleNumber: WP-CAB-1234, vehicleType: CAR, serviceArea: Jaffna
