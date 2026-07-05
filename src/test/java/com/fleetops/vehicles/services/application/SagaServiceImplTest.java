package com.fleetops.vehicles.services.application;

import com.fleetops.vehicles.dto.request.ReservaRequest;
import com.fleetops.vehicles.dto.request.UpdateReservaDatesRequest;
import com.fleetops.vehicles.dto.response.ReservaResponse;
import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.ReservaConflictException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.mapper.DtoMapperReserva;
import com.fleetops.vehicles.mapper.DtoMapperSaga;
import com.fleetops.vehicles.models.entities.*;
import com.fleetops.vehicles.repositories.*;
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
    @Mock private HistorialEstadoRepository historialEstadoRepository;
    @Mock private IdempotencyValidator idempotencyValidator;

    @InjectMocks private SagaServiceImpl service;

    private Vehiculo vehiculo;
    private ReservaRequest request;

    @BeforeEach
    void setUp() {
        vehiculo = TestDataFactory.vehiculoDisponible();
        request = TestDataFactory.reservaRequest();
    }

    @Test
    void iniciarReservaExitosa() {
        when(vehicleRepository.findById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        doNothing().when(idempotencyValidator).validateNotDuplicate(anyString());
        when(reservaRepository.obtenerReservasConflictivas(any(), anyList(), any(), any())).thenReturn(List.of());
        when(sagaRepository.save(any())).thenAnswer(inv -> {
            SagaVehiculo s = inv.getArgument(0);
            s.setIdSaga(UUID.randomUUID());
            return s;
        });
        when(reservaRepository.save(any())).thenAnswer(inv -> {
            ReservaVehiculo r = inv.getArgument(0);
            r.setIdReserva(UUID.randomUUID());
            return r;
        });

        ReservaResponse response = service.iniciarReserva(vehiculo.getIdVehiculo(), request);
        assertNotNull(response);
        assertEquals(EstadoReserva.PENDIENTE.name(), response.estadoReserva());
    }

    @Test
    void iniciarReservaByPlaca() {
        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("ABC123"))
                .thenReturn(Optional.of(vehiculo));
        doNothing().when(idempotencyValidator).validateNotDuplicate(anyString());
        when(reservaRepository.obtenerReservasConflictivas(any(), anyList(), any(), any())).thenReturn(List.of());
        when(sagaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertNotNull(service.iniciarReservaByPlaca("ABC123", request));
    }

    @Test
    void iniciarReservaRechazaSoatYEstado() {
        doNothing().when(idempotencyValidator).validateNotDuplicate(anyString());
        when(vehicleRepository.findById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));

        vehiculo.setFechaSoat(LocalDate.now().minusDays(1));
        assertThrows(BusinessException.class, () -> service.iniciarReserva(vehiculo.getIdVehiculo(), request));

        //vehiculo.setFechaSoat(LocalDate.now().plusMonths(6));
        // vehiculo.setEstadoVehiculo(EstadoVehiculo.RESERVADO);
        // when(reservaRepository.findByVehiculo_IdVehiculoAndEstadoReservaIn(any(), anyList()))
        //         .thenReturn(List.of());
        // assertThrows(ReservaConflictException.class, () -> service.iniciarReserva(vehiculo.getIdVehiculo(), request));
    }

    @Test
    void iniciarReservaRechazaSolapamiento() {
        doNothing().when(idempotencyValidator).validateNotDuplicate(anyString());
        when(vehicleRepository.findById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(reservaRepository.obtenerReservasConflictivas(any(), anyList(), any(), any()))
                .thenReturn(List.of(TestDataFactory.reserva(vehiculo, EstadoReserva.CONFIRMADA)));

        assertThrows(ReservaConflictException.class, () -> service.iniciarReserva(vehiculo.getIdVehiculo(), request));
    }

    @Test
    void confirmarReservaYPorPlaca() {
        SagaVehiculo saga = TestDataFactory.saga(vehiculo, EstadoSaga.EN_PROGRESO);
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.PENDIENTE);
        reserva.setSagaVehiculo(saga);

        when(reservaRepository.findById(reserva.getIdReserva())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);
        when(sagaRepository.save(any())).thenReturn(saga);

        assertTrue(service.confirmarReserva(reserva.getIdReserva()).isPresent());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstadoReserva());

        when(reservaRepository.findAllByVehiculoNumeroPlacaIgnoreCaseAndEstadoReserva("ABC123", EstadoReserva.PENDIENTE))
                .thenReturn(List.of(reserva));
        when(reservaRepository.saveAll(anyList())).thenReturn(List.of(reserva));
        assertEquals(1, service.confirmarReservaPorPlaca("ABC123").size());
    }

    @Test
    void confirmarReservaFallaSiSagaNoEnProgreso() {
        SagaVehiculo saga = TestDataFactory.saga(vehiculo, EstadoSaga.COMPLETADA);
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.PENDIENTE);
        reserva.setSagaVehiculo(saga);
        when(reservaRepository.findById(reserva.getIdReserva())).thenReturn(Optional.of(reserva));

        assertThrows(BusinessException.class, () -> service.confirmarReserva(reserva.getIdReserva()));
    }

    @Test
    void actualizarFechasReserva() {
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.PENDIENTE);
        when(reservaRepository.findById(reserva.getIdReserva())).thenReturn(Optional.of(reserva));
        when(reservaRepository.findOverlappingReservations(any(), any(), any(), any(), anyList()))
                .thenReturn(List.of());
        when(reservaRepository.save(any())).thenReturn(reserva);
        when(dtoMapperReserva.toDto(any())).thenReturn(mock(ReservaResponse.class));

        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        UpdateReservaDatesRequest dates = new UpdateReservaDatesRequest(inicio, inicio.plusDays(2));
        assertNotNull(service.actualizarFechasReserva(reserva.getIdReserva(), dates));
    }

    @Test
    void compensarPorReservaIdYSaga() {
        SagaVehiculo saga = TestDataFactory.saga(vehiculo, EstadoSaga.EN_PROGRESO);
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.PENDIENTE);
        reserva.setSagaVehiculo(saga);
        vehiculo.setEstadoVehiculo(EstadoVehiculo.RESERVADO);

        when(reservaRepository.findById(reserva.getIdReserva())).thenReturn(Optional.of(reserva));
        when(sagaRepository.findById(saga.getIdSaga())).thenReturn(Optional.of(saga));
        when(reservaRepository.findBySagaVehiculo_IdSaga(saga.getIdSaga())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);
        when(vehicleRepository.save(any())).thenReturn(vehiculo);
        when(sagaRepository.save(any())).thenReturn(saga);

        assertNotNull(service.compensarPorReservaId(reserva.getIdReserva(), "timeout"));
        assertEquals(EstadoSaga.COMPENSADA, saga.getEstadoSaga());
        assertEquals(EstadoVehiculo.DISPONIBLE, vehiculo.getEstadoVehiculo());
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
        assertEquals(0, service.findSagasByPlacaAndEstado("ABC", EstadoSaga.EN_PROGRESO, pageable).getTotalElements());
    }

    @Test
    void confirmarReservaPorPlacaSinPendientes() {
        when(reservaRepository.findAllByVehiculoNumeroPlacaIgnoreCaseAndEstadoReserva("ZZZ", EstadoReserva.PENDIENTE))
                .thenReturn(List.of());
        assertThrows(BusinessException.class, () -> service.confirmarReservaPorPlaca("ZZZ"));
    }

    @Test
    void iniciarReservaValidaDocumentosYEstados() {
        doNothing().when(idempotencyValidator).validateNotDuplicate(anyString());
        when(vehicleRepository.findById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));

        vehiculo.setFechaSoat(null);
        assertThrows(BusinessException.class, () -> service.iniciarReserva(vehiculo.getIdVehiculo(), request));

        vehiculo.setFechaSoat(LocalDate.now().plusDays(3));
        assertThrows(BusinessException.class, () -> service.iniciarReserva(vehiculo.getIdVehiculo(), request));

        vehiculo.setFechaSoat(LocalDate.now().plusMonths(6));
        vehiculo.setFechaRtm(null);
        assertThrows(BusinessException.class, () -> service.iniciarReserva(vehiculo.getIdVehiculo(), request));

        vehiculo.setFechaRtm(LocalDate.now().minusDays(1));
        assertThrows(BusinessException.class, () -> service.iniciarReserva(vehiculo.getIdVehiculo(), request));

        vehiculo.setFechaRtm(LocalDate.now().plusDays(2));
        assertThrows(BusinessException.class, () -> service.iniciarReserva(vehiculo.getIdVehiculo(), request));

        vehiculo.setFechaRtm(LocalDate.now().plusMonths(6));
        vehiculo.setEstadoVehiculo(EstadoVehiculo.FUERA_DE_SERVICIO);
        // when(reservaRepository.findByVehiculo_IdVehiculoAndEstadoReservaIn(any(), anyList()))
        //         .thenReturn(List.of(TestDataFactory.reserva(vehiculo, EstadoReserva.CONFIRMADA)));
        ReservaConflictException fuera = assertThrows(ReservaConflictException.class,
                () -> service.iniciarReserva(vehiculo.getIdVehiculo(), request));
        assertTrue(fuera.getMessage().contains("fuera de servicio"));
        //assertEquals(1, fuera.getReservas().size());

        vehiculo.setEstadoVehiculo(EstadoVehiculo.EN_MANTENIMIENTO);
        ReservaConflictException mant = assertThrows(ReservaConflictException.class,
                () -> service.iniciarReserva(vehiculo.getIdVehiculo(), request));
        assertTrue(mant.getMessage().contains("mantenimiento"));
    }

    @Test
    void iniciarReservaVehiculoNoEncontrado() {
        doNothing().when(idempotencyValidator).validateNotDuplicate(anyString());
        when(vehicleRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.iniciarReserva(UUID.randomUUID(), request));

        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("XXX"))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.iniciarReservaByPlaca("XXX", request));
    }

    @Test
    void actualizarFechasValidaErroresYColisiones() {
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.PENDIENTE);
        when(reservaRepository.findById(reserva.getIdReserva())).thenReturn(Optional.of(reserva));

        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        assertThrows(BusinessException.class, () -> service.actualizarFechasReserva(
                reserva.getIdReserva(), new UpdateReservaDatesRequest(inicio, inicio)));
        assertThrows(BusinessException.class, () -> service.actualizarFechasReserva(
                reserva.getIdReserva(), new UpdateReservaDatesRequest(inicio, inicio.minusHours(1))));

        reserva.setEstadoReserva(EstadoReserva.CANCELADA);
        assertThrows(BusinessException.class, () -> service.actualizarFechasReserva(
                reserva.getIdReserva(), new UpdateReservaDatesRequest(inicio, inicio.plusDays(1))));

        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        when(reservaRepository.findOverlappingReservations(any(), any(), any(), any(), anyList()))
                .thenReturn(List.of(TestDataFactory.reserva(vehiculo, EstadoReserva.PENDIENTE)));
        assertThrows(ReservaConflictException.class, () -> service.actualizarFechasReserva(
                reserva.getIdReserva(), new UpdateReservaDatesRequest(inicio, inicio.plusDays(1))));

        when(reservaRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.actualizarFechasReserva(
                UUID.randomUUID(), new UpdateReservaDatesRequest(inicio, inicio.plusDays(1))));
    }

    @Test
    void compensarSinSagaYVehiculoEnMantenimiento() {
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.PENDIENTE);
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
    void compensarSagaNoLiberaVehiculoEnMantenimiento() {
        vehiculo.setEstadoVehiculo(EstadoVehiculo.EN_MANTENIMIENTO);
        SagaVehiculo saga = TestDataFactory.saga(vehiculo, EstadoSaga.EN_PROGRESO);
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.PENDIENTE);
        reserva.setSagaVehiculo(saga);

        when(sagaRepository.findById(saga.getIdSaga())).thenReturn(Optional.of(saga));
        when(reservaRepository.findBySagaVehiculo_IdSaga(saga.getIdSaga())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);
        when(sagaRepository.save(any())).thenReturn(saga);

        assertTrue(service.compensarSaga(saga.getIdSaga(), "taller"));
        assertEquals(EstadoVehiculo.EN_MANTENIMIENTO, vehiculo.getEstadoVehiculo());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void cancelarReservasCortaViajeEnCurso() {
        vehiculo.setEstadoVehiculo(EstadoVehiculo.RESERVADO);
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
        when(vehicleRepository.save(any())).thenReturn(vehiculo);
        when(sagaRepository.save(any())).thenReturn(saga);

        List<ReservaResponse> result = service.cancelarReservasPorPlaca("ABC123", "emergencia");
        assertEquals(1, result.size());
        assertEquals(EstadoReserva.CANCELADA, enCurso.getEstadoReserva());
    }

    @Test
    void confirmarReservaPorPlacaConSagaNoEnProgreso() {
        ReservaVehiculo reserva = TestDataFactory.reserva(vehiculo, EstadoReserva.PENDIENTE);
        SagaVehiculo saga = TestDataFactory.saga(vehiculo, EstadoSaga.INICIADA);
        reserva.setSagaVehiculo(saga);

        when(reservaRepository.findAllByVehiculoNumeroPlacaIgnoreCaseAndEstadoReserva("ABC123", EstadoReserva.PENDIENTE))
                .thenReturn(List.of(reserva));
        when(reservaRepository.saveAll(anyList())).thenReturn(List.of(reserva));

        assertEquals(1, service.confirmarReservaPorPlaca("ABC123").size());
        verify(sagaRepository, never()).save(any());
    }

    @Test
    void compensarSagaNoEncontrada() {
        when(sagaRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.compensarSaga(UUID.randomUUID(), "x"));
    }

    @Test
    void confirmarReservaPorPlacaConSagaEnProgresoYSinSaga() {
        ReservaVehiculo conSaga = TestDataFactory.reserva(vehiculo, EstadoReserva.PENDIENTE);
        SagaVehiculo saga = TestDataFactory.saga(vehiculo, EstadoSaga.EN_PROGRESO);
        conSaga.setSagaVehiculo(saga);

        ReservaVehiculo sinSaga = TestDataFactory.reserva(vehiculo, EstadoReserva.PENDIENTE);
        sinSaga.setSagaVehiculo(null);

        when(reservaRepository.findAllByVehiculoNumeroPlacaIgnoreCaseAndEstadoReserva("ABC123", EstadoReserva.PENDIENTE))
                .thenReturn(List.of(conSaga, sinSaga));
        when(reservaRepository.saveAll(anyList())).thenReturn(List.of(conSaga, sinSaga));
        when(sagaRepository.save(any())).thenReturn(saga);

        assertEquals(2, service.confirmarReservaPorPlaca("ABC123").size());
        assertEquals(EstadoSaga.COMPLETADA, saga.getEstadoSaga());
        verify(sagaRepository, times(1)).save(saga);
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
        vehiculo.setEstadoVehiculo(EstadoVehiculo.DISPONIBLE);
        SagaVehiculo saga = TestDataFactory.saga(vehiculo, EstadoSaga.EN_PROGRESO);

        when(sagaRepository.findById(saga.getIdSaga())).thenReturn(Optional.of(saga));
        when(reservaRepository.findBySagaVehiculo_IdSaga(saga.getIdSaga())).thenReturn(Optional.empty());
        when(vehicleRepository.save(any())).thenReturn(vehiculo);
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
