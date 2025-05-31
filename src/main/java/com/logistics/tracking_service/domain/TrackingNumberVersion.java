package com.logistics.tracking_service.domain;

/**
 * Value object representing a tracking number version in the logistics system.
 * <p>
 * The tracking number version follows a specific format: 'TP' followed by a single digit
 * (e.g., TP1, TP2). This class encapsulates the version and provides validation
 * to ensure it follows the required format. It is immutable and can only be created
 * through the factory method {@link #of(String)}.
 * </p>
 *
 * @since 1.0
 */
public class TrackingNumberVersion {
    /**
     * The tracking number version value.
     */
    private final String version;

    /**
     * Private constructor to enforce creation through factory method.
     *
     * @param version the tracking number version value
     */
    private TrackingNumberVersion(String version){
        this.version = version;
    }

    /**
     * Returns the tracking number version value.
     *
     * @return the tracking number version value
     */
    public String getVersion() {
        return version;
    }

    /**
     * Factory method to create a new TrackingNumberVersion instance.
     * <p>
     * Validates that the provided version is not null or empty and matches
     * the required format (TP followed by a single digit).
     * </p>
     *
     * @param version the tracking number version value
     * @return a new TrackingNumberVersion instance
     * @throws InvalidTrackingNumberVersionException if the version format is invalid
     * @throws IllegalArgumentException if the version is null or empty
     */
    public static TrackingNumberVersion of(String version) throws InvalidTrackingNumberVersionException {
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }
        if( !version.matches("^TP\\d$")) {
            throw new InvalidTrackingNumberVersionException(version);
        }
        return new TrackingNumberVersion(version);
    }
}
