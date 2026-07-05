package com.fleetops.vehicles.dto.response;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class VehicleReleaseResult {

    boolean processed;
    boolean idempotentReplay;
    UUID idAsignacion;
    UUID idReserva;
    String motivo;

    public static VehicleReleaseResult processed(UUID idAsignacion, UUID idReserva) {
        return VehicleReleaseResult.builder()
                .processed(true)
                .idempotentReplay(false)
                .idAsignacion(idAsignacion)
                .idReserva(idReserva)
                .build();
    }

    public static VehicleReleaseResult idempotent(UUID idAsignacion, UUID idReserva) {
        return VehicleReleaseResult.builder()
                .processed(true)
                .idempotentReplay(true)
                .idAsignacion(idAsignacion)
                .idReserva(idReserva)
                .motivo("La reserva ya estaba cancelada")
                .build();
    }

    public static VehicleReleaseResult ignored(String motivo) {
        return VehicleReleaseResult.builder()
                .processed(false)
                .motivo(motivo)
                .build();
    }
}
