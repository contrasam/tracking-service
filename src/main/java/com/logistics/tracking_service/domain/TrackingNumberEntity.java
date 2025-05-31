package com.logistics.tracking_service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Sequence;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@Getter
@Setter
@Table("tracking_number")
public class TrackingNumberEntity {

    @Id
    @Sequence(value = "tracking_sequence")
    private String id;

    private String shipperCode;

    private String trackingNumber;

    private String createdAt;

    private Double weight;

    private String sourceCountryCode;

    private String destinationCountryCode;

}
