package com.fleetops.vehicles.service;

import com.fleetops.vehicles.dto.request.ReservaRequest;
import com.fleetops.vehicles.dto.request.UpdateReservaDatesRequest;
import com.fleetops.vehicles.dto.response.ReservaResponse;
import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.ReservaConflictException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.mapper.DtoMapperReserva;
import com.fleetops.vehicles.mapper.DtoMapperSaga;
import com.fleetops.vehicles.models.entities.EstadoReserva;
import com.fleetops.vehicles.models.entities.EstadoSaga;
import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.ReservaVehiculo;
import com.fleetops.vehicles.models.entities.SagaVehiculo;
import com.fleetops.vehicles.models.entities.TipoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.repositories.SagaRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import com.fleetops.vehicles.services.application.SagaServiceImpl;
import com.fleetops.vehicles.services.domain.AvailabilityPolicy;
import com.fleetops.vehicles.services.domain.IdempotencyValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - SagaService")
class SagaServiceTest {

    @Mock
    private SagaRepository sagaRepository;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private HistorialEstadoRepository historialEstadoRepository;
    @Mock
    private IdempotencyValidator idempotencyValidator;

    private SagaServiceImpl sagaService;
    private Vehiculo vehiculo;
    private UUID vehiculoId;

    @BeforeEach
    void setUp() {
        sagaService = new SagaServiceImpl(
                new DtoMapperReserva(),
                new DtoMapperSaga(),
                sagaRepository,
                reservaRepository,
                vehicleRepository,
                historialEstadoRepository,
                idempotencyValidator,
                new AvailabilityPolicy());

        vehiculoId = UUID.randomUUID();
        vehiculo = Vehiculo.builder()
                .idVehiculo(vehiculoId)
                .numeroPlaca("TWA101")
                .marca("Volvo")
                .modelo("FH16")
                .anioFabricacion(2022)
                .kilometraje(100000)
                .estadoVehiculo(EstadoVehiculo.DISPONIBLE)
                .activo(true)
                .fechaSoat(LocalDate.now().plusMonths(6))
                .fechaRtm(LocalDate.now().plusMonths(6))
                .tipoVehiculo(TipoVehiculo.builder()
                        .idTipoVehiculo(1L)
                        .nombreTipo("Camion")
                        .descripcion("Carga")
                        .capacidadCarga(20000.0)
                        .build())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private ReservaRequest reservaRequestValida() {
        return new ReservaRequest(
                UUID.randomUUID().toString(),
                "Juan Perez",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                "clave-" + UUID.randomUUID());
    }

    private void mockSagaAndReservaSave() {
        given(sagaRepository.save(any(SagaVehiculo.class))).willAnswer(invocation -> {
            SagaVehiculo saga = invocation.getArgument(0);
            if (saga.getIdSaga() == null) {
                saga.setIdSaga(UUID.randomUUID());
            }
            return saga;
        });
        given(reservaRepository.save(any(ReservaVehiculo.class))).willAnswer(invocation -> {
            ReservaVehiculo reserva = invocation.getArgument(0);
            if (reserva.getIdReserva() == null) {
                reserva.setIdReserva(UUID.randomUUID());
            }
            return reserva;
        });
    }

    @Test
    @DisplayName("iniciarReserva crea saga y reserva cuando el vehículo está disponible")
    void iniciarReservaExitoso() {
        ReservaRequest request = reservaRequestValida();
        doNothing().when(idempotencyValidator).validateNotDuplicate(request.claveIdempotencia());
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(reservaRepository.obtenerReservasConflictivas(any(), anyList(), any(), any()))
                .willReturn(Collections.emptyList());
        given(reservaRepository.existeReservaEnRango(any(), anyList(), any(), any())).willReturn(false);
        mockSagaAndReservaSave();

        ReservaResponse result = sagaService.iniciarReserva(vehiculoId, request);

        assertNotNull(result.idReserva());
        assertEquals("PENDIENTE", result.estadoReserva());
        assertEquals("TWA101", result.numeroPlaca());
        verify(sagaRepository, org.mockito.Mockito.atLeastOnce()).save(any(SagaVehiculo.class));
        verify(reservaRepository).save(any(ReservaVehiculo.class));
    }

    @Test
    @DisplayName("iniciarReserva lanza 404 si el vehículo no existe")
    void iniciarReservaVehiculoNoExiste() {
        UUID id = UUID.randomUUID();
        ReservaRequest request = reservaRequestValida();
        doNothing().when(idempotencyValidator).validateNotDuplicate(request.claveIdempotencia());
        given(vehicleRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sagaService.iniciarReserva(id, request));
    }

    @Test
    @DisplayName("iniciarReserva rechaza clave de idempotencia duplicada")
    void iniciarReservaIdempotenciaDuplicada() {
        ReservaRequest request = reservaRequestValida();
        doThrow(new IllegalStateException("duplicado"))
                .when(idempotencyValidator).validateNotDuplicate(request.claveIdempotencia());

        assertThrows(IllegalStateException.class, () -> sagaService.iniciarReserva(vehiculoId, request));
    }

    @Test
    @DisplayName("iniciarReserva lanza ReservaConflictException si el vehículo no está DISPONIBLE")
    void iniciarReservaVehiculoNoDisponible() {
        vehiculo.setEstadoVehiculo(EstadoVehiculo.RESERVADO);
        ReservaRequest request = reservaRequestValida();
        doNothing().when(idempotencyValidator).validateNotDuplicate(request.claveIdempotencia());
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));

