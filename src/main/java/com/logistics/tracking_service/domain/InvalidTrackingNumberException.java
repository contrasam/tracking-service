package com.logistics.tracking_service.domain;

public class InvalidTrackingNumberException extends Throwable {
    public InvalidTrackingNumberException(String id) {
        super("Invalid tracking number: " + id + ". It must be alphanumeric and up to 16 characters long.");
    }
}
