package com.fleetops.vehicles.services.application;

import com.fleetops.vehicles.dto.response.ReservaResponse;
import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;
import com.fleetops.vehicles.mapper.DtoMapperReserva;
import com.fleetops.vehicles.mapper.DtoMapperSaga;
import com.fleetops.vehicles.models.entities.*;
import com.fleetops.vehicles.repositories.*;
import com.fleetops.vehicles.services.domain.AvailabilityPolicy;
import com.fleetops.vehicles.services.domain.IdempotencyValidator;
import com.fleetops.vehicles.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SagaServiceImplTest {

    @Mock private DtoMapperReserva dtoMapperReserva;
    @Mock private DtoMapperSaga dtoMapperSaga;
    @Mock private SagaRepository sagaRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private IdempotencyValidator idempotencyValidator;
    @Mock private AvailabilityPolicy availabilityPolicy;

    @InjectMocks private SagaServiceImpl service;

    private Vehiculo vehiculo;

    @BeforeEach
    void setUp() {
        vehiculo = TestDataFactory.vehiculoDisponible();
    }

    @Test
    void procesarSolicitudAsignacionCreaReservaConfirmada() {
        VehicleRequestEvent event = new VehicleRequestEvent();
        event.setIdSaga(UUID.randomUUID());
        event.setIdAsignacion(UUID.randomUUID());
        event.setTipoVehiculo("Camion");
        event.setFechaInicio(LocalDate.now().plusDays(1));
        event.setFechaFin(LocalDate.now().plusDays(3));

        doNothing().when(idempotencyValidator).validateNotDuplicate(anyString());
        when(vehicleRepository.findByActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase("Camion"))
                .thenReturn(List.of(vehiculo));
        when(availabilityPolicy.isAssignable(any(), any(), any())).thenReturn(true);
        when(sagaRepository.save(any())).thenAnswer(inv -> {
            SagaVehiculo s = inv.getArgument(0);
            s.setIdSaga(UUID.randomUUID());
            return s;
        });
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.procesarSolicitudAsignacion(event);

        assertTrue(result.isSuccess());
        verify(reservaRepository).save(argThat(r -> r.getEstadoReserva() == EstadoReserva.CONFIRMADA));
        verify(sagaRepository).save(argThat(s -> s.getEstadoSaga() == EstadoSaga.COMPLETADA));
    }

    @Test
    void procesarSolicitudAsignacionSinVehiculos() {
        VehicleRequestEvent event = new VehicleRequestEvent();
        event.setIdSaga(UUID.randomUUID());
        event.setIdAsignacion(UUID.randomUUID());
        event.setTipoVehiculo("Camion");
        event.setFechaInicio(LocalDate.now().plusDays(1));
        event.setFechaFin(LocalDate.now().plusDays(3));

        doNothing().when(idempotencyValidator).validateNotDuplicate(anyString());
        when(vehicleRepository.findByActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase("Camion"))
                .thenReturn(List.of());

        var result = service.procesarSolicitudAsignacion(event);
        assertFalse(result.isSuccess());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void compensarPorReservaIdYSaga() {
        SagaVehiculo saga = TestDataFactory.saga(vehiculo, EstadoSaga.COMPLETADA);
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.CONFIRMADA);
        reserva.setSagaVehiculo(saga);

        when(reservaRepository.findById(reserva.getIdReserva())).thenReturn(Optional.of(reserva));
        when(sagaRepository.findById(saga.getIdSaga())).thenReturn(Optional.of(saga));
        when(reservaRepository.findBySagaVehiculo_IdSaga(saga.getIdSaga())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);
        when(sagaRepository.save(any())).thenReturn(saga);

        assertNotNull(service.compensarPorReservaId(reserva.getIdReserva(), "cancelacion"));
        assertEquals(EstadoSaga.COMPENSADA, saga.getEstadoSaga());
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstadoReserva());
    }

    @Test
    void compensarSagaYaCompensadaYCompletadaAntigua() {
        SagaVehiculo compensada = TestDataFactory.saga(vehiculo, EstadoSaga.COMPENSADA);
        when(sagaRepository.findById(compensada.getIdSaga())).thenReturn(Optional.of(compensada));
        assertTrue(service.compensarSaga(compensada.getIdSaga(), "x"));

        SagaVehiculo antigua = TestDataFactory.saga(vehiculo, EstadoSaga.COMPLETADA);
        antigua.setActualizadoEn(LocalDateTime.now().minusDays(20));
        when(sagaRepository.findById(antigua.getIdSaga())).thenReturn(Optional.of(antigua));
        assertThrows(BusinessException.class, () -> service.compensarSaga(antigua.getIdSaga(), "x"));
    }

    @Test
    void cancelarReservasPorPlaca() {
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.CONFIRMADA);
        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("ABC123"))
                .thenReturn(Optional.of(vehiculo));
        when(reservaRepository.findByVehiculo_IdVehiculoAndEstadoReservaIn(any(), anyList()))
                .thenReturn(List.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);

        assertEquals(1, service.cancelarReservasPorPlaca("ABC123", "fuerza mayor").size());
    }

    @Test
    void listadosReservasYSagas() {
        when(reservaRepository.findAllByOrderByCreadoEnDesc(any())).thenReturn(new PageImpl<>(List.of()));
        when(reservaRepository.findAllByEstadoReservaOrderByCreadoEnDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(reservaRepository.findByVehiculo_NumeroPlacaIgnoreCaseOrderByCreadoEnDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(reservaRepository.findByVehiculo_NumeroPlacaIgnoreCaseAndEstadoReservaOrderByCreadoEnDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(reservaRepository.findById(any())).thenReturn(Optional.empty());

        var pageable = PageRequest.of(0, 10);
        assertEquals(0, service.findAllReservas(pageable).getTotalElements());
        assertEquals(0, service.findReservasPendientes(pageable).getTotalElements());
        assertEquals(0, service.findReservasConfirmadas(pageable).getTotalElements());
        assertEquals(0, service.findReservasFallidas(pageable).getTotalElements());
        assertEquals(0, service.findReservasCanceladas(pageable).getTotalElements());
        assertEquals(0, service.findReservasByPlaca("ABC", pageable).getTotalElements());
        assertEquals(0, service.findReservasByPlacaAndEstado("ABC", EstadoReserva.PENDIENTE, pageable).getTotalElements());
        assertTrue(service.findReservaById(UUID.randomUUID()).isEmpty());

        when(sagaRepository.findAllByOrderByCreadoEnDesc(any())).thenReturn(new PageImpl<>(List.of()));
        when(sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(sagaRepository.findByVehiculo_NumeroPlacaIgnoreCaseOrderByCreadoEnDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(sagaRepository.findByVehiculo_NumeroPlacaIgnoreCaseAndEstadoSagaOrderByCreadoEnDesc(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        assertEquals(0, service.findAllSagas(pageable).getTotalElements());
        assertEquals(0, service.findSagasIniciadas(pageable).getTotalElements());
        assertEquals(0, service.findSagasEnProgreso(pageable).getTotalElements());
        assertEquals(0, service.findSagasCompletadas(pageable).getTotalElements());
        assertEquals(0, service.findSagasFallidas(pageable).getTotalElements());
        assertEquals(0, service.findSagasCompensadas(pageable).getTotalElements());
        assertEquals(0, service.findSagasByPlaca("ABC", pageable).getTotalElements());
        assertEquals(0, service.findSagasByPlacaAndEstado("ABC", EstadoSaga.COMPLETADA, pageable).getTotalElements());
    }

    @Test
    void compensarSinSaga() {
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.CONFIRMADA);
        reserva.setSagaVehiculo(null);
        when(reservaRepository.findById(reserva.getIdReserva())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);

        assertNotNull(service.compensarPorReservaId(reserva.getIdReserva(), "sin saga"));
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstadoReserva());

        when(reservaRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.compensarPorReservaId(UUID.randomUUID(), "x"));
    }

    @Test
    void cancelarReservasCortaViajeEnCurso() {
        ReservaVehiculo enCurso = TestDataFactory.reserva(vehiculo, EstadoReserva.CONFIRMADA);
        enCurso.setFechaInicio(LocalDateTime.now().minusHours(1));
        enCurso.setFechaFin(LocalDateTime.now().plusHours(2));
        SagaVehiculo saga = TestDataFactory.saga(vehiculo, EstadoSaga.COMPLETADA);
        enCurso.setSagaVehiculo(saga);

        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("ABC123"))
                .thenReturn(Optional.of(vehiculo));
        when(reservaRepository.findByVehiculo_IdVehiculoAndEstadoReservaIn(any(), anyList()))
                .thenReturn(List.of(enCurso));
        when(reservaRepository.save(any())).thenReturn(enCurso);
        when(sagaRepository.findById(saga.getIdSaga())).thenReturn(Optional.of(saga));
        when(reservaRepository.findBySagaVehiculo_IdSaga(saga.getIdSaga())).thenReturn(Optional.of(enCurso));
        when(sagaRepository.save(any())).thenReturn(saga);

        List<ReservaResponse> result = service.cancelarReservasPorPlaca("ABC123", "emergencia");
        assertEquals(1, result.size());
        assertEquals(EstadoReserva.CANCELADA, enCurso.getEstadoReserva());
    }

    @Test
    void compensarSagaNoEncontrada() {
        when(sagaRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.compensarSaga(UUID.randomUUID(), "x"));
    }

    @Test
    void cancelarReservasPorPlacaVehiculoNoExiste() {
        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("NOPE"))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.cancelarReservasPorPlaca("NOPE", "x"));
    }

    @Test
    void compensarSagaUsaUsuarioAutenticado() {
        SagaVehiculo saga = TestDataFactory.saga(vehiculo, EstadoSaga.COMPLETADA);

        when(sagaRepository.findById(saga.getIdSaga())).thenReturn(Optional.of(saga));
        when(reservaRepository.findBySagaVehiculo_IdSaga(saga.getIdSaga())).thenReturn(Optional.empty());
        when(sagaRepository.save(any())).thenReturn(saga);

        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "operador.real", null, List.of());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            assertTrue(service.compensarSaga(saga.getIdSaga(), "manual"));
            assertEquals("operador.real", saga.getCompensadoPor());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
