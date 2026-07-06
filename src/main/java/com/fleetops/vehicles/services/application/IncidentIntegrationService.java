package com.fleetops.vehicles.services.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.infrastructure.messaging.sqs.dto.IncidentRegisteredEvent;
import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentIntegrationService {

    private static final String SERVICIO_ORIGEN = "INCIDENTES-SQS";

    private final VehicleRepository vehicleRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final VehicleService vehicleService;

    @Transactional
    public void processIncidentRegistered(IncidentRegisteredEvent event) {
        if (event == null || event.getIncidentId() == null || event.getIncidentId().isBlank()) {
            log.warn("Evento de incidente sin incident_id; se ignora.");
            return;
        }

        if (historialEstadoRepository.existsByIdCorrelacion(event.getIncidentId())) {
            log.info("Incidente {} ya procesado (idempotencia).", event.getIncidentId());
            return;
        }

        if (event.getVehiclePlate() == null || event.getVehiclePlate().isBlank()) {
            log.warn("Incidente {} sin placa de vehículo; se ignora.", event.getIncidentId());
            return;
        }

        String incidentType = normalize(event.getIncidentType());
        String severity = normalize(event.getSeverity());

        if ("HUMANO".equals(incidentType)) {
            log.info("Incidente humano {} para placa {} — sin cambio de estado del vehículo.",
                    event.getIncidentId(), event.getVehiclePlate());
            return;
        }

        if (!"MECANICO".equals(incidentType)) {
            log.warn("Incidente {} con tipo desconocido '{}'; se ignora.",
                    event.getIncidentId(), event.getIncidentType());
            return;
        }

        EstadoVehiculo targetState = resolveTargetState(severity);
        if (targetState == null) {
            log.info("Incidente mecánico {} con gravedad '{}' no requiere cambio de estado.",
                    event.getIncidentId(), event.getSeverity());
            return;
        }

        Vehiculo vehiculo = vehicleRepository
                .findByNumeroPlacaIgnoreCaseAndActivoTrue(event.getVehiclePlate().trim())
                .orElse(null);

        if (vehiculo == null) {
            log.warn("Incidente {}: no existe vehículo activo con placa {}.",
                    event.getIncidentId(), event.getVehiclePlate());
            return;
        }

        if (vehiculo.getEstadoVehiculo() == targetState) {
            log.info("Incidente {}: vehículo {} ya está en {}.",
                    event.getIncidentId(), event.getVehiclePlate(), targetState);
            return;
        }

        String motivo = buildMotivo(event, targetState);

        try {
            vehicleService.changeOperationalStateOnly(
                    vehiculo.getIdVehiculo(),
                    targetState.name(),
                    motivo,
                    SERVICIO_ORIGEN,
                    event.getIncidentId());
            log.info("Incidente {} aplicado: placa {} → {} (solo estado; reservas vía Kafka liberar).",
                    event.getIncidentId(), event.getVehiclePlate(), targetState);
        } catch (ResourceNotFoundException ex) {
            log.warn("Incidente {}: vehículo no encontrado al aplicar cambio de estado.", event.getIncidentId());
        }
    }

    private EstadoVehiculo resolveTargetState(String severity) {
        if ("GRAVE".equals(severity)) {
            return EstadoVehiculo.FUERA_DE_SERVICIO;
        }
        if ("LEVE".equals(severity)) {
            return EstadoVehiculo.EN_MANTENIMIENTO;
        }
        return null;
    }

    private String buildMotivo(IncidentRegisteredEvent event, EstadoVehiculo targetState) {
        String descripcion = event.getDescription() != null ? event.getDescription() : "sin descripción";
        return "Incidente mecánico %s (%s) → %s | %s"
                .formatted(event.getIncidentId(), event.getSeverity(), targetState.name(), descripcion);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
