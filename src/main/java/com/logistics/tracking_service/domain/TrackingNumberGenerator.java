package com.logistics.tracking_service.domain;

import java.util.Objects;

public class TrackingNumberGenerator {

    private final TrackingNumberVersion version;
    private static final String BASE36_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SHIPPER_CODE_LENGTH = 4;
    private static final int SEQUENCE_LENGTH = 8;

    public TrackingNumberGenerator(TrackingNumberVersion version) {
        this.version = version;
    }

    public TrackingNumber generateNextTrackingNumber(ShipperCode shipperCode, long currentSequenceValue) throws InvalidTrackingNumberException {
        if (shipperCode == null || shipperCode.getCode() == null || shipperCode.getCode().isEmpty()) {
            throw new IllegalArgumentException("Shipper code cannot be null or empty");
        }

        String versionPrefix = version.getVersion();

        Objects.requireNonNull(versionPrefix, "Version prefix cannot be null.");
        Objects.requireNonNull(shipperCode, "Shipper code cannot be null.");

        if (shipperCode.getCode().length() != SHIPPER_CODE_LENGTH) {
            throw new IllegalArgumentException("Shipper code length must be " + SHIPPER_CODE_LENGTH +
                    ", but was " + shipperCode.getCode().length());
        }

        for (char scChar : shipperCode.getCode().toCharArray()) {
            if (BASE36_CHARS.indexOf(scChar) == -1) {
                throw new IllegalArgumentException("Shipper code contains invalid character: " + scChar);
            }
        }

        for (char vpChar : versionPrefix.toCharArray()) {
            if (BASE36_CHARS.indexOf(vpChar) == -1) {
                throw new IllegalArgumentException("Version prefix contains invalid character: " + vpChar);
            }
        }

        String base36SeqRaw = toBase36(currentSequenceValue);
        String payload = versionPrefix +getSequence(shipperCode, base36SeqRaw);
        // Calculating the checksum
        char checksumChar = calculateChecksum(payload);

        return TrackingNumber.of(payload + checksumChar);
    }

    private String getSequence(ShipperCode shipperCode, String base36SeqRaw) {
        if (base36SeqRaw.length() > SEQUENCE_LENGTH) {
            throw new IllegalArgumentException("Generated base-36 sequence (" + base36SeqRaw +
                    ") is too long for allocated space of " + SEQUENCE_LENGTH +
                    " characters. Max sequence value exceeded for this length.");
        }

        StringBuilder paddedBase36SeqBuilder = new StringBuilder(SEQUENCE_LENGTH);
        for (int i = 0; i < SEQUENCE_LENGTH - base36SeqRaw.length(); i++) {
            paddedBase36SeqBuilder.append('0');
        }
        paddedBase36SeqBuilder.append(base36SeqRaw);
        String paddedBase36Seq = paddedBase36SeqBuilder.toString();

        String payload = shipperCode.getCode() + paddedBase36Seq;
        return payload;
    }

    private char calculateChecksum(String payload) {
        Objects.requireNonNull(payload, "Payload cannot be null.");
        int sum = 0;
        for (int i = 0; i < payload.length(); i++) {
            char c = payload.charAt(payload.length() - 1 - i);
            int value = fromBase36Char(c);

            if (i % 2 == 0) {
                value *= 2;
                if (value >= 36) {
                    value = (value / 36) + (value % 36);
                }
            }
            sum += value;
        }
        int checkDigitValue = (36 - (sum % 36)) % 36;
        return BASE36_CHARS.charAt(checkDigitValue);
    }

    public boolean validateChecksum(TrackingNumber fullTrackingNumber) {
        Objects.requireNonNull(fullTrackingNumber, "Full tracking number cannot be null.");
        if (fullTrackingNumber.getId().isEmpty()) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < fullTrackingNumber.getId().length(); i++) {
            char c = fullTrackingNumber.getId().charAt(fullTrackingNumber.getId().length() - 1 - i);
            int value;
            try {
                value = fromBase36Char(c);
            } catch (IllegalArgumentException e) {
                return false;
            }

            if (i % 2 == 1) {
                value *= 2;
                if (value >= 36) {
                    value = (value / 36) + (value % 36);
                }
            }
            sum += value;
        }
        return sum % 36 == 0;
    }

    public String toBase36(long n_decimal) {
        if (n_decimal < 0) {
            throw new IllegalArgumentException("Input number must be non-negative for base-36 conversion.");
        }
        if (n_decimal == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        while (n_decimal > 0) {
            sb.insert(0, BASE36_CHARS.charAt((int) (n_decimal % 36)));
            n_decimal /= 36;
        }
        return sb.toString();
    }

    public static int fromBase36Char(char c) {
        int value = BASE36_CHARS.indexOf(c);
        if (value == -1) {
            throw new IllegalArgumentException("Invalid character for base-36: " + c);
        }
        return value;
    }
}
