package com.logistics.tracking_service.domain;

import java.util.Objects;

/**
 * Value object representing a tracking number in the logistics system.
 * <p>
 * This class encapsulates the tracking number ID and provides validation
 * to ensure the tracking number follows the required format. It is immutable
 * and can only be created through the factory method {@link #of(String)}.
 * </p>
 * <p>
 * Valid tracking numbers must match the pattern ^[A-Z0-9]{1,16}$ (alphanumeric,
 * uppercase, maximum 16 characters).
 * </p>
 *
 * @since 1.0
 */
public class TrackingNumber {

    /**
     * The tracking number identifier.
     */
    private final String id;

    /**
     * Private constructor to enforce creation through factory method.
     *
     * @param id the tracking number identifier
     */
    private TrackingNumber(String id) {
        this.id = id;
    }

    /**
     * Returns the tracking number identifier.
     *
     * @return the tracking number identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Factory method to create a new TrackingNumber instance.
     * <p>
     * Validates that the provided ID is not null and matches the required format.
     * </p>
     *
     * @param id the tracking number identifier to validate
     * @return a new TrackingNumber instance
     * @throws InvalidTrackingNumberException if the tracking number format is invalid
     * @throws NullPointerException if the tracking number is null
     */
    public static TrackingNumber of(String id) throws InvalidTrackingNumberException {
        Objects.requireNonNull(id, "Tracking number cannot be null.");
        if (!id.matches("^[A-Z0-9]{1,16}$")) {
            throw new InvalidTrackingNumberException(id);
        }
        return new TrackingNumber(id);
    }
}