        ReservaVehiculo reservaActiva = ReservaVehiculo.builder()
                .fechaInicio(LocalDateTime.now().plusDays(1))
                .fechaFin(LocalDateTime.now().plusDays(3))
                .estadoReserva(EstadoReserva.CONFIRMADA)
                .build();
        given(reservaRepository.findByVehiculo_IdVehiculoAndEstadoReservaIn(eq(vehiculoId), anyList()))
                .willReturn(List.of(reservaActiva));

        assertThrows(ReservaConflictException.class, () -> sagaService.iniciarReserva(vehiculoId, request));
    }

    @Test
    @DisplayName("iniciarReserva lanza ReservaConflictException por solapamiento de fechas")
    void iniciarReservaSolapamientoFechas() {
        ReservaRequest request = reservaRequestValida();
        doNothing().when(idempotencyValidator).validateNotDuplicate(request.claveIdempotencia());
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));

        ReservaVehiculo conflicto = ReservaVehiculo.builder()
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .estadoReserva(EstadoReserva.PENDIENTE)
                .build();
        given(reservaRepository.obtenerReservasConflictivas(any(), anyList(), any(), any()))
                .willReturn(List.of(conflicto));

        assertThrows(ReservaConflictException.class, () -> sagaService.iniciarReserva(vehiculoId, request));
    }

    @Test
    @DisplayName("iniciarReserva lanza BusinessException si SOAT está vencido")
    void iniciarReservaSoatVencido() {
        vehiculo.setFechaSoat(LocalDate.now().minusDays(1));
        ReservaRequest request = reservaRequestValida();
        doNothing().when(idempotencyValidator).validateNotDuplicate(request.claveIdempotencia());
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(reservaRepository.obtenerReservasConflictivas(any(), anyList(), any(), any()))
                .willReturn(Collections.emptyList());

        assertThrows(BusinessException.class, () -> sagaService.iniciarReserva(vehiculoId, request));
    }

    @Test
    @DisplayName("iniciarReservaByPlaca resuelve vehículo por placa")
    void iniciarReservaByPlacaExitoso() {
        ReservaRequest request = reservaRequestValida();
        doNothing().when(idempotencyValidator).validateNotDuplicate(request.claveIdempotencia());
        given(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("twa101")).willReturn(Optional.of(vehiculo));
        given(reservaRepository.obtenerReservasConflictivas(any(), anyList(), any(), any()))
                .willReturn(Collections.emptyList());
        given(reservaRepository.existeReservaEnRango(any(), anyList(), any(), any())).willReturn(false);
        mockSagaAndReservaSave();

        ReservaResponse result = sagaService.iniciarReservaByPlaca("twa101", request);

        assertEquals("TWA101", result.numeroPlaca());
    }

    @Test
    @DisplayName("confirmarReserva actualiza reserva y saga a COMPLETADA")
    void confirmarReservaExitoso() {
        UUID reservaId = UUID.randomUUID();
        SagaVehiculo saga = SagaVehiculo.builder()
                .idSaga(UUID.randomUUID())
                .estadoSaga(EstadoSaga.EN_PROGRESO)
                .vehiculo(vehiculo)
                .build();
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(reservaId)
                .vehiculo(vehiculo)
                .sagaVehiculo(saga)
                .estadoReserva(EstadoReserva.PENDIENTE)
                .idAsignacionExt(UUID.randomUUID())
                .solicitadoPor("Juan")
                .fechaInicio(LocalDateTime.now().plusDays(1))
                .fechaFin(LocalDateTime.now().plusDays(3))
                .claveIdempotencia("clave-1")
                .build();

        given(reservaRepository.findById(reservaId)).willReturn(Optional.of(reserva));
        given(reservaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(sagaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        Optional<ReservaResponse> result = sagaService.confirmarReserva(reservaId);

        assertTrue(result.isPresent());
        assertEquals("CONFIRMADA", result.get().estadoReserva());
        verify(sagaRepository).save(any(SagaVehiculo.class));
    }

    @Test
    @DisplayName("confirmarReserva lanza BusinessException si la saga no está EN_PROGRESO")
    void confirmarReservaSagaEstadoInvalido() {
        UUID reservaId = UUID.randomUUID();
        SagaVehiculo saga = SagaVehiculo.builder()
                .estadoSaga(EstadoSaga.COMPLETADA)
                .build();
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(reservaId)
                .sagaVehiculo(saga)
                .estadoReserva(EstadoReserva.PENDIENTE)
                .build();

        given(reservaRepository.findById(reservaId)).willReturn(Optional.of(reserva));

        assertThrows(BusinessException.class, () -> sagaService.confirmarReserva(reservaId));
    }

    @Test
    @DisplayName("confirmarReservaPorPlaca confirma todas las reservas pendientes")
    void confirmarReservaPorPlacaExitoso() {
        SagaVehiculo saga = SagaVehiculo.builder()
                .idSaga(UUID.randomUUID())
                .estadoSaga(EstadoSaga.EN_PROGRESO)
                .vehiculo(vehiculo)
                .build();
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(UUID.randomUUID())
                .vehiculo(vehiculo)
                .sagaVehiculo(saga)
                .estadoReserva(EstadoReserva.PENDIENTE)
                .idAsignacionExt(UUID.randomUUID())
                .solicitadoPor("Juan")
                .fechaInicio(LocalDateTime.now().plusDays(1))
                .fechaFin(LocalDateTime.now().plusDays(3))
                .claveIdempotencia("clave-2")
                .build();

        given(reservaRepository.findAllByVehiculoNumeroPlacaIgnoreCaseAndEstadoReserva("TWA101", EstadoReserva.PENDIENTE))
                .willReturn(List.of(reserva));
        given(reservaRepository.saveAll(anyList())).willReturn(List.of(reserva));
        given(sagaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        List<ReservaResponse> result = sagaService.confirmarReservaPorPlaca("TWA101");

        assertEquals(1, result.size());
        assertEquals("CONFIRMADA", result.get(0).estadoReserva());
    }

    @Test
    @DisplayName("confirmarReservaPorPlaca lanza BusinessException si no hay pendientes")
    void confirmarReservaPorPlacaSinPendientes() {
        given(reservaRepository.findAllByVehiculoNumeroPlacaIgnoreCaseAndEstadoReserva("XXX", EstadoReserva.PENDIENTE))
                .willReturn(Collections.emptyList());

        assertThrows(BusinessException.class, () -> sagaService.confirmarReservaPorPlaca("XXX"));
    }

    @Test
    @DisplayName("compensarSaga libera vehículo y marca saga como COMPENSADA")
    void compensarSagaExitoso() {
        UUID sagaId = UUID.randomUUID();
        SagaVehiculo saga = SagaVehiculo.builder()
                .idSaga(sagaId)
                .estadoSaga(EstadoSaga.EN_PROGRESO)
                .vehiculo(vehiculo)
                .build();
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(UUID.randomUUID())
                .estadoReserva(EstadoReserva.PENDIENTE)
                .build();

        configurarSecurityContext("operador-test");
        given(sagaRepository.findById(sagaId)).willReturn(Optional.of(saga));
        given(reservaRepository.findReservaPendienteByVehiculoId(vehiculoId)).willReturn(Optional.of(reserva));
        given(reservaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(vehicleRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(sagaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        boolean result = sagaService.compensarSaga(sagaId, "fallo en asignaciones");

        assertTrue(result);
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstadoReserva());
        assertEquals(EstadoVehiculo.DISPONIBLE, vehiculo.getEstadoVehiculo());
        verify(historialEstadoRepository).save(any());
    }

    @Test
    @DisplayName("compensarSaga retorna true si ya estaba compensada (idempotente)")
    void compensarSagaYaCompensada() {
        UUID sagaId = UUID.randomUUID();
        SagaVehiculo saga = SagaVehiculo.builder()
                .idSaga(sagaId)
                .estadoSaga(EstadoSaga.COMPENSADA)
                .build();
        given(sagaRepository.findById(sagaId)).willReturn(Optional.of(saga));

        assertTrue(sagaService.compensarSaga(sagaId, "reintento"));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("compensarSaga rechaza saga completada hace más de 15 días")
    void compensarSagaCompletadaAntigua() {
        UUID sagaId = UUID.randomUUID();
        SagaVehiculo saga = SagaVehiculo.builder()
                .idSaga(sagaId)
                .estadoSaga(EstadoSaga.COMPLETADA)
                .actualizadoEn(LocalDateTime.now().minusDays(20))
                .vehiculo(vehiculo)
                .build();
        given(sagaRepository.findById(sagaId)).willReturn(Optional.of(saga));

        assertThrows(BusinessException.class, () -> sagaService.compensarSaga(sagaId, "tarde"));
    }

    @Test
    @DisplayName("compensarPorReservaId delega a compensarSaga cuando hay saga asociada")
    void compensarPorReservaIdConSaga() {
        UUID reservaId = UUID.randomUUID();
        UUID sagaId = UUID.randomUUID();
        SagaVehiculo saga = SagaVehiculo.builder()
                .idSaga(sagaId)
                .estadoSaga(EstadoSaga.COMPENSADA)
                .build();
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(reservaId)
                .sagaVehiculo(saga)
                .build();
        given(reservaRepository.findById(reservaId)).willReturn(Optional.of(reserva));
        given(sagaRepository.findById(sagaId)).willReturn(Optional.of(saga));

        assertTrue(sagaService.compensarPorReservaId(reservaId, "rollback"));
    }

    @Test
    @DisplayName("compensarPorReservaId retorna false si la reserva no tiene saga")
    void compensarPorReservaIdSinSaga() {
        UUID reservaId = UUID.randomUUID();
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(reservaId)
                .sagaVehiculo(null)
                .build();
        given(reservaRepository.findById(reservaId)).willReturn(Optional.of(reserva));

        assertFalse(sagaService.compensarPorReservaId(reservaId, "sin saga"));
    }

    @Test
    @DisplayName("actualizarFechasReserva actualiza fechas cuando no hay colisiones")
    void actualizarFechasReservaExitoso() {
        UUID reservaId = UUID.randomUUID();
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(reservaId)
                .vehiculo(vehiculo)
                .estadoReserva(EstadoReserva.PENDIENTE)
                .fechaInicio(LocalDateTime.now().plusDays(2))
                .fechaFin(LocalDateTime.now().plusDays(4))
                .idAsignacionExt(UUID.randomUUID())
                .solicitadoPor("Ana")
                .claveIdempotencia("clave-3")
                .build();

        UpdateReservaDatesRequest request = new UpdateReservaDatesRequest(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(6));

        given(reservaRepository.findById(reservaId)).willReturn(Optional.of(reserva));
        given(reservaRepository.findOverlappingReservations(any(), eq(reservaId), any(), any(), anyList()))
                .willReturn(Collections.emptyList());
        given(reservaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ReservaResponse result = sagaService.actualizarFechasReserva(reservaId, request);

        assertEquals(request.fechaInicio(), result.fechaInicio());
        assertEquals(request.fechaFin(), result.fechaFin());
    }

    @Test
    @DisplayName("actualizarFechasReserva lanza ReservaConflictException si hay solapamiento")
    void actualizarFechasReservaConColision() {
        UUID reservaId = UUID.randomUUID();
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(reservaId)
                .vehiculo(vehiculo)
                .estadoReserva(EstadoReserva.PENDIENTE)
                .fechaInicio(LocalDateTime.now().plusDays(2))
                .fechaFin(LocalDateTime.now().plusDays(4))
                .build();

        UpdateReservaDatesRequest request = new UpdateReservaDatesRequest(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(6));

        ReservaVehiculo colision = ReservaVehiculo.builder()
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .estadoReserva(EstadoReserva.CONFIRMADA)
                .build();

        given(reservaRepository.findById(reservaId)).willReturn(Optional.of(reserva));
        given(reservaRepository.findOverlappingReservations(any(), eq(reservaId), any(), any(), anyList()))
                .willReturn(List.of(colision));

        assertThrows(ReservaConflictException.class,
                () -> sagaService.actualizarFechasReserva(reservaId, request));
    }

    @Test
    @DisplayName("actualizarFechasReserva rechaza fechas inválidas")
    void actualizarFechasReservaFechasInvalidas() {
        UUID reservaId = UUID.randomUUID();
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(reservaId)
                .vehiculo(vehiculo)
                .estadoReserva(EstadoReserva.PENDIENTE)
                .build();
        given(reservaRepository.findById(reservaId)).willReturn(Optional.of(reserva));

        UpdateReservaDatesRequest request = new UpdateReservaDatesRequest(
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(3));

        assertThrows(BusinessException.class, () -> sagaService.actualizarFechasReserva(reservaId, request));
    }

    @Test
    @DisplayName("findReservaById retorna DTO cuando existe")
    void findReservaByIdExitoso() {
        UUID reservaId = UUID.randomUUID();
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(reservaId)
                .vehiculo(vehiculo)
                .estadoReserva(EstadoReserva.CONFIRMADA)
                .idAsignacionExt(UUID.randomUUID())
                .solicitadoPor("Luis")
                .fechaInicio(LocalDateTime.now().plusDays(1))
                .fechaFin(LocalDateTime.now().plusDays(2))
                .claveIdempotencia("clave-4")
                .build();
        given(reservaRepository.findById(reservaId)).willReturn(Optional.of(reserva));

        Optional<ReservaResponse> result = sagaService.findReservaById(reservaId);

        assertTrue(result.isPresent());
        assertEquals(reservaId, result.get().idReserva());
    }

    @Test
    @DisplayName("findAllReservas retorna página mapeada")
    void findAllReservasPaginado() {
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(UUID.randomUUID())
                .vehiculo(vehiculo)
                .estadoReserva(EstadoReserva.PENDIENTE)
                .idAsignacionExt(UUID.randomUUID())
                .solicitadoPor("Pedro")
                .fechaInicio(LocalDateTime.now().plusDays(1))
                .fechaFin(LocalDateTime.now().plusDays(2))
                .claveIdempotencia("clave-5")
                .build();
        Page<ReservaVehiculo> page = new PageImpl<>(List.of(reserva));
        given(reservaRepository.findAllByOrderByCreadoEnDesc(any())).willReturn(page);

        Page<ReservaResponse> result = sagaService.findAllReservas(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Consultas paginadas de reservas por estado")
    void findReservasPorEstado() {
        ReservaVehiculo reserva = reservaBase();
        Page<ReservaVehiculo> page = new PageImpl<>(List.of(reserva));
        given(reservaRepository.findAllByEstadoReservaOrderByCreadoEnDesc(eq(EstadoReserva.PENDIENTE), any()))
                .willReturn(page);
        given(reservaRepository.findAllByEstadoReservaOrderByCreadoEnDesc(eq(EstadoReserva.CONFIRMADA), any()))
                .willReturn(page);
        given(reservaRepository.findAllByEstadoReservaOrderByCreadoEnDesc(eq(EstadoReserva.FALLIDA), any()))
                .willReturn(new PageImpl<>(List.of()));
        given(reservaRepository.findAllByEstadoReservaOrderByCreadoEnDesc(eq(EstadoReserva.CANCELADA), any()))
                .willReturn(new PageImpl<>(List.of()));

        assertEquals(1, sagaService.findReservasPendientes(PageRequest.of(0, 5)).getTotalElements());
        assertEquals(1, sagaService.findReservasConfirmadas(PageRequest.of(0, 5)).getTotalElements());
        assertEquals(0, sagaService.findReservasFallidas(PageRequest.of(0, 5)).getTotalElements());
        assertEquals(0, sagaService.findReservasCanceladas(PageRequest.of(0, 5)).getTotalElements());
    }

    @Test
    @DisplayName("Consultas de reservas y sagas por placa")
    void findPorPlaca() {
        ReservaVehiculo reserva = reservaBase();
        SagaVehiculo saga = SagaVehiculo.builder()
                .idSaga(UUID.randomUUID())
                .vehiculo(vehiculo)
                .tipoOperacion("RESERVA_VEHICULO")
                .estadoSaga(EstadoSaga.EN_PROGRESO)
                .claveIdempotencia("clave-s")
                .creadoEn(LocalDateTime.now())
                .build();

        given(reservaRepository.findByVehiculo_NumeroPlacaIgnoreCaseOrderByCreadoEnDesc(eq("TWA101"), any()))
                .willReturn(new PageImpl<>(List.of(reserva)));
        given(reservaRepository.findByVehiculo_NumeroPlacaIgnoreCaseAndEstadoReservaOrderByCreadoEnDesc(
                eq("TWA101"), eq(EstadoReserva.PENDIENTE), any()))
                .willReturn(new PageImpl<>(List.of(reserva)));
        given(sagaRepository.findByVehiculo_NumeroPlacaIgnoreCaseOrderByCreadoEnDesc(eq("TWA101"), any()))
                .willReturn(new PageImpl<>(List.of(saga)));
        given(sagaRepository.findByVehiculo_NumeroPlacaIgnoreCaseAndEstadoSagaOrderByCreadoEnDesc(
                eq("TWA101"), eq(EstadoSaga.EN_PROGRESO), any()))
                .willReturn(new PageImpl<>(List.of(saga)));

        assertEquals(1, sagaService.findReservasByPlaca("TWA101", PageRequest.of(0, 5)).getTotalElements());
        assertEquals(1, sagaService.findReservasByPlacaAndEstado(
                "TWA101", EstadoReserva.PENDIENTE, PageRequest.of(0, 5)).getTotalElements());
        assertEquals(1, sagaService.findSagasByPlaca("TWA101", PageRequest.of(0, 5)).getTotalElements());
        assertEquals(1, sagaService.findSagasByPlacaAndEstado(
                "TWA101", EstadoSaga.EN_PROGRESO, PageRequest.of(0, 5)).getTotalElements());
    }

    @Test
    @DisplayName("Consultas paginadas de sagas por estado")
    void findSagasPorEstado() {
        SagaVehiculo saga = SagaVehiculo.builder()
                .idSaga(UUID.randomUUID())
                .vehiculo(vehiculo)
                .tipoOperacion("RESERVA_VEHICULO")
                .estadoSaga(EstadoSaga.COMPLETADA)
                .claveIdempotencia("clave-s2")
                .creadoEn(LocalDateTime.now())
                .build();
        Page<SagaVehiculo> page = new PageImpl<>(List.of(saga));

        given(sagaRepository.findAllByOrderByCreadoEnDesc(any())).willReturn(page);
        given(sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(eq(EstadoSaga.INICIADA), any()))
                .willReturn(new PageImpl<>(List.of()));
        given(sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(eq(EstadoSaga.EN_PROGRESO), any()))
                .willReturn(page);
        given(sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(eq(EstadoSaga.COMPLETADA), any()))
                .willReturn(page);
        given(sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(eq(EstadoSaga.FALLIDA), any()))
                .willReturn(new PageImpl<>(List.of()));
        given(sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(eq(EstadoSaga.COMPENSADA), any()))
                .willReturn(new PageImpl<>(List.of()));

        assertEquals(1, sagaService.findAllSagas(PageRequest.of(0, 5)).getTotalElements());
        assertEquals(0, sagaService.findSagasIniciadas(PageRequest.of(0, 5)).getTotalElements());
        assertEquals(1, sagaService.findSagasEnProgreso(PageRequest.of(0, 5)).getTotalElements());
        assertEquals(1, sagaService.findSagasCompletadas(PageRequest.of(0, 5)).getTotalElements());
        assertEquals(0, sagaService.findSagasFallidas(PageRequest.of(0, 5)).getTotalElements());
        assertEquals(0, sagaService.findSagasCompensadas(PageRequest.of(0, 5)).getTotalElements());
    }

    @Test
    @DisplayName("actualizarFechasReserva rechaza estado no modificable")
    void actualizarFechasReservaEstadoInvalido() {
        UUID reservaId = UUID.randomUUID();
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(reservaId)
                .vehiculo(vehiculo)
                .estadoReserva(EstadoReserva.CANCELADA)
                .build();
        given(reservaRepository.findById(reservaId)).willReturn(Optional.of(reserva));

        UpdateReservaDatesRequest request = new UpdateReservaDatesRequest(
                LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(6));

        assertThrows(BusinessException.class, () -> sagaService.actualizarFechasReserva(reservaId, request));
    }

    @Test
    @DisplayName("iniciarReserva lanza BusinessException por solapamiento en procesarCreacionReserva")
    void iniciarReservaSolapamientoEnProcesar() {
        ReservaRequest request = reservaRequestValida();
        doNothing().when(idempotencyValidator).validateNotDuplicate(request.claveIdempotencia());
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(reservaRepository.obtenerReservasConflictivas(any(), anyList(), any(), any()))
                .willReturn(Collections.emptyList());
        given(reservaRepository.existeReservaEnRango(any(), anyList(), any(), any())).willReturn(true);

        assertThrows(BusinessException.class, () -> sagaService.iniciarReserva(vehiculoId, request));
    }

    private ReservaVehiculo reservaBase() {
        return ReservaVehiculo.builder()
                .idReserva(UUID.randomUUID())
                .vehiculo(vehiculo)
                .estadoReserva(EstadoReserva.PENDIENTE)
                .idAsignacionExt(UUID.randomUUID())
                .solicitadoPor("Pedro")
                .fechaInicio(LocalDateTime.now().plusDays(1))
                .fechaFin(LocalDateTime.now().plusDays(2))
                .claveIdempotencia("clave-base")
                .build();
    }

    private void configurarSecurityContext(String username) {
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }
}
