package com.fleetops.vehicles.infrastructure.messaging;

import com.fleetops.vehicles.dto.response.VehicleAssignmentResult;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleConfirmedEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleFailedEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;
import com.fleetops.vehicles.infrastructure.messaging.topics.KafkaTopics;
import com.fleetops.vehicles.services.application.SagaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleAssignmentCoordinatorTest {

    @Mock private SagaService sagaService;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks private VehicleAssignmentCoordinator coordinator;

    @Test
    void publicaConfirmadoTrasCrearReserva() {
        VehicleRequestEvent event = new VehicleRequestEvent();
        event.setIdSaga(UUID.randomUUID());
        event.setIdAsignacion(UUID.randomUUID());
        event.setTipoVehiculo("Camion");
        event.setFechaInicio(LocalDate.now().plusDays(1));
        event.setFechaFin(LocalDate.now().plusDays(2));

        UUID idVehiculo = UUID.randomUUID();
        when(sagaService.procesarSolicitudAsignacion(event)).thenReturn(
                VehicleAssignmentResult.builder()
                        .success(true)
                        .idAsignacion(event.getIdAsignacion())
                        .idVehiculo(idVehiculo)
                        .build());

        coordinator.procesarSolicitudConPublicacion(event);

        ArgumentCaptor<VehicleConfirmedEvent> captor = ArgumentCaptor.forClass(VehicleConfirmedEvent.class);
        verify(kafkaTemplate).send(eq(KafkaTopics.VEHICLE_CONFIRMED), captor.capture());
        assertEquals(event.getIdAsignacion(), captor.getValue().getIdAsignacion());
        assertEquals(idVehiculo, captor.getValue().getIdVehiculo());
    }

    @Test
    void publicaFallidoCuandoNoHayVehiculo() {
        VehicleRequestEvent event = new VehicleRequestEvent();
        event.setIdSaga(UUID.randomUUID());
        event.setIdAsignacion(UUID.randomUUID());
        event.setTipoVehiculo("Camion");
        event.setFechaInicio(LocalDate.now().plusDays(1));
        event.setFechaFin(LocalDate.now().plusDays(2));

        when(sagaService.procesarSolicitudAsignacion(event)).thenReturn(
                VehicleAssignmentResult.builder()
                        .success(false)
                        .idAsignacion(event.getIdAsignacion())
                        .motivo("Sin stock")
                        .build());

        coordinator.procesarSolicitudConPublicacion(event);

        ArgumentCaptor<VehicleFailedEvent> captor = ArgumentCaptor.forClass(VehicleFailedEvent.class);
        verify(kafkaTemplate).send(eq(KafkaTopics.VEHICLE_FAILED), captor.capture());
        assertEquals("Sin stock", captor.getValue().getMotivo());
    }
}
