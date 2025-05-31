package com.logistics.tracking_service.exception;

public class TrackingServiceException extends RuntimeException {
    
    private final String errorCode;
    
    public TrackingServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public TrackingServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public static class ErrorCodes {
        public static final String INVALID_SHIPPER_CODE = "INVALID_SHIPPER_CODE";
        public static final String INVALID_VERSION = "INVALID_VERSION";
        public static final String SEQUENCE_ERROR = "SEQUENCE_ERROR";
        public static final String DATABASE_ERROR = "DATABASE_ERROR";
        public static final String TRACKING_NUMBER_GENERATION_ERROR = "TRACKING_NUMBER_GENERATION_ERROR";
        public static final String INVALID_COUNTRY_CODE = "INVALID_COUNTRY_CODE";
        public static final String INVALID_WEIGHT = "INVALID_WEIGHT";
        public static final String INVALID_CUSTOMER_ID = "INVALID_CUSTOMER_ID";
        public static final String INVALID_CUSTOMER_NAME = "INVALID_CUSTOMER_NAME";
        public static final String INVALID_DATE_FORMAT = "INVALID_DATE_FORMAT";
    }
}
