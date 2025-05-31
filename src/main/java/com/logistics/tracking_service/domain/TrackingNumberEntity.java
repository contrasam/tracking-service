package com.logistics.tracking_service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Sequence;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entity class representing a tracking number record in the database.
 * <p>
 * This entity maps to the tracking_number table and contains all the information
 * related to a generated tracking number, including its unique identifier, shipper code,
 * creation timestamp, package weight, and source/destination country codes.
 * </p>
 * <p>
 * The ID is generated using a database sequence called "tracking_sequence".
 * </p>
 *
 * @since 1.0
 */
@AllArgsConstructor
@Getter
@Setter
@Table("tracking_number")
public class TrackingNumberEntity {

    /**
     * The unique identifier for the tracking number.
     * Generated from a database sequence.
     */
    @Id
    @Sequence(value = "tracking_sequence")
    private String id;

    /**
     * The 4-character shipper code derived from the customer's name.
     */
    private String shipperCode;

    /**
     * The full tracking number string.
     */
    private String trackingNumber;

    /**
     * The timestamp when the tracking number was created.
     * Stored in ISO date-time format.
     */
    private String createdAt;

    /**
     * The weight of the package in kilograms.
     */
    private Double weight;

    /**
     * The ISO 3166-1 alpha-2 country code for the package's origin.
     */
    private String sourceCountryCode;

    /**
     * The ISO 3166-1 alpha-2 country code for the package's destination.
     */
    private String destinationCountryCode;

}
