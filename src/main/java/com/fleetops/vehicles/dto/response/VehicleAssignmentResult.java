package com.fleetops.vehicles.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class VehicleAssignmentResult {

    private final boolean success;

    private final UUID idAsignacion;

    private final UUID idVehiculo;

    private final String motivo;

    /** true cuando la respuesta proviene de una operación ya persistida (reintento Kafka seguro). */
    @Builder.Default
    private final boolean idempotentReplay = false;

}