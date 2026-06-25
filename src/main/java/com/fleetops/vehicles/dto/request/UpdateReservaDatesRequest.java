package com.fleetops.vehicles.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record UpdateReservaDatesRequest(
    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser presente o futura")
    LocalDateTime fechaInicio,

    @NotNull(message = "La fecha de fin es obligatoria")
    @FutureOrPresent(message = "La fecha de fin debe ser presente o futura")
    LocalDateTime fechaFin
) {}