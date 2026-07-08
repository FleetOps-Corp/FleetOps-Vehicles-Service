package com.fleetops.vehicles.infrastructure.messaging.sqs.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MaintenanceEvent(

        UUID maintenanceId,

        UUID vehicleId,

        String maintenanceType,

        String status,

        LocalDateTime occurredAt

) {}