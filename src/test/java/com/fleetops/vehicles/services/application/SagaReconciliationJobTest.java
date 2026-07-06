package com.fleetops.vehicles.services.application;

import com.fleetops.vehicles.infrastructure.messaging.VehicleAssignmentCoordinator;
import com.fleetops.vehicles.models.entities.EstadoReserva;
import com.fleetops.vehicles.models.entities.EstadoSaga;
import com.fleetops.vehicles.models.entities.ReservaVehiculo;
import com.fleetops.vehicles.models.entities.SagaVehiculo;
import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SagaReconciliationJobTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private SagaService sagaService;
    @Mock private VehicleAssignmentCoordinator assignmentCoordinator;

    @InjectMocks private SagaReconciliationJob job;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(job, "graciaMinutos", 30);
        ReflectionTestUtils.setField(job, "compensarDespuesMinutos", 240);
        ReflectionTestUtils.setField(job, "maxReconfirmaciones", 3);
    }

    @Test
    void republicaConfirmadoParaReservasSinAck() {
        var vehiculo = TestDataFactory.vehiculoDisponible();
        SagaVehiculo saga = TestDataFactory.saga(vehiculo, EstadoSaga.COMPLETADA);
        saga.setAsignacionesAck(false);
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.CONFIRMADA);
        reserva.setSagaVehiculo(saga);
        reserva.setCreadoEn(LocalDateTime.now().minusHours(2));

        when(reservaRepository.findConfirmadasSinAckAntesDe(any(), eq(3)))
                .thenReturn(List.of(reserva));
        when(reservaRepository.findConfirmadasSinAckParaCompensar(any(), eq(3)))
                .thenReturn(List.of());

        job.ejecutar();

        verify(assignmentCoordinator).republicarConfirmado(
                reserva.getIdAsignacionExt(),
                vehiculo.getIdVehiculo(),
                reserva.getIdReserva());
    }

    @Test
    void compensaReservasSinAckTrasMaxReintentos() {
        var vehiculo = TestDataFactory.vehiculoDisponible();
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.CONFIRMADA);
        reserva.setCreadoEn(LocalDateTime.now().minusHours(5));

        when(reservaRepository.findConfirmadasSinAckAntesDe(any(), anyInt()))
                .thenReturn(List.of());
        when(reservaRepository.findConfirmadasSinAckParaCompensar(any(), eq(3)))
                .thenReturn(List.of(reserva));

        job.ejecutar();

        verify(sagaService).compensarPorReservaId(eq(reserva.getIdReserva()), contains("Auto-compensación"));
    }
}
