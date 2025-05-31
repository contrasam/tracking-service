package com.logistics.tracking_service.domain;

public class InvalidTrackingNumberVersionException extends Throwable {
    public InvalidTrackingNumberVersionException(String version) {
        super("Invalid tracking number version: " + version + ". It must match the pattern 'TP' followed by a single digit (e.g., TP1, TP2, etc.).");
    }
}
