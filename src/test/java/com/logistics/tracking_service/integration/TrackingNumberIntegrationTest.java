package com.logistics.tracking_service.integration;

import com.logistics.tracking_service.domain.TrackingNumberEntity;
import com.logistics.tracking_service.repository.TrackingNumberRepository;
import com.logistics.tracking_service.service.TrackingNumberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
public class TrackingNumberIntegrationTest {

    @Autowired
    private TrackingNumberService trackingNumberService;
    
    @Autowired
    private TrackingNumberRepository trackingNumberRepository;
    
    @Test
    void createAndRetrieveTrackingNumber() {
        // Create a new tracking number
        String shipperCode = "TEST";
        Double weight = 15.5;
        String sourceCountry = "US";
        String destCountry = "UK";
        
        Mono<TrackingNumberEntity> createdEntityMono = trackingNumberService.createTrackingNumber(
                shipperCode, weight, sourceCountry, destCountry);
        
        // Verify the entity was created successfully
        StepVerifier.create(createdEntityMono)
            .expectNextMatches(entity -> 
                entity.getShipperCode().equals(shipperCode) &&
                entity.getWeight().equals(weight) &&
                entity.getSourceCountryCode().equals(sourceCountry) &&
                entity.getDestinationCountryCode().equals(destCountry)
            )
            .verifyComplete();
        
        // Retrieve the created entity and verify it matches
        TrackingNumberEntity createdEntity = createdEntityMono.block();
        assert createdEntity != null;
        
        StepVerifier.create(trackingNumberService.findByTrackingNumber(createdEntity.getTrackingNumber()))
            .expectNextMatches(entity -> 
                entity.getId().equals(createdEntity.getId()) &&
                entity.getShipperCode().equals(shipperCode) &&
                entity.getWeight().equals(weight) &&
                entity.getSourceCountryCode().equals(sourceCountry) &&
                entity.getDestinationCountryCode().equals(destCountry)
            )
            .verifyComplete();
    }
}
