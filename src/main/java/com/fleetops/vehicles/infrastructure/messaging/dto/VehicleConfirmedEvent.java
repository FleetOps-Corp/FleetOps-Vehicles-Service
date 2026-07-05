package com.fleetops.vehicles.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleConfirmedEvent {

    private UUID idAsignacion;
    private UUID idVehiculo;

}