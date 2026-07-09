package com.fleetops.vehicles.infrastructure.messaging.consumer;

import com.fleetops.vehicles.infrastructure.messaging.VehicleAssignmentCoordinator;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleFailedEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;
import com.fleetops.vehicles.infrastructure.messaging.producer.VehicleEventProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleRequestConsumerTest {

    @Mock private VehicleAssignmentCoordinator assignmentCoordinator;
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
    void delegaProcesamientoAlCoordinator() {
        VehicleRequestEvent event = event();
        consumer.receiveVehicleRequest(event);
        verify(assignmentCoordinator).procesarSolicitudConPublicacion(event);
        verify(vehicleEventProducer, never()).publishVehicleFailed(any());
    }

    @Test
    void publicaFallidoCuandoCoordinatorLanzaExcepcion() {
        VehicleRequestEvent event = event();
        when(assignmentCoordinator.procesarSolicitudConPublicacion(event))
                .thenThrow(new RuntimeException("BD caída"));

        consumer.receiveVehicleRequest(event);

        verify(vehicleEventProducer).publishVehicleFailed(any(VehicleFailedEvent.class));
    }
}
