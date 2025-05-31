package com.logistics.tracking_service.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackingNumberGeneratorTest {

    @Test
    void shouldBeAbleToGenerateAUniqueTrackingNumber() throws InvalidTrackingNumberVersionException, InvalidTrackingNumberException {
        TrackingNumberGenerator generator = new TrackingNumberGenerator(TrackingNumberVersion.of("TP1"));
        TrackingNumber trackingNumber = generator.generateNextTrackingNumber(ShipperCode.of("REDX"), 1L);
        assertEquals("TP1REDX000000017", trackingNumber.getId(),
                "Generated tracking number should match the expected format and value");
    }

    @Test
    void shouldBeAbleToValidateChecksumOfTrackingNumber() throws InvalidTrackingNumberVersionException, InvalidTrackingNumberException {
        TrackingNumberGenerator generator = new TrackingNumberGenerator(TrackingNumberVersion.of("TP1"));
        TrackingNumber trackingNumber = generator.generateNextTrackingNumber(ShipperCode.of("REDX"), 1L);
        boolean isValid = generator.validateChecksum(trackingNumber);
        assertEquals(true, isValid, "Tracking number checksum should be valid");
    }
}
