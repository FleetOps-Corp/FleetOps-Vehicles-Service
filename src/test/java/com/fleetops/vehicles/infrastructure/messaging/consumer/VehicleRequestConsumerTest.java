package com.fleetops.vehicles.infrastructure.messaging.consumer;

import com.fleetops.vehicles.dto.response.VehicleAssignmentResult;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleConfirmedEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleFailedEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;
import com.fleetops.vehicles.infrastructure.messaging.producer.VehicleEventProducer;
import com.fleetops.vehicles.services.application.SagaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleRequestConsumerTest {

    @Mock private SagaService sagaService;
    @Mock private VehicleEventProducer vehicleEventProducer;

    @InjectMocks private VehicleRequestConsumer consumer;

    private VehicleRequestEvent event() {
        VehicleRequestEvent event = new VehicleRequestEvent();
        event.setIdSaga(UUID.randomUUID());
        event.setIdAsignacion(UUID.randomUUID());
        event.setTipoVehiculo("Camion");
        event.setFechaInicio(LocalDate.now().plusDays(1));
        event.setFechaFin(LocalDate.now().plusDays(2));
        return event;
    }

    @Test
    void publicaConfirmadoCuandoAsignacionExitosa() {
        VehicleRequestEvent event = event();
        UUID idVehiculo = UUID.randomUUID();
        when(sagaService.procesarSolicitudAsignacion(event)).thenReturn(
                VehicleAssignmentResult.builder()
                        .success(true)
                        .idAsignacion(event.getIdAsignacion())
                        .idVehiculo(idVehiculo)
                        .build());

        consumer.receiveVehicleRequest(event);

        ArgumentCaptor<VehicleConfirmedEvent> captor = ArgumentCaptor.forClass(VehicleConfirmedEvent.class);
        verify(vehicleEventProducer).publishVehicleConfirmed(captor.capture());
        assertEquals(event.getIdAsignacion(), captor.getValue().getIdAsignacion());
        assertEquals(idVehiculo, captor.getValue().getIdVehiculo());
        verify(vehicleEventProducer, never()).publishVehicleFailed(any());
    }

    @Test
    void publicaFallidoCuandoNoHayVehiculos() {
        VehicleRequestEvent event = event();
        when(sagaService.procesarSolicitudAsignacion(event)).thenReturn(
                VehicleAssignmentResult.builder()
                        .success(false)
                        .idAsignacion(event.getIdAsignacion())
                        .motivo("Sin stock")
                        .build());

        consumer.receiveVehicleRequest(event);

        ArgumentCaptor<VehicleFailedEvent> captor = ArgumentCaptor.forClass(VehicleFailedEvent.class);
        verify(vehicleEventProducer).publishVehicleFailed(captor.capture());
        assertEquals("Sin stock", captor.getValue().getMotivo());
        verify(vehicleEventProducer, never()).publishVehicleConfirmed(any());
    }

    @Test
    void publicaFallidoCuandoServicioLanzaExcepcion() {
        VehicleRequestEvent event = event();
        when(sagaService.procesarSolicitudAsignacion(event))
                .thenThrow(new RuntimeException("BD caída"));

        consumer.receiveVehicleRequest(event);

        ArgumentCaptor<VehicleFailedEvent> captor = ArgumentCaptor.forClass(VehicleFailedEvent.class);
        verify(vehicleEventProducer).publishVehicleFailed(captor.capture());
        assertTrue(captor.getValue().getMotivo().contains("BD caída"));
    }

    @Test
    void reintentoIdempotenteRepublicaConfirmado() {
        VehicleRequestEvent event = event();
        UUID idVehiculo = UUID.randomUUID();
        when(sagaService.procesarSolicitudAsignacion(event)).thenReturn(
                VehicleAssignmentResult.builder()
                        .success(true)
                        .idAsignacion(event.getIdAsignacion())
                        .idVehiculo(idVehiculo)
                        .idempotentReplay(true)
                        .build());

        consumer.receiveVehicleRequest(event);

        verify(vehicleEventProducer).publishVehicleConfirmed(any());
    }
}
