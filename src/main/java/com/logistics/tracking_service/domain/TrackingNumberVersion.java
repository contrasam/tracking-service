package com.logistics.tracking_service.domain;

public class TrackingNumberVersion {
    private final String version;

    private TrackingNumberVersion(String version){
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

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
