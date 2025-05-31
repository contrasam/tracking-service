package com.logistics.tracking_service.domain;

/**
 * Exception thrown when an invalid tracking number is encountered.
 * <p>
 * This exception is thrown when attempting to create a tracking number
 * that does not conform to the required format (alphanumeric, uppercase,
 * maximum 16 characters).
 * </p>
 *
 * @since 1.0
 */
public class InvalidTrackingNumberException extends Throwable {
    
    /**
     * Constructs a new InvalidTrackingNumberException with a detailed message.
     *
     * @param id the invalid tracking number that caused this exception
     */
    public InvalidTrackingNumberException(String id) {
        super("Invalid tracking number: " + id + ". It must be alphanumeric and up to 16 characters long.");
    }
}
