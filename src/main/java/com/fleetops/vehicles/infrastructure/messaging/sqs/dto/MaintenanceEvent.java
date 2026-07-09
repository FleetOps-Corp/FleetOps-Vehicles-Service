package com.fleetops.vehicles.infrastructure.messaging.sqs.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MaintenanceEvent(

        UUID maintenanceId,

        UUID vehicleId,

        String maintenanceType,

        String status,

        Instant occurredAt

) {}
