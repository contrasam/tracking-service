package com.logistics.tracking_service.domain;

/**
 * Exception thrown when an invalid tracking number version is encountered.
 * <p>
 * This exception is thrown when attempting to create a tracking number
 * with a version that does not conform to the required format. Valid versions
 * must match the pattern 'TP' followed by a single digit (e.g., TP1, TP2).
 * </p>
 *
 * @since 1.0
 */
public class InvalidTrackingNumberVersionException extends Throwable {
    
    /**
     * Constructs a new InvalidTrackingNumberVersionException with a detailed message.
     *
     * @param version the invalid tracking number version that caused this exception
     */
    public InvalidTrackingNumberVersionException(String version) {
        super("Invalid tracking number version: " + version + ". It must match the pattern 'TP' followed by a single digit (e.g., TP1, TP2, etc.).");
    }
}
