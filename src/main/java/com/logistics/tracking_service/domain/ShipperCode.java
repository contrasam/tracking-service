package com.logistics.tracking_service.domain;

import java.util.Objects;

public class ShipperCode {
    private final String code;

    private ShipperCode(String code) {
        this.code = code;
    }

    public static ShipperCode of(String code) {
        Objects.requireNonNull(code, "Shipper code cannot be null.");
        return new ShipperCode(code);
    }

    public String getCode() {
        return code;
    }
}
