package com.fleetops.vehicles.infrastructure.messaging.consumer;

import com.fleetops.vehicles.dto.response.VehicleReleaseResult;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleReleaseEvent;
import com.fleetops.vehicles.services.application.SagaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleReleaseConsumerTest {

    @Mock private SagaService sagaService;
    @InjectMocks private VehicleReleaseConsumer consumer;

    @Test
    void procesaLiberacionExitosa() {
        UUID idAsignacion = UUID.randomUUID();
        UUID idReserva = UUID.randomUUID();
        VehicleReleaseEvent event = VehicleReleaseEvent.builder()
                .idAsignacion(idAsignacion)
                .motivo("cancelacion cliente")
                .build();

        when(sagaService.procesarLiberacionAsignacion(event))
                .thenReturn(VehicleReleaseResult.processed(idAsignacion, idReserva));

        consumer.receiveVehicleRelease(event);

        verify(sagaService).procesarLiberacionAsignacion(event);
    }

    @Test
    void ignoraEventoNoProcesadoSinError() {
        VehicleReleaseEvent event = VehicleReleaseEvent.builder().motivo("x").build();
        when(sagaService.procesarLiberacionAsignacion(event))
                .thenReturn(VehicleReleaseResult.ignored("sin id"));

        consumer.receiveVehicleRelease(event);

        verify(sagaService).procesarLiberacionAsignacion(event);
    }

    @Test
    void relanzaExcepcionParaRetryKafka() {
        VehicleReleaseEvent event = VehicleReleaseEvent.builder()
                .idAsignacion(UUID.randomUUID())
                .motivo("cancelacion")
                .build();
        when(sagaService.procesarLiberacionAsignacion(event))
                .thenThrow(new RuntimeException("bd caida"));

        assertThrows(RuntimeException.class, () -> consumer.receiveVehicleRelease(event));
    }
}
