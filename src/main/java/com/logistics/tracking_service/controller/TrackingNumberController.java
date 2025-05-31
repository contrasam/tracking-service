package com.logistics.tracking_service.controller;

import com.logistics.tracking_service.domain.TrackingNumberEntity;
import com.logistics.tracking_service.exception.TrackingServiceException;
import com.logistics.tracking_service.service.TrackingNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TrackingNumberController {

    private final TrackingNumberService trackingNumberService;

    /**
     * Creates a new tracking number based on the specified parameters.
     * 
     * @param originCountryId The order's origin country code (ISO 3166-1 alpha-2)
     * @param destinationCountryId The order's destination country code (ISO 3166-1 alpha-2)
     * @param weight The order's weight in kilograms
     * @param createdAtStr The order's creation timestamp in RFC 3339 format
     * @param customerIdStr The customer's UUID
     * @param customerName The customer's name
     * @param customerSlug The customer's name in slug-case/kebab-case
     * @return The created tracking number entity
     */
    @GetMapping("/next-tracking-number")
    public Mono<TrackingNumberEntity> getNextTrackingNumber(
            @RequestParam("origin_country_id") String originCountryId,
            @RequestParam("destination_country_id") String destinationCountryId,
            @RequestParam Double weight,
            @RequestParam("created_at") String createdAtStr,
            @RequestParam("customer_id") String customerIdStr,
            @RequestParam("customer_name") String customerName,
            @RequestParam("customer_slug") String customerSlug) {
        
        try {
            // Parse the created_at timestamp
            OffsetDateTime createdAt = OffsetDateTime.parse(createdAtStr);
            
            // Parse the customer_id UUID
            UUID customerId = UUID.fromString(customerIdStr);
            
            return trackingNumberService.generateNextTrackingNumber(
                    originCountryId,
                    destinationCountryId,
                    weight,
                    createdAt,
                    customerId,
                    customerName,
                    customerSlug
            );
        } catch (DateTimeParseException e) {
            return Mono.error(new TrackingServiceException(
                "Invalid date format for created_at. Expected RFC 3339 format (e.g., 2018-11-20T19:29:32+08:00)",
                TrackingServiceException.ErrorCodes.INVALID_DATE_FORMAT,
                e
            ));
        } catch (IllegalArgumentException e) {
            return Mono.error(new TrackingServiceException(
                "Invalid UUID format for customer_id",
                TrackingServiceException.ErrorCodes.INVALID_CUSTOMER_ID,
                e
            ));
        }
    }

    /**
     * Retrieves a tracking number by its value
     *
     * @param trackingNumber The tracking number to retrieve
     * @return The tracking number entity
     */
    @GetMapping("/tracking/{trackingNumber}")
    public Mono<TrackingNumberEntity> getTrackingNumber(@PathVariable String trackingNumber) {
        return trackingNumberService.findByTrackingNumber(trackingNumber);
    }

    /**
     * Request class for creating a tracking number
     */
    public static class CreateTrackingNumberRequest {
        private String shipperCode;
        private Double weight;
        private String sourceCountryCode;
        private String destinationCountryCode;

        // Getters and setters
        public String getShipperCode() {
            return shipperCode;
        }

        public void setShipperCode(String shipperCode) {
            this.shipperCode = shipperCode;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        public String getSourceCountryCode() {
            return sourceCountryCode;
        }

        public void setSourceCountryCode(String sourceCountryCode) {
            this.sourceCountryCode = sourceCountryCode;
        }

        public String getDestinationCountryCode() {
            return destinationCountryCode;
        }

        public void setDestinationCountryCode(String destinationCountryCode) {
            this.destinationCountryCode = destinationCountryCode;
        }
    }
}
