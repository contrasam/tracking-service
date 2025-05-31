package com.logistics.tracking_service.domain;

import java.util.Objects;

public class TrackingNumber {

    private final String id;

    private TrackingNumber(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static TrackingNumber of(String id) throws InvalidTrackingNumberException {
        Objects.requireNonNull(id, "Tracking number cannot be null.");
        if (!id.matches("^[A-Z0-9]{1,16}$")) {
            throw new InvalidTrackingNumberException(id);
        }
        return new TrackingNumber(id);
    }
}
