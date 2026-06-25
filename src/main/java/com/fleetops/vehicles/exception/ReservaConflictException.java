package com.fleetops.vehicles.exception;

import com.fleetops.vehicles.dto.response.AgendaReservaResponse;
import java.util.List;

public class ReservaConflictException extends RuntimeException {
    
    private final List<AgendaReservaResponse> reservas;

    public ReservaConflictException(String message, List<AgendaReservaResponse> reservas) {
        super(message);
        this.reservas = reservas;
    }

    public List<AgendaReservaResponse> getReservas() {
        return reservas;
    }
}