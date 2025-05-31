package com.logistics.tracking_service.service;

import com.logistics.tracking_service.domain.TrackingNumberEntity;
import com.logistics.tracking_service.domain.TrackingNumberGenerator;
import com.logistics.tracking_service.domain.TrackingNumberVersion;
import com.logistics.tracking_service.repository.TrackingNumberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class TrackingNumberServiceTest {

    private TrackingNumberService trackingNumberService;

    @Mock
    private DatabaseClient databaseClient;

    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;

    @Mock
    private DatabaseClient.GenericExecuteSpec.FetchSpec<?> fetchSpec;

    @Mock
    private TrackingNumberGenerator trackingNumberGenerator;

    @Mock
    private TrackingNumberRepository trackingNumberRepository;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Setup DatabaseClient mock for sequence generation
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.map(any())).thenReturn(fetchSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(1L));
        
        // Setup repository mock
        when(trackingNumberRepository.save(any(TrackingNumberEntity.class)))
            .thenAnswer(invocation -> {
                TrackingNumberEntity entity = invocation.getArgument(0);
                return Mono.just(entity);
            });
        
        trackingNumberService = new TrackingNumberService(databaseClient, trackingNumberGenerator, trackingNumberRepository);
    }

    @Test
    void createTrackingNumber_ShouldReturnNewEntity() {
        // Arrange
        String shipperCode = "ABCD";
        Double weight = 10.5;
        String sourceCountry = "US";
        String destCountry = "CA";
        String trackingNumberValue = "TP1ABCD00000001X";
        
        TrackingNumberEntity expectedEntity = new TrackingNumberEntity(
            trackingNumberValue,
            shipperCode,
            trackingNumberValue,
            "2025-05-31T17:27:17",
            weight,
            sourceCountry,
            destCountry
        );
        
        // Mock the tracking number generator
        when(trackingNumberRepository.save(any(TrackingNumberEntity.class)))
            .thenReturn(Mono.just(expectedEntity));
        
        // Act & Assert
        StepVerifier.create(trackingNumberService.createTrackingNumber(
                shipperCode, weight, sourceCountry, destCountry))
            .expectNextMatches(entity -> 
                entity.getId().equals(trackingNumberValue) &&
                entity.getShipperCode().equals(shipperCode) &&
                entity.getWeight().equals(weight) &&
                entity.getSourceCountryCode().equals(sourceCountry) &&
                entity.getDestinationCountryCode().equals(destCountry)
            )
            .verifyComplete();
    }

    @Test
    void findByTrackingNumber_ShouldReturnEntity() {
        // Arrange
        String trackingNumberValue = "TP1ABCD00000001X";
        TrackingNumberEntity expectedEntity = new TrackingNumberEntity(
            trackingNumberValue,
            "ABCD",
            trackingNumberValue,
            "2025-05-31T17:27:17",
            10.5,
            "US",
            "CA"
        );
        
        when(trackingNumberRepository.findByTrackingNumber(trackingNumberValue))
            .thenReturn(Mono.just(expectedEntity));
        
        // Act & Assert
        StepVerifier.create(trackingNumberService.findByTrackingNumber(trackingNumberValue))
            .expectNext(expectedEntity)
            .verifyComplete();
    }
}
