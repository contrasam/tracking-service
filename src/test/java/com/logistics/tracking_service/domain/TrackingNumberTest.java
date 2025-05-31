package com.logistics.tracking_service.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackingNumberTest {

    @Test
    void shouldValidateInvariants() throws InvalidTrackingNumberException {
        String trackingNumber = "TP1REDX000000011";
        TrackingNumber trackingNumber1 = TrackingNumber.of(trackingNumber);
        assertEquals(trackingNumber, trackingNumber1.getId(), "Tracking number should match the input value");
    }
}
