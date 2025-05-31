package com.logistics.tracking_service.domain;

import java.util.Objects;

/**
 * Value object representing a shipper code in the logistics system.
 * <p>
 * A shipper code is a unique identifier for a customer or shipper,
 * typically derived from the first 4 characters of the customer's name.
 * This class encapsulates the shipper code and ensures it cannot be
 * modified after creation.
 * </p>
 * <p>
 * Instances of this class can only be created through the factory method
 * {@link #of(String)}.
 * </p>
 *
 * @since 1.0
 */
public class ShipperCode {
    /**
     * The shipper code value.
     */
    private final String code;

    /**
     * Private constructor to enforce creation through factory method.
     *
     * @param code the shipper code value
     */
    private ShipperCode(String code) {
        this.code = code;
    }

    /**
     * Factory method to create a new ShipperCode instance.
     * <p>
     * Validates that the provided code is not null.
     * </p>
     *
     * @param code the shipper code value
     * @return a new ShipperCode instance
     * @throws NullPointerException if the code is null
     */
    public static ShipperCode of(String code) {
        Objects.requireNonNull(code, "Shipper code cannot be null.");
        return new ShipperCode(code);
    }

    /**
     * Returns the shipper code value.
     *
     * @return the shipper code value
     */
    public String getCode() {
        return code;
    }
}
