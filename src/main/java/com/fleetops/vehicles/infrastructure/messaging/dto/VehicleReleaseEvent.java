package com.fleetops.vehicles.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Evento entrante desde Asignaciones para liberar el calendario de un vehículo.
 * Contrato provisional — ver docs/contrato-kafka.md.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleReleaseEvent {

    /** Correlación con el microservicio de Asignaciones (preferido). */
    private UUID idAsignacion;

    /** Correlación con la saga distribuida (alternativa si no hay idAsignacion). */
    private UUID idSaga;

    /** Motivo de negocio de la cancelación/liberación. */
    private String motivo;

    /** Origen opcional del evento (ej. ASIGNACIONES, INCIDENTES). */
    private String origen;
}
