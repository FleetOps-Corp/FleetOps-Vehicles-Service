package com.fleetops.vehicles.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class VehicleAssignmentResult {

    private final boolean success;

    private final UUID idAsignacion;

    private final UUID idVehiculo;

    private final String motivo;

}