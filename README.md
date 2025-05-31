# Tracking Service

A reactive Spring Boot service for generating and managing tracking numbers for logistics operations.

## Tracking Number Generation Strategy

The tracking service implements a sophisticated strategy for generating unique, validated tracking numbers with the following characteristics:

### Tracking Number Format

Each tracking number follows this format: `TP1XXXX########X` where:

- `TP1`: Fixed prefix indicating the tracking service version
- `XXXX`: 4-character shipper code derived from the customer's slug/name
- `########`: 8-digit sequential number from the database sequence
- `X`: Check digit for validation

### Generation Process

1. **Customer Identification**:
   - The shipper code is derived from the customer's slug (kebab-case name)
   - First 4 characters of the slug are extracted, converted to uppercase
   - If less than 4 characters, it's padded with 'X' characters

2. **Sequence Generation**:
   - Uses an H2 database sequence (`tracking_sequence`) to ensure uniqueness
   - The sequence generates an incremental 8-digit number

3. **Validation**:
   - Each tracking number includes a check digit for validation
   - The system validates country codes against ISO 3166-1 alpha-2 format
   - Weight must be a positive number with up to three decimal places
   - Customer information (ID, name, slug) is validated

4. **Storage**:
   - Tracking numbers are stored in the database with metadata including:
     - Origin/destination country codes
     - Package weight
     - Creation timestamp
     - Customer information

### Reactive Implementation

The service is built using Spring WebFlux and R2DBC for full reactive programming:

- Non-blocking database operations using R2DBC
- Reactive endpoints returning `Mono<TrackingNumberEntity>`
- Comprehensive error handling with custom exception types
- Reactive streams for efficient resource utilization

## API Endpoints

### Generate Tracking Number

```
GET /next-tracking-number
```

Query Parameters:
- `origin_country_id` - ISO 3166-1 alpha-2 country code
- `destination_country_id` - ISO 3166-1 alpha-2 country code
- `weight` - Package weight in kilograms (positive number)
- `created_at` - RFC 3339 timestamp
- `customer_id` - UUID string
- `customer_name` - Customer name
- `customer_slug` - Customer name in slug-case/kebab-case

### Get Tracking Number Details

```
GET /tracking/{trackingNumber}
```

Path Parameter:
- `trackingNumber` - The tracking number to retrieve

## Running the Application

### Prerequisites

- Java 21 or higher
- Gradle

### Build and Run

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd tracking-service
   ```

2. Build the application:
   ```bash
   ./gradlew build
   ```

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

The application will start on port 8080 by default.

### Sample Request

```bash
curl -X GET "http://localhost:8080/next-tracking-number?origin_country_id=US&destination_country_id=CA&weight=10.5&created_at=2025-05-31T12%3A00%3A00Z&customer_id=123e4567-e89b-12d3-a456-426614174000&customer_name=Acme%20Corporation&customer_slug=acme-corporation"
```

## Database Schema

The application uses an H2 in-memory database with the following schema:

```sql
-- Create tracking sequence
CREATE SEQUENCE IF NOT EXISTS tracking_sequence START WITH 1 INCREMENT BY 1;

-- Create tracking_number table
CREATE TABLE IF NOT EXISTS tracking_number (
    id VARCHAR(255) PRIMARY KEY,
    shipper_code VARCHAR(4) NOT NULL,
    tracking_number VARCHAR(16) NOT NULL,
    created_at VARCHAR(30) NOT NULL,
    weight DOUBLE,
    source_country_code VARCHAR(2),
    destination_country_code VARCHAR(2)
);
```

## Error Handling

The service includes comprehensive error handling with specific error codes for various validation failures:
- Invalid country codes
- Invalid weight
- Invalid customer information
- Date format errors
- Database errors

All errors are returned with appropriate HTTP status codes and detailed error messages.

## Technologies Used

- Spring Boot 3.5.0
- Spring WebFlux (Reactive Web)
- Spring Data R2DBC
- H2 Database (in-memory)
- Java 21
- Gradle
- Lombok

## Performance Test Results

The following performance test was conducted on a MacBook Pro using wrk HTTP benchmarking tool:

```
Duration: 30s
Threads: 4
Connections: 100
URL: http://localhost:8080
Using script: tracking_number_perf_test.lua

Running 30s test @ http://localhost:8080
  4 threads and 100 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    18.50ms   29.77ms 403.95ms   96.70%
    Req/Sec     1.58k   339.14     2.06k    87.88%
  186546 requests in 30.04s, 46.97MB read
Requests/sec:   6209.03
Transfer/sec:      1.56MB
---------------------------------------------
Test Summary:
  Total Requests: 186546
  Total Duration: 30.04 seconds
  Requests/sec: 6209.03
  Mean Latency: 18.50 ms
  Max Latency: 403.95 ms
---------------------------------------------
```

These results demonstrate the high performance capabilities of the reactive architecture:
- Processing over 6,200 requests per second
- Average latency of just 18.5ms
- Handling 100 concurrent connections with only 4 threads
- Efficient memory usage with only 1.56MB/sec transfer rate

