package com.fleetops.vehicles.infrastructure.messaging.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentCompletedEvent {

    private UUID idSaga;
    private UUID idAsignacion;
    private UUID idVehiculo;
    private UUID idConductor;

}