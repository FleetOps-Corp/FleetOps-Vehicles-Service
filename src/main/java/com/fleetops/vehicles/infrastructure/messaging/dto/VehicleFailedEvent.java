package com.fleetops.vehicles.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleFailedEvent {

    private UUID idAsignacion;

    private String motivo;

}