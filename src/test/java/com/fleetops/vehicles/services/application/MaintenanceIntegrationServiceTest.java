package com.fleetops.vehicles.services.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.infrastructure.messaging.sqs.dto.MaintenanceEvent;
import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class MaintenanceIntegrationServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private HistorialEstadoRepository historialEstadoRepository;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private MaintenanceIntegrationService maintenanceIntegrationService;

    private MaintenanceEvent event(UUID maintenanceId, UUID vehicleId, String status) {
        return new MaintenanceEvent(
                maintenanceId,
                vehicleId,
                "CORRECTIVE",
                status,
                Instant.parse("2026-07-08T10:30:00Z"));
    }

    @Test
    void creacionCambiaEstadoAEnMantenimiento() {
        UUID maintenanceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        Vehiculo vehiculo = vehiculo(vehicleId, EstadoVehiculo.DISPONIBLE);

        String idCorrelacion = maintenanceId + ":CREATED";
        when(historialEstadoRepository.existsByIdCorrelacion(idCorrelacion)).thenReturn(false);
        when(vehicleRepository.findByIdVehiculoAndActivoTrue(vehicleId)).thenReturn(Optional.of(vehiculo));

        maintenanceIntegrationService.processMaintenanceCreated(event(maintenanceId, vehicleId, "CREATED"));

        verify(vehicleService).changeOperationalStateOnly(
                eq(vehicleId),
                eq("EN_MANTENIMIENTO"),
                contains(maintenanceId.toString()),
                eq("MANTENIMIENTO-SQS"),
                eq(idCorrelacion));
    }

    @Test
    void finalizacionCambiaEstadoADisponible() {
        UUID maintenanceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        Vehiculo vehiculo = vehiculo(vehicleId, EstadoVehiculo.EN_MANTENIMIENTO);

        String idCorrelacion = maintenanceId + ":COMPLETED";
        when(historialEstadoRepository.existsByIdCorrelacion(idCorrelacion)).thenReturn(false);
        when(vehicleRepository.findByIdVehiculoAndActivoTrue(vehicleId)).thenReturn(Optional.of(vehiculo));

        maintenanceIntegrationService.processMaintenanceCompleted(event(maintenanceId, vehicleId, "COMPLETED"));

        verify(vehicleService).changeOperationalStateOnly(
                eq(vehicleId),
                eq("DISPONIBLE"),
                contains(maintenanceId.toString()),
                eq("MANTENIMIENTO-SQS"),
                eq(idCorrelacion));
    }

    @Test
    void eventoDuplicadoEsIdempotente() {
        UUID maintenanceId = UUID.randomUUID();
        String idCorrelacion = maintenanceId + ":COMPLETED";

        when(historialEstadoRepository.existsByIdCorrelacion(idCorrelacion)).thenReturn(true);

        maintenanceIntegrationService.processMaintenanceCompleted(
                event(maintenanceId, UUID.randomUUID(), "COMPLETED"));

        verify(vehicleRepository, never()).findByIdVehiculoAndActivoTrue(any());
        verify(vehicleService, never()).changeOperationalStateOnly(
                any(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void creacionDuplicadaEsIdempotentePorSufijoCreated() {
        UUID maintenanceId = UUID.randomUUID();
        String idCorrelacion = maintenanceId + ":CREATED";

        when(historialEstadoRepository.existsByIdCorrelacion(idCorrelacion)).thenReturn(true);

        maintenanceIntegrationService.processMaintenanceCreated(
                event(maintenanceId, UUID.randomUUID(), "CREATED"));

        verify(vehicleService, never()).changeOperationalStateOnly(
                any(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void sinMaintenanceIdNoCambiaEstado() {
        MaintenanceEvent event = new MaintenanceEvent(
                null, UUID.randomUUID(), "CORRECTIVE", "CREATED", Instant.now());

        maintenanceIntegrationService.processMaintenanceCreated(event);

        verify(historialEstadoRepository, never()).existsByIdCorrelacion(anyString());
        verify(vehicleService, never()).changeOperationalStateOnly(
                any(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void sinVehicleIdNoCambiaEstado() {
        UUID maintenanceId = UUID.randomUUID();
        when(historialEstadoRepository.existsByIdCorrelacion(maintenanceId + ":CREATED")).thenReturn(false);

        maintenanceIntegrationService.processMaintenanceCreated(
                event(maintenanceId, null, "CREATED"));

        verify(vehicleRepository, never()).findByIdVehiculoAndActivoTrue(any());
        verify(vehicleService, never()).changeOperationalStateOnly(
                any(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void vehiculoInexistenteNoCambiaEstado() {
        UUID maintenanceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        when(historialEstadoRepository.existsByIdCorrelacion(maintenanceId + ":COMPLETED")).thenReturn(false);
        when(vehicleRepository.findByIdVehiculoAndActivoTrue(vehicleId)).thenReturn(Optional.empty());

        maintenanceIntegrationService.processMaintenanceCompleted(
                event(maintenanceId, vehicleId, "COMPLETED"));

        verify(vehicleService, never()).changeOperationalStateOnly(
                any(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void vehiculoYaEnEstadoDestinoNoLlamaChangeState() {
        UUID maintenanceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        when(historialEstadoRepository.existsByIdCorrelacion(maintenanceId + ":COMPLETED")).thenReturn(false);
        when(vehicleRepository.findByIdVehiculoAndActivoTrue(vehicleId))
                .thenReturn(Optional.of(vehiculo(vehicleId, EstadoVehiculo.DISPONIBLE)));

        maintenanceIntegrationService.processMaintenanceCompleted(
                event(maintenanceId, vehicleId, "COMPLETED"));

        verify(vehicleService, never()).changeOperationalStateOnly(
                any(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void businessExceptionAlCambiarEstadoNoRelanza() {
        UUID maintenanceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        String idCorrelacion = maintenanceId + ":CREATED";

        when(historialEstadoRepository.existsByIdCorrelacion(idCorrelacion)).thenReturn(false);
        when(vehicleRepository.findByIdVehiculoAndActivoTrue(vehicleId))
                .thenReturn(Optional.of(vehiculo(vehicleId, EstadoVehiculo.DISPONIBLE)));
        doThrow(new BusinessException("transición inválida"))
                .when(vehicleService)
                .changeOperationalStateOnly(any(), anyString(), anyString(), anyString(), any());

        maintenanceIntegrationService.processMaintenanceCreated(event(maintenanceId, vehicleId, "CREATED"));

        verify(vehicleService).changeOperationalStateOnly(
                eq(vehicleId), eq("EN_MANTENIMIENTO"), anyString(), eq("MANTENIMIENTO-SQS"), eq(idCorrelacion));
    }

    @Test
    void resourceNotFoundAlCambiarEstadoNoRelanza() {
        UUID maintenanceId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        String idCorrelacion = maintenanceId + ":COMPLETED";

        when(historialEstadoRepository.existsByIdCorrelacion(idCorrelacion)).thenReturn(false);
        when(vehicleRepository.findByIdVehiculoAndActivoTrue(vehicleId))
                .thenReturn(Optional.of(vehiculo(vehicleId, EstadoVehiculo.EN_MANTENIMIENTO)));
        doThrow(new ResourceNotFoundException("Vehículo", "id", vehicleId))
                .when(vehicleService)
                .changeOperationalStateOnly(any(), anyString(), anyString(), anyString(), any());

        maintenanceIntegrationService.processMaintenanceCompleted(event(maintenanceId, vehicleId, "COMPLETED"));

        verify(vehicleService).changeOperationalStateOnly(
                eq(vehicleId), eq("DISPONIBLE"), anyString(), eq("MANTENIMIENTO-SQS"), eq(idCorrelacion));
    }

    @Test
    void motivoIncluyeTipoYFaseDeMantenimiento() {
        UUID maintenanceId = UUID.fromString("9f26d4de-d43b-4d9e-a8d8-cba72b9d96d1");
        UUID vehicleId = UUID.fromString("bc5d79f4-0ef7-43dd-9038-6382d51d58e0");
        String idCorrelacion = maintenanceId + ":CREATED";

        when(historialEstadoRepository.existsByIdCorrelacion(idCorrelacion)).thenReturn(false);
        when(vehicleRepository.findByIdVehiculoAndActivoTrue(vehicleId))
                .thenReturn(Optional.of(vehiculo(vehicleId, EstadoVehiculo.DISPONIBLE)));

        maintenanceIntegrationService.processMaintenanceCreated(event(maintenanceId, vehicleId, "CREATED"));

        verify(vehicleService).changeOperationalStateOnly(
                eq(vehicleId),
                eq("EN_MANTENIMIENTO"),
                contains("creación"),
                eq("MANTENIMIENTO-SQS"),
                eq(idCorrelacion));
    }

    private Vehiculo vehiculo(UUID vehicleId, EstadoVehiculo estado) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setIdVehiculo(vehicleId);
        vehiculo.setEstadoVehiculo(estado);
        return vehiculo;
    }
}
