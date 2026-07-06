package com.fleetops.vehicles.infrastructure.messaging.consumer;

import com.fleetops.vehicles.infrastructure.messaging.dto.AssignmentCompletedEvent;
import com.fleetops.vehicles.services.application.SagaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssignmentCompletedConsumerTest {

    @Mock private SagaService sagaService;
    @InjectMocks private AssignmentCompletedConsumer consumer;

    @Test
    void registraAckSinConfirmarReserva() {
        UUID idAsignacion = UUID.randomUUID();
        AssignmentCompletedEvent event = AssignmentCompletedEvent.builder()
                .idSaga(UUID.randomUUID())
                .idAsignacion(idAsignacion)
                .idVehiculo(UUID.randomUUID())
                .idConductor(UUID.randomUUID())
                .build();

        consumer.receive(event);

        verify(sagaService).registrarAckAsignacion(idAsignacion);
    }
}
