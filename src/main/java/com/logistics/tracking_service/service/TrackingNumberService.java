package com.logistics.tracking_service.service;

import com.logistics.tracking_service.domain.*;
import com.logistics.tracking_service.exception.TrackingServiceException;
import com.logistics.tracking_service.repository.TrackingNumberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingNumberService {

    private final DatabaseClient databaseClient;
    private final TrackingNumberGenerator trackingNumberGenerator;
    private final TrackingNumberRepository trackingNumberRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final String DEFAULT_VERSION = "TP1";

    /**
     * Creates a new tracking number entity with a unique tracking number
     * generated using the tracking sequence.
     *
     * @param shipperCode The shipper code (must be 4 characters)
     * @param weight The package weight (optional)
     * @param sourceCountryCode The source country code (optional)
     * @param destinationCountryCode The destination country code (optional)
     * @return A Mono containing the created TrackingNumberEntity
     */
    public Mono<TrackingNumberEntity> createTrackingNumber(
            String shipperCode, 
            Double weight, 
            String sourceCountryCode, 
            String destinationCountryCode) {
        
        if (shipperCode == null || shipperCode.isEmpty()) {
            return Mono.error(new TrackingServiceException(
                "Shipper code cannot be null or empty", 
                TrackingServiceException.ErrorCodes.INVALID_SHIPPER_CODE));
        }
        
        return getNextSequenceValue()
                .onErrorMap(e -> {
                    log.error("Error getting next sequence value", e);
                    return new TrackingServiceException(
                        "Failed to generate sequence for tracking number", 
                        TrackingServiceException.ErrorCodes.SEQUENCE_ERROR, 
                        e);
                })
                .flatMap(sequenceValue -> generateTrackingNumber(
                    shipperCode, weight, sourceCountryCode, destinationCountryCode, sequenceValue));
    }
    
    /**
     * Creates a new tracking number based on the specified parameters.
     * 
     * @param originCountryId The order's origin country code (ISO 3166-1 alpha-2)
     * @param destinationCountryId The order's destination country code (ISO 3166-1 alpha-2)
     * @param weight The order's weight in kilograms
     * @param createdAt The order's creation timestamp
     * @param customerId The customer's UUID
     * @param customerName The customer's name
     * @param customerSlug The customer's name in slug-case/kebab-case
     * @return A Mono containing the created TrackingNumberEntity
     */
    public Mono<TrackingNumberEntity> generateNextTrackingNumber(
            String originCountryId,
            String destinationCountryId,
            Double weight,
            OffsetDateTime createdAt,
            UUID customerId,
            String customerName,
            String customerSlug) {
        
        // Validate inputs
        if (originCountryId == null || originCountryId.length() != 2) {
            return Mono.error(new TrackingServiceException(
                "Origin country code must be in ISO 3166-1 alpha-2 format", 
                TrackingServiceException.ErrorCodes.INVALID_COUNTRY_CODE));
        }
        
        if (destinationCountryId == null || destinationCountryId.length() != 2) {
            return Mono.error(new TrackingServiceException(
                "Destination country code must be in ISO 3166-1 alpha-2 format", 
                TrackingServiceException.ErrorCodes.INVALID_COUNTRY_CODE));
        }
        
        if (weight == null || weight <= 0) {
            return Mono.error(new TrackingServiceException(
                "Weight must be a positive number", 
                TrackingServiceException.ErrorCodes.INVALID_WEIGHT));
        }
        
        if (customerId == null) {
            return Mono.error(new TrackingServiceException(
                "Customer ID cannot be null", 
                TrackingServiceException.ErrorCodes.INVALID_CUSTOMER_ID));
        }
        
        if (customerName == null || customerName.isEmpty()) {
            return Mono.error(new TrackingServiceException(
                "Customer name cannot be null or empty", 
                TrackingServiceException.ErrorCodes.INVALID_CUSTOMER_NAME));
        }
        
        // Generate shipper code from customer slug (first 4 characters, uppercase)
        String shipperCode = generateShipperCodeFromSlug(customerSlug);
        
        // Format creation date
        String formattedCreatedAt = createdAt != null ? 
                createdAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : 
                OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        
        // Generate tracking number
        return getNextSequenceValue()
                .onErrorMap(e -> {
                    log.error("Error getting next sequence value", e);
                    return new TrackingServiceException(
                        "Failed to generate sequence for tracking number", 
                        TrackingServiceException.ErrorCodes.SEQUENCE_ERROR, 
                        e);
                })
                .flatMap(sequenceValue -> {
                    try {
                        ShipperCode sc = ShipperCode.of(shipperCode);
                        TrackingNumber trackingNumber = trackingNumberGenerator
                                .generateNextTrackingNumber(sc, sequenceValue);
                        
                        TrackingNumberEntity entity = new TrackingNumberEntity(
                                trackingNumber.getId(),
                                shipperCode,
                                trackingNumber.getId(),
                                formattedCreatedAt,
                                weight,
                                originCountryId,
                                destinationCountryId
                        );
                        
                        return saveTrackingNumberEntity(entity);
                    } catch (InvalidTrackingNumberException e) {
                        log.error("Error generating tracking number", e);
                        return Mono.error(new TrackingServiceException(
                            "Failed to generate tracking number", 
                            TrackingServiceException.ErrorCodes.TRACKING_NUMBER_GENERATION_ERROR, 
                            e));
                    }
                });
    }
    
    /**
     * Generates a shipper code from a customer slug.
     * Takes the first 4 characters of the slug and converts to uppercase.
     * If the slug is less than 4 characters, it pads with 'X'.
     */
    private String generateShipperCodeFromSlug(String customerSlug) {
        if (customerSlug == null || customerSlug.isEmpty()) {
            return "XXXX";
        }
        
        // Remove any hyphens and get first 4 characters
        String code = customerSlug.replace("-", "").toUpperCase();
        
        // Ensure it's exactly 4 characters
        if (code.length() >= 4) {
            return code.substring(0, 4);
        } else {
            // Pad with 'X' if less than 4 characters
            StringBuilder sb = new StringBuilder(code);
            while (sb.length() < 4) {
                sb.append('X');
            }
            return sb.toString();
        }
    }
    
    /**
     * Generates a tracking number entity based on the provided information and sequence value.
     */
    private Mono<TrackingNumberEntity> generateTrackingNumber(
            String shipperCode, 
            Double weight, 
            String sourceCountryCode, 
            String destinationCountryCode,
            Long sequenceValue) {
        
        try {
            ShipperCode sc = ShipperCode.of(shipperCode);
            TrackingNumber trackingNumber = trackingNumberGenerator
                    .generateNextTrackingNumber(sc, sequenceValue);
            
            String createdAt = LocalDateTime.now().format(DATE_FORMATTER);
            
            TrackingNumberEntity entity = new TrackingNumberEntity(
                    trackingNumber.getId(),
                    shipperCode,
                    trackingNumber.getId(),
                    createdAt,
                    weight,
                    sourceCountryCode,
                    destinationCountryCode
            );
            
            return saveTrackingNumberEntity(entity);
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument for tracking number generation", e);
            return Mono.error(new TrackingServiceException(
                e.getMessage(), 
                TrackingServiceException.ErrorCodes.INVALID_SHIPPER_CODE, 
                e));
        } catch (InvalidTrackingNumberException e) {
            log.error("Invalid tracking number generated", e);
            return Mono.error(new TrackingServiceException(
                "Failed to generate valid tracking number", 
                TrackingServiceException.ErrorCodes.TRACKING_NUMBER_GENERATION_ERROR, 
                e));
        } catch (Exception e) {
            log.error("Unexpected error during tracking number generation", e);
            return Mono.error(new TrackingServiceException(
                "Unexpected error during tracking number generation", 
                TrackingServiceException.ErrorCodes.TRACKING_NUMBER_GENERATION_ERROR, 
                e));
        }
    }
    
    /**
     * Saves a tracking number entity to the database.
     */
    private Mono<TrackingNumberEntity> saveTrackingNumberEntity(TrackingNumberEntity entity) {
        // Use insert query instead of save to avoid update attempts on non-existent records
        return databaseClient.sql("INSERT INTO tracking_number (id, shipper_code, tracking_number, created_at, weight, source_country_code, destination_country_code) " +
                "VALUES (:id, :shipperCode, :trackingNumber, :createdAt, :weight, :sourceCountryCode, :destinationCountryCode)")
                .bind("id", entity.getId())
                .bind("shipperCode", entity.getShipperCode())
                .bind("trackingNumber", entity.getTrackingNumber())
                .bind("createdAt", entity.getCreatedAt())
                .bind("weight", entity.getWeight())
                .bind("sourceCountryCode", entity.getSourceCountryCode())
                .bind("destinationCountryCode", entity.getDestinationCountryCode())
                .fetch()
                .rowsUpdated()
                .thenReturn(entity)
                .onErrorMap(e -> {
                    log.error("Error saving tracking number entity", e);
                    return new TrackingServiceException(
                        "Failed to save tracking number to database", 
                        TrackingServiceException.ErrorCodes.DATABASE_ERROR, 
                        e);
                })
                .doOnSuccess(savedEntity -> 
                    log.info("Successfully created tracking number: {}", savedEntity.getTrackingNumber()));
    }
    
    /**
     * Gets the next value from the tracking sequence.
     *
     * @return A Mono containing the next sequence value
     */
    private Mono<Long> getNextSequenceValue() {
        return databaseClient
                .sql("SELECT NEXT VALUE FOR tracking_sequence")
                .map(row -> row.get(0, Long.class))
                .one()
                .doOnSuccess(value -> log.debug("Generated sequence value: {}", value));
    }
    
    /**
     * Find a tracking number entity by its tracking number
     *
     * @param trackingNumber The tracking number to search for
     * @return A Mono containing the found entity, or an empty Mono if not found
     */
    public Mono<TrackingNumberEntity> findByTrackingNumber(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isEmpty()) {
            return Mono.error(new TrackingServiceException(
                "Tracking number cannot be null or empty", 
                TrackingServiceException.ErrorCodes.INVALID_SHIPPER_CODE));
        }
        
        return trackingNumberRepository.findByTrackingNumber(trackingNumber)
                .switchIfEmpty(Mono.error(new TrackingServiceException(
                    "Tracking number not found: " + trackingNumber, 
                    "TRACKING_NUMBER_NOT_FOUND")))
                .onErrorMap(e -> {
                    if (e instanceof TrackingServiceException) {
                        return e;
                    }
                    log.error("Error retrieving tracking number", e);
                    return new TrackingServiceException(
                        "Failed to retrieve tracking number from database", 
                        TrackingServiceException.ErrorCodes.DATABASE_ERROR, 
                        e);
                });
    }
}
