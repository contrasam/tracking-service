package com.logistics.tracking_service.repository;

import com.logistics.tracking_service.domain.TrackingNumberEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface TrackingNumberRepository extends ReactiveCrudRepository<TrackingNumberEntity, String> {
    
    /**
     * Find a tracking number entity by its tracking number
     * 
     * @param trackingNumber The tracking number to search for
     * @return A Mono containing the found entity, or an empty Mono if not found
     */
    Mono<TrackingNumberEntity> findByTrackingNumber(String trackingNumber);
}
