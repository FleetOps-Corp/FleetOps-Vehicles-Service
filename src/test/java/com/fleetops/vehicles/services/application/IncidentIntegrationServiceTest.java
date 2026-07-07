package com.fleetops.vehicles.services.application;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fleetops.vehicles.infrastructure.messaging.sqs.dto.IncidentRegisteredEvent;
import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class IncidentIntegrationServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private HistorialEstadoRepository historialEstadoRepository;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private IncidentIntegrationService incidentIntegrationService;

    @Test
    void incidenteMecanicoGraveCambiaEstadoAFueraDeServicio() {
        UUID vehiculoId = UUID.randomUUID();
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setIdVehiculo(vehiculoId);
        vehiculo.setNumeroPlaca("CXT401");
        vehiculo.setEstadoVehiculo(EstadoVehiculo.DISPONIBLE);

        IncidentRegisteredEvent event = new IncidentRegisteredEvent();
        event.setIncidentId("inc-001");
        event.setVehiclePlate("CXT401");
        event.setIncidentType("MECANICO");
        event.setSeverity("GRAVE");
        event.setDescription("Motor averiado");

        when(historialEstadoRepository.existsByIdCorrelacion("inc-001")).thenReturn(false);
        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("CXT401"))
                .thenReturn(Optional.of(vehiculo));

        incidentIntegrationService.processIncidentRegistered(event);

        verify(vehicleService).changeOperationalStateOnly(
                eq(vehiculoId),
                eq("FUERA_DE_SERVICIO"),
                org.mockito.ArgumentMatchers.contains("inc-001"),
                eq("INCIDENTES-SQS"),
                eq("inc-001"));
    }

    @Test
    void incidenteHumanoNoCambiaEstado() {
        IncidentRegisteredEvent event = new IncidentRegisteredEvent();
        event.setIncidentId("inc-002");
        event.setVehiclePlate("CXT401");
        event.setIncidentType("HUMANO");
        event.setSeverity("LEVE");

        when(historialEstadoRepository.existsByIdCorrelacion("inc-002")).thenReturn(false);

        incidentIntegrationService.processIncidentRegistered(event);

        verify(vehicleService, never()).changeOperationalStateOnly(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
        verify(vehicleService, never()).changeState(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void incidenteDuplicadoEsIdempotente() {
        IncidentRegisteredEvent event = new IncidentRegisteredEvent();
        event.setIncidentId("inc-003");
        event.setVehiclePlate("CXT401");
        event.setIncidentType("MECANICO");
        event.setSeverity("GRAVE");

        when(historialEstadoRepository.existsByIdCorrelacion("inc-003")).thenReturn(true);

        incidentIntegrationService.processIncidentRegistered(event);

        verify(vehicleRepository, never()).findByNumeroPlacaIgnoreCaseAndActivoTrue(org.mockito.ArgumentMatchers.any());
    }
}
