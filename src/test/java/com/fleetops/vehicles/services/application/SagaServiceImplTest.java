package com.fleetops.vehicles.services.application;

import com.fleetops.vehicles.dto.response.ReservaResponse;
import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleReleaseEvent;
import com.fleetops.vehicles.mapper.DtoMapperReserva;
import com.fleetops.vehicles.mapper.DtoMapperSaga;
import com.fleetops.vehicles.models.entities.*;
import com.fleetops.vehicles.repositories.*;
import com.fleetops.vehicles.services.domain.AvailabilityPolicy;
import com.fleetops.vehicles.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
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
    @Mock private AvailabilityPolicy availabilityPolicy;

    @InjectMocks private SagaServiceImpl service;

    private Vehiculo vehiculo;
    private VehicleRequestEvent event;

    @BeforeEach
    void setUp() {
        vehiculo = TestDataFactory.vehiculoDisponible();
        event = new VehicleRequestEvent();
        event.setIdSaga(UUID.randomUUID());
        event.setIdAsignacion(UUID.randomUUID());
        event.setTipoVehiculo("Camion");
        event.setFechaInicio(LocalDate.now().plusDays(1));
        event.setFechaFin(LocalDate.now().plusDays(3));
    }

    private void sinReservaPrevia() {
        when(reservaRepository.findByClaveIdempotencia(anyString())).thenReturn(Optional.empty());
        when(reservaRepository.findByIdAsignacionExt(any())).thenReturn(Optional.empty());
    }

    @Test
    void procesarSolicitudAsignacionCreaReservaPendiente() {
        sinReservaPrevia();
        when(vehicleRepository.findByActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase("Camion"))
                .thenReturn(List.of(vehiculo));
        when(availabilityPolicy.isAssignable(any(), any(), any())).thenReturn(true);
        when(vehicleRepository.findByIdForUpdate(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(sagaRepository.save(any())).thenAnswer(inv -> {
            SagaVehiculo s = inv.getArgument(0);
            s.setIdSaga(UUID.randomUUID());
            return s;
        });
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.procesarSolicitudAsignacion(event);

        assertTrue(result.isSuccess());
        assertFalse(result.isIdempotentReplay());
        verify(vehicleRepository).findByIdForUpdate(vehiculo.getIdVehiculo());
        verify(reservaRepository).save(argThat(r -> r.getEstadoReserva() == EstadoReserva.PENDIENTE));
        verify(reservaRepository).save(argThat(r ->
            r.getEstadoReserva() == EstadoReserva.PENDIENTE &&
            r.getSagaVehiculo() != null &&
            r.getSagaVehiculo().getEstadoSaga() == EstadoSaga.EN_PROGRESO
        ));
    }

    @Test
    void procesarSolicitudAsignacionSinVehiculos() {
        sinReservaPrevia();
        when(vehicleRepository.findByActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase("Camion"))
                .thenReturn(List.of());

        var result = service.procesarSolicitudAsignacion(event);
        assertFalse(result.isSuccess());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void procesarSolicitudAsignacionReintentoIdempotenteConfirmada() {
        ReservaVehiculo existente = TestDataFactory.reserva(vehiculo, EstadoReserva.CONFIRMADA);
        existente.setIdAsignacionExt(event.getIdAsignacion());
        when(reservaRepository.findByClaveIdempotencia(event.getIdSaga().toString()))
                .thenReturn(Optional.of(existente));

        var result = service.procesarSolicitudAsignacion(event);

        assertTrue(result.isSuccess());
        assertTrue(result.isIdempotentReplay());
        assertEquals(vehiculo.getIdVehiculo(), result.getIdVehiculo());
        verify(reservaRepository, never()).save(any());
        verify(vehicleRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void procesarSolicitudAsignacionReintentoIdempotenteCancelada() {
        ReservaVehiculo existente = TestDataFactory.reserva(vehiculo, EstadoReserva.CANCELADA);
        when(reservaRepository.findByClaveIdempotencia(event.getIdSaga().toString()))
                .thenReturn(Optional.of(existente));

        var result = service.procesarSolicitudAsignacion(event);

        assertFalse(result.isSuccess());
        assertTrue(result.isIdempotentReplay());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void procesarSolicitudAsignacionRevalidaTrasLock() {
        sinReservaPrevia();
        Vehiculo otro = TestDataFactory.vehiculoDisponible();
        otro.setIdVehiculo(UUID.randomUUID());
        otro.setNumeroPlaca("AAA111");

        when(vehicleRepository.findByActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase("Camion"))
                .thenReturn(List.of(otro, vehiculo));
        when(availabilityPolicy.isAssignable(eq(otro), any(), any())).thenReturn(true, false);
        when(availabilityPolicy.isAssignable(eq(vehiculo), any(), any())).thenReturn(true);
        when(vehicleRepository.findByIdForUpdate(otro.getIdVehiculo())).thenReturn(Optional.of(otro));
        when(vehicleRepository.findByIdForUpdate(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(sagaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.procesarSolicitudAsignacion(event);

        assertTrue(result.isSuccess());
        assertEquals(vehiculo.getIdVehiculo(), result.getIdVehiculo());
        verify(vehicleRepository).findByIdForUpdate(otro.getIdVehiculo());
        verify(vehicleRepository).findByIdForUpdate(vehiculo.getIdVehiculo());
    }

    @Test
    void procesarSolicitudAsignacionColisionInsertResuelveIdempotencia() {
        sinReservaPrevia();
        ReservaVehiculo existente = TestDataFactory.reserva(vehiculo, EstadoReserva.CONFIRMADA);
        existente.setIdAsignacionExt(event.getIdAsignacion());

        when(vehicleRepository.findByActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase("Camion"))
                .thenReturn(List.of(vehiculo));
        when(availabilityPolicy.isAssignable(any(), any(), any())).thenReturn(true);
        when(vehicleRepository.findByIdForUpdate(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(sagaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservaRepository.save(any())).thenThrow(new DataIntegrityViolationException("dup"));
        when(reservaRepository.findByClaveIdempotencia(event.getIdSaga().toString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existente));

        var result = service.procesarSolicitudAsignacion(event);

        assertTrue(result.isSuccess());
        assertTrue(result.isIdempotentReplay());
    }

    @Test
    void procesarSolicitudAsignacionValidaEvento() {
        event.setIdSaga(null);
        assertFalse(service.procesarSolicitudAsignacion(event).isSuccess());

        event.setIdSaga(UUID.randomUUID());
        event.setFechaFin(event.getFechaInicio());
        assertFalse(service.procesarSolicitudAsignacion(event).isSuccess());

        event.setFechaFin(LocalDate.now().plusDays(3));
        event.setTipoVehiculo("  ");
        assertFalse(service.procesarSolicitudAsignacion(event).isSuccess());
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
        when(reservaRepository.findById(any())).thenReturn(Optional.empty());
        when(sagaRepository.findAllByOrderByCreadoEnDesc(any())).thenReturn(new PageImpl<>(List.of()));

        var pageable = PageRequest.of(0, 10);
        assertEquals(0, service.findAllReservas(pageable).getTotalElements());
        assertEquals(0, service.findReservasPendientes(pageable).getTotalElements());
        assertTrue(service.findReservaById(UUID.randomUUID()).isEmpty());
        assertEquals(0, service.findAllSagas(pageable).getTotalElements());
    }

    @Test
    void compensarSinSaga() {
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.CONFIRMADA);
        reserva.setSagaVehiculo(null);
        when(reservaRepository.findById(reserva.getIdReserva())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);

        assertNotNull(service.compensarPorReservaId(reserva.getIdReserva(), "sin saga"));
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

        assertEquals(1, service.cancelarReservasPorPlaca("ABC123", "emergencia").size());
    }

    @Test
    void compensarSagaNoEncontrada() {
        when(sagaRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.compensarSaga(UUID.randomUUID(), "x"));
    }

    @Test
    void procesarLiberacionAsignacionInvalida() {
        assertFalse(service.procesarLiberacionAsignacion(null).isProcessed());
        assertFalse(service.procesarLiberacionAsignacion(
                VehicleReleaseEvent.builder().motivo("x").build()).isProcessed());
        assertFalse(service.procesarLiberacionAsignacion(
                VehicleReleaseEvent.builder().idAsignacion(UUID.randomUUID()).build()).isProcessed());
    }

    @Test
    void procesarLiberacionAsignacionSinReservaLocal() {
        UUID idAsignacion = UUID.randomUUID();
        when(reservaRepository.findByIdAsignacionExt(idAsignacion)).thenReturn(Optional.empty());

        var result = service.procesarLiberacionAsignacion(VehicleReleaseEvent.builder()
                .idAsignacion(idAsignacion)
                .motivo("cancelacion")
                .build());

        assertFalse(result.isProcessed());
    }

    @Test
    void procesarLiberacionAsignacionIdempotente() {
        UUID idAsignacion = UUID.randomUUID();
        ReservaVehiculo cancelada = TestDataFactory.reserva(vehiculo, EstadoReserva.CANCELADA);
        cancelada.setIdAsignacionExt(idAsignacion);

        when(reservaRepository.findByIdAsignacionExt(idAsignacion)).thenReturn(Optional.of(cancelada));

        var result = service.procesarLiberacionAsignacion(VehicleReleaseEvent.builder()
                .idAsignacion(idAsignacion)
                .motivo("cancelacion")
                .build());

        assertTrue(result.isProcessed());
        assertTrue(result.isIdempotentReplay());
    }

    @Test
    void procesarLiberacionAsignacionCompensaReserva() {
        UUID idAsignacion = UUID.randomUUID();
        SagaVehiculo saga = TestDataFactory.saga(vehiculo, EstadoSaga.COMPLETADA);
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.CONFIRMADA);
        reserva.setIdAsignacionExt(idAsignacion);
        reserva.setSagaVehiculo(saga);

        when(reservaRepository.findByIdAsignacionExt(idAsignacion)).thenReturn(Optional.of(reserva));
        when(reservaRepository.findById(reserva.getIdReserva())).thenReturn(Optional.of(reserva));
        when(sagaRepository.findById(saga.getIdSaga())).thenReturn(Optional.of(saga));
        when(reservaRepository.findBySagaVehiculo_IdSaga(saga.getIdSaga())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);
        when(sagaRepository.save(any())).thenReturn(saga);

        var result = service.procesarLiberacionAsignacion(VehicleReleaseEvent.builder()
                .idAsignacion(idAsignacion)
                .motivo("cancelacion")
                .origen("ASIGNACIONES")
                .build());

        assertTrue(result.isProcessed());
        assertFalse(result.isIdempotentReplay());
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstadoReserva());
    }

    @Test
    void confirmarReservaPorAsignacionConfirmaReservaYSaga() {

        // Arrange
        ReservaVehiculo reserva = TestDataFactory.reserva(
                vehiculo,
                EstadoReserva.PENDIENTE
        );

        SagaVehiculo saga = new SagaVehiculo();
        saga.setEstadoSaga(EstadoSaga.EN_PROGRESO);

        reserva.setSagaVehiculo(saga);
        reserva.setIdAsignacionExt(UUID.randomUUID());

        when(reservaRepository.findByIdAsignacionExt(reserva.getIdAsignacionExt()))
                .thenReturn(Optional.of(reserva));

        when(reservaRepository.save(any(ReservaVehiculo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(sagaRepository.save(any(SagaVehiculo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.confirmarReservaPorAsignacion(reserva.getIdAsignacionExt());

        // Assert
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstadoReserva());
        assertEquals(EstadoSaga.COMPLETADA, saga.getEstadoSaga());

        verify(reservaRepository).save(reserva);
        verify(sagaRepository).save(saga);
    }

    @Test
    void confirmarReservaPorAsignacionLanzaExcepcionSiNoExiste() {

        UUID idAsignacion = UUID.randomUUID();

        when(reservaRepository.findByIdAsignacionExt(idAsignacion))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.confirmarReservaPorAsignacion(idAsignacion)
        );

        verify(reservaRepository).findByIdAsignacionExt(idAsignacion);
        verifyNoMoreInteractions(sagaRepository);
    }
}
