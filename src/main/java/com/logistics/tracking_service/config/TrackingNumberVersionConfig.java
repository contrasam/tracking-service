package com.logistics.tracking_service.config;

import com.logistics.tracking_service.domain.InvalidTrackingNumberVersionException;
import com.logistics.tracking_service.domain.TrackingNumberVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TrackingNumberVersionConfig {

    @Bean
    public TrackingNumberVersion trackingNumberVersion() throws InvalidTrackingNumberVersionException {
        // Default version for tracking numbers
        return TrackingNumberVersion.of("TP1");
    }
}
