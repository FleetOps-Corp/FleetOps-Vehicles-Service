package com.fleetops.vehicles.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequestEvent {

    private UUID idSaga;

    private UUID idAsignacion;

    private String tipoVehiculo;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private Integer kilometros;

}