package com.fleetops.vehicles.dto.response;

import java.time.LocalDateTime;

// DTO simple que representa un bloque de tiempo ocupado
public record AgendaReservaResponse(
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String estadoReserva
) {}