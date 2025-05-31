package com.logistics.tracking_service.domain;

import java.util.Objects;

/**
 * Service responsible for generating and validating tracking numbers.
 * <p>
 * This class implements the core business logic for creating tracking numbers
 * with the format: [Version][ShipperCode][Sequence][Checksum] where:
 * <ul>
 *   <li>Version: A tracking number version (e.g., TP1)</li>
 *   <li>ShipperCode: A 4-character code identifying the shipper</li>
 *   <li>Sequence: An 8-character base-36 encoded sequence number</li>
 *   <li>Checksum: A single character checksum for validation</li>
 * </ul>
 * </p>
 * <p>
 * The generator uses a base-36 encoding system (digits 0-9 and letters A-Z)
 * and implements a checksum algorithm for validation.
 * </p>
 *
 * @since 1.0
 */
public class TrackingNumberGenerator {

    /**
     * The tracking number version to use for generation.
     */
    private final TrackingNumberVersion version;
    
    /**
     * Characters used for base-36 encoding (0-9, A-Z).
     */
    private static final String BASE36_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    /**
     * The fixed length of a shipper code.
     */
    private static final int SHIPPER_CODE_LENGTH = 4;
    
    /**
     * The fixed length of the sequence portion of a tracking number.
     */
    private static final int SEQUENCE_LENGTH = 8;

    /**
     * Constructs a new TrackingNumberGenerator with the specified version.
     *
     * @param version the tracking number version to use
     */
    public TrackingNumberGenerator(TrackingNumberVersion version) {
        this.version = version;
    }

    /**
     * Generates a new tracking number based on the shipper code and sequence value.
     * <p>
     * The generated tracking number follows the format:
     * [Version][ShipperCode][Sequence][Checksum]
     * </p>
     *
     * @param shipperCode the 4-character shipper code
     * @param currentSequenceValue the current sequence value from the database
     * @return a new TrackingNumber instance
     * @throws InvalidTrackingNumberException if the generated tracking number is invalid
     * @throws IllegalArgumentException if the shipper code is invalid or null
     */
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
        String payload = versionPrefix + getSequence(shipperCode, base36SeqRaw);
        // Calculating the checksum
        char checksumChar = calculateChecksum(payload);

        return TrackingNumber.of(payload + checksumChar);
    }

    /**
     * Creates the sequence portion of the tracking number by combining the shipper code
     * and the zero-padded base-36 sequence.
     *
     * @param shipperCode the shipper code
     * @param base36SeqRaw the raw base-36 sequence value
     * @return the combined shipper code and padded sequence
     * @throws IllegalArgumentException if the sequence is too long for the allocated space
     */
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

    /**
     * Calculates the checksum character for a tracking number payload.
     * <p>
     * The checksum algorithm uses a weighted sum of the base-36 values of each character
     * in the payload, with alternating weights of 1 and 2.
     * </p>
     *
     * @param payload the tracking number payload (without checksum)
     * @return the calculated checksum character
     * @throws NullPointerException if the payload is null
     */
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

    /**
     * Validates the checksum of a tracking number.
     * <p>
     * The validation algorithm calculates a weighted sum of all characters
     * in the tracking number (including the checksum) and verifies that
     * the sum is divisible by 36.
     * </p>
     *
     * @param fullTrackingNumber the tracking number to validate
     * @return true if the checksum is valid, false otherwise
     * @throws NullPointerException if the tracking number is null
     */
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

    /**
     * Converts a decimal number to a base-36 string representation.
     * <p>
     * Base-36 uses digits 0-9 and letters A-Z to represent values.
     * </p>
     *
     * @param n_decimal the decimal number to convert
     * @return the base-36 string representation
     * @throws IllegalArgumentException if the input number is negative
     */
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

    /**
     * Converts a base-36 character to its decimal value.
     * <p>
     * Valid characters are 0-9 and A-Z (case-sensitive).
     * </p>
     *
     * @param c the base-36 character to convert
     * @return the decimal value of the character
     * @throws IllegalArgumentException if the character is not a valid base-36 character
     */
    public static int fromBase36Char(char c) {
        int value = BASE36_CHARS.indexOf(c);
        if (value == -1) {
            throw new IllegalArgumentException("Invalid character for base-36: " + c);
        }
        return value;
    }
}
