package com.fleetops.vehicles.services.application;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetops.vehicles.infrastructure.messaging.sqs.dto.MaintenanceEvent;
import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.repositories.VehicleRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceIntegrationService {

    private final VehicleRepository vehicleRepository;
    private final ObjectMapper objectMapper;

    public void processMaintenanceCreated(JsonNode payload) {

        MaintenanceEvent event =
                objectMapper.convertValue(payload, MaintenanceEvent.class);

        Vehiculo vehiculo = vehicleRepository
                .findByIdVehiculoAndActivoTrue(event.vehicleId())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Vehículo no encontrado: " + event.vehicleId()));

        vehiculo.setEstadoVehiculo(EstadoVehiculo.EN_MANTENIMIENTO);

        vehicleRepository.save(vehiculo);

        log.info(
                "Vehículo {} cambiado a MANTENIMIENTO",
                event.vehicleId());
    }

public void processMaintenanceCompleted(MaintenanceEvent event) {

    Vehiculo vehiculo = vehicleRepository
            .findByIdVehiculoAndActivoTrue(event.vehicleId())
            .orElseThrow(() ->
                    new EntityNotFoundException(
                            "Vehículo no encontrado: " + event.vehicleId()));

    vehiculo.setEstadoVehiculo(EstadoVehiculo.DISPONIBLE);

    vehicleRepository.save(vehiculo);

    log.info(
            "Vehículo {} cambiado a DISPONIBLE",
            event.vehicleId());
}

}