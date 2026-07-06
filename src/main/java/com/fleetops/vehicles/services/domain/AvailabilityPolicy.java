package com.fleetops.vehicles.services.domain;

import com.fleetops.vehicles.models.entities.EstadoReserva;
import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.repositories.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AvailabilityPolicy {

    private static final List<EstadoReserva> ESTADOS_BLOQUEANTES = List.of(EstadoReserva.CONFIRMADA);

    private final ReservaRepository reservaRepository;

    /** Vehículo operativo: activo y no en mantenimiento ni fuera de servicio. */
    public boolean isOperational(Vehiculo vehiculo) {
        return vehiculo != null
                && Boolean.TRUE.equals(vehiculo.getActivo())
                && vehiculo.getEstadoVehiculo() == EstadoVehiculo.DISPONIBLE;
    }

    /** Alias de isOperational para compatibilidad con consultas REST existentes. */
    public boolean isAvailable(Vehiculo vehiculo) {
        return isOperational(vehiculo);
    }

    public boolean hasValidDocuments(Vehiculo vehiculo) {
        LocalDate hoy = LocalDate.now();

        if (vehiculo.getFechaSoat() == null || vehiculo.getFechaSoat().isBefore(hoy)) {
            log.warn("Vehículo {} rechazado: SOAT vencido o ausente", vehiculo.getNumeroPlaca());
            return false;
        }
        if (ChronoUnit.DAYS.between(hoy, vehiculo.getFechaSoat()) <= 7) {
            log.warn("Vehículo {} rechazado: SOAT vence en 7 días o menos", vehiculo.getNumeroPlaca());
            return false;
        }
        if (vehiculo.getFechaRtm() == null || vehiculo.getFechaRtm().isBefore(hoy)) {
            log.warn("Vehículo {} rechazado: RTM vencida o ausente", vehiculo.getNumeroPlaca());
            return false;
        }
        if (ChronoUnit.DAYS.between(hoy, vehiculo.getFechaRtm()) <= 7) {
            log.warn("Vehículo {} rechazado: RTM vence en 7 días o menos", vehiculo.getNumeroPlaca());
            return false;
        }
        return true;
    }

    /** Documentos vigentes sin la regla estricta de 7 días (consultas rápidas). */
    public boolean isAvailableForReservation(Vehiculo vehiculo) {
        if (!isOperational(vehiculo)) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        if (vehiculo.getFechaSoat() != null && vehiculo.getFechaSoat().isBefore(hoy)) {
            log.warn("Vehículo {} rechazado: SOAT vencido", vehiculo.getNumeroPlaca());
            return false;
        }
        if (vehiculo.getFechaRtm() != null && vehiculo.getFechaRtm().isBefore(hoy)) {
            log.warn("Vehículo {} rechazado: RTM vencida", vehiculo.getNumeroPlaca());
            return false;
        }
        return true;
    }

    public boolean hasReservationConflict(UUID idVehiculo, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return !reservaRepository.obtenerReservasConflictivas(
                idVehiculo, ESTADOS_BLOQUEANTES, fechaFin, fechaInicio).isEmpty();
    }

    /** Asignable si está operativo, con documentos válidos y sin solapamiento de reservas CONFIRMADAS. */
    public boolean isAssignable(Vehiculo vehiculo, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return isOperational(vehiculo)
                && hasValidDocuments(vehiculo)
                && !hasReservationConflict(vehiculo.getIdVehiculo(), fechaInicio, fechaFin);
    }
}
