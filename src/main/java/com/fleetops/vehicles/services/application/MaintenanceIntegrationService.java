package com.fleetops.vehicles.services.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.infrastructure.messaging.sqs.dto.MaintenanceEvent;
import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceIntegrationService {

    private static final String SERVICIO_ORIGEN = "MANTENIMIENTO-SQS";
    private static final String SUFFIX_CREATED = ":CREATED";
    private static final String SUFFIX_COMPLETED = ":COMPLETED";

    private final VehicleRepository vehicleRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final VehicleService vehicleService;

    @Transactional
    public void processMaintenanceCreated(MaintenanceEvent event) {
        processStateChange(event, EstadoVehiculo.EN_MANTENIMIENTO, SUFFIX_CREATED, "creación");
    }

    @Transactional
    public void processMaintenanceCompleted(MaintenanceEvent event) {
        processStateChange(event, EstadoVehiculo.DISPONIBLE, SUFFIX_COMPLETED, "finalización");
    }

    private void processStateChange(
            MaintenanceEvent event,
            EstadoVehiculo targetState,
            String idempotencySuffix,
            String fase) {

        if (event == null || event.maintenanceId() == null) {
            log.warn("Evento de mantenimiento ({}) sin maintenanceId; se ignora.", fase);
            return;
        }

        String idCorrelacion = event.maintenanceId() + idempotencySuffix;
        if (historialEstadoRepository.existsByIdCorrelacion(idCorrelacion)) {
            log.info("Mantenimiento {} ya procesado (idempotencia, {}).", event.maintenanceId(), fase);
            return;
        }

        if (event.vehicleId() == null) {
            log.warn("Mantenimiento {} sin vehicleId; se ignora.", event.maintenanceId());
            return;
        }

        Vehiculo vehiculo = vehicleRepository
                .findByIdVehiculoAndActivoTrue(event.vehicleId())
                .orElse(null);

        if (vehiculo == null) {
            log.warn("Mantenimiento {}: no existe vehículo activo con id {}.",
                    event.maintenanceId(), event.vehicleId());
            return;
        }

        if (vehiculo.getEstadoVehiculo() == targetState) {
            log.info("Mantenimiento {}: vehículo {} ya está en {}.",
                    event.maintenanceId(), event.vehicleId(), targetState);
            return;
        }

        String motivo = buildMotivo(event, targetState, fase);

        try {
            vehicleService.changeOperationalStateOnly(
                    vehiculo.getIdVehiculo(),
                    targetState.name(),
                    motivo,
                    SERVICIO_ORIGEN,
                    idCorrelacion);
            log.info("Mantenimiento {} aplicado: vehículo {} → {} (solo estado; reservas vía Kafka liberar).",
                    event.maintenanceId(), event.vehicleId(), targetState);
        } catch (ResourceNotFoundException ex) {
            log.warn("Mantenimiento {}: vehículo no encontrado al aplicar cambio de estado.",
                    event.maintenanceId());
        } catch (BusinessException ex) {
            log.warn("Mantenimiento {}: cambio de estado rechazado: {}",
                    event.maintenanceId(), ex.getMessage());
        }
    }

    private String buildMotivo(MaintenanceEvent event, EstadoVehiculo targetState, String fase) {
        String tipo = event.maintenanceType() != null ? event.maintenanceType() : "sin tipo";
        String status = event.status() != null ? event.status() : "sin estado";
        return "Mantenimiento %s (%s, %s) → %s | tipo=%s"
                .formatted(event.maintenanceId(), fase, status, targetState.name(), tipo);
    }
}
