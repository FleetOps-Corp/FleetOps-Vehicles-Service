package com.fleetops.vehicles.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DisponibilidadRangoResponse(
        UUID idVehiculo,
        String numeroPlaca,
        String estadoVehiculo,
        boolean operativo,
        boolean documentosVigentes,
        boolean disponibleEnRango,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String motivo,
        LocalDateTime evaluadoEn
) {}
