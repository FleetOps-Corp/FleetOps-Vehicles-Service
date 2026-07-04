package com.fleetops.vehicles.services.application;

import com.fleetops.vehicles.dto.request.EstadoCambioRequest;
import com.fleetops.vehicles.dto.request.VehicleRequest;
import com.fleetops.vehicles.dto.request.VehicleUpdateRequest;
import com.fleetops.vehicles.dto.response.VehicleResponse;
import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.mapper.DtoMapperHistorial;
import com.fleetops.vehicles.mapper.DtoMapperVehicle;
import com.fleetops.vehicles.models.entities.*;
import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.repositories.TipoVehiculoRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import com.fleetops.vehicles.services.domain.AvailabilityPolicy;
import com.fleetops.vehicles.services.domain.StateTransitionValidator;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private TipoVehiculoRepository tipoVehiculoRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private HistorialEstadoRepository historialEstadoRepository;
    @Mock private AvailabilityPolicy availabilityPolicy;
    @Mock private StateTransitionValidator stateTransitionValidator;
    @Mock private DtoMapperVehicle dtoMapperVehicle;
    @Mock private DtoMapperHistorial dtoMapperHistorial;
    @Mock private SagaService sagaService;

    @InjectMocks private VehicleServiceImpl service;

    private Vehiculo vehiculo;
    private VehicleResponse vehicleResponse;
    private TipoVehiculo tipo;

    @BeforeEach
    void setUp() {
        tipo = TestDataFactory.tipoVehiculo();
        vehiculo = TestDataFactory.vehiculoDisponible();
        vehicleResponse = mock(VehicleResponse.class);
    }

    @Test
    void listadosPaginados() {
        when(vehicleRepository.findAllByActivoTrue(any())).thenReturn(new PageImpl<>(List.of(vehiculo)));
        when(vehicleRepository.findAllByActivoFalse(any())).thenReturn(new PageImpl<>(List.of(vehiculo)));
        when(vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(eq(EstadoVehiculo.DISPONIBLE), any()))
                .thenReturn(new PageImpl<>(List.of(vehiculo)));
        when(vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(eq(EstadoVehiculo.RESERVADO), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(eq(EstadoVehiculo.EN_MANTENIMIENTO), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(eq(EstadoVehiculo.FUERA_DE_SERVICIO), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(dtoMapperVehicle.toDto(vehiculo)).thenReturn(vehicleResponse);

        var pageable = PageRequest.of(0, 10);
        assertEquals(1, service.findAll(pageable).getTotalElements());
        assertEquals(1, service.getDeletedVehicles(0, 10).getTotalElements());
        assertEquals(1, service.findDisponibles(pageable).getTotalElements());
        assertEquals(0, service.findReservados(pageable).getTotalElements());
        assertEquals(0, service.findMantenimiento(pageable).getTotalElements());
        assertEquals(0, service.findFueraServicio(pageable).getTotalElements());
    }

    @Test
    void findByIdYPlaca() {
        when(vehicleRepository.findByIdVehiculoAndActivoTrue(vehiculo.getIdVehiculo()))
                .thenReturn(Optional.of(vehiculo));
        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("ABC123"))
                .thenReturn(Optional.of(vehiculo));
        when(dtoMapperVehicle.toDto(vehiculo)).thenReturn(vehicleResponse);

        assertNotNull(service.findById(vehiculo.getIdVehiculo()));
        assertNotNull(service.findByPlaca("ABC123"));

        when(vehicleRepository.findByIdVehiculoAndActivoTrue(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(UUID.randomUUID()));
    }

    @Test
    void createExitosoYDocumentosInvalidos() {
        VehicleRequest request = TestDataFactory.vehicleRequest();
        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipo));
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dtoMapperVehicle.toDto(any())).thenReturn(vehicleResponse);

        assertNotNull(service.create(request));
        verify(historialEstadoRepository).save(any());

        VehicleRequest soatCorto = new VehicleRequest(
                1L, "XYZ999", "M", "M", 2020, "Rojo", "C1", "M1", 0, "Cali", "Sede",
                "DISPONIBLE", LocalDate.now().plusDays(3), LocalDate.now().plusMonths(6),
                LocalDate.now().minusDays(1));
        assertThrows(IllegalArgumentException.class, () -> service.create(soatCorto));
    }

    @Test
    void updateExitosoYCambioDeTipo() {
        VehicleUpdateRequest request = TestDataFactory.vehicleUpdateRequest(2L);
        TipoVehiculo otro = TestDataFactory.tipoVehiculo();
        otro.setIdTipoVehiculo(2L);

        when(vehicleRepository.findDetailedById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(tipoVehiculoRepository.findById(2L)).thenReturn(Optional.of(otro));
        when(vehicleRepository.save(any())).thenReturn(vehiculo);
        when(dtoMapperVehicle.toDto(any())).thenReturn(vehicleResponse);

        assertNotNull(service.update(vehiculo.getIdVehiculo(), request));
        assertEquals(2L, vehiculo.getTipoVehiculo().getIdTipoVehiculo());
    }

    @Test
    void softDeleteBloqueadoConReservasActivas() {
        when(vehicleRepository.findById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(reservaRepository.existsByVehiculo_IdVehiculoAndEstadoReservaIn(any(), anyList())).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.softDelete(vehiculo.getIdVehiculo()));
    }

    @Test
    void softDeleteExitoso() {
        when(vehicleRepository.findById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(reservaRepository.existsByVehiculo_IdVehiculoAndEstadoReservaIn(any(), anyList())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehiculo);

        assertTrue(service.softDelete(vehiculo.getIdVehiculo()));
        assertFalse(vehiculo.getActivo());
        assertEquals(EstadoVehiculo.FUERA_DE_SERVICIO, vehiculo.getEstadoVehiculo());
    }

    @Test
    void deleteByPlacaDelegaEnSoftDelete() {
        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("ABC123"))
                .thenReturn(Optional.of(vehiculo));
        when(vehicleRepository.findById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(reservaRepository.existsByVehiculo_IdVehiculoAndEstadoReservaIn(any(), anyList())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehiculo);

        service.deleteByPlaca("ABC123");
        verify(vehicleRepository).save(vehiculo);
    }

    @Test
    void reactivarVehiculoYPorPlaca() {
        vehiculo.setActivo(false);
        when(vehicleRepository.findDetailedById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(vehicleRepository.save(any())).thenReturn(vehiculo);
        when(dtoMapperVehicle.toDto(any())).thenReturn(vehicleResponse);

        assertNotNull(service.reactivarVehiculo(vehiculo.getIdVehiculo(), "reactivacion"));
        assertTrue(vehiculo.getActivo());

        vehiculo.setActivo(true);
        assertThrows(BusinessException.class,
                () -> service.reactivarVehiculo(vehiculo.getIdVehiculo(), "ya activo"));

        vehiculo.setActivo(false);
        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoFalse("ABC123"))
                .thenReturn(Optional.of(vehiculo));
        assertNotNull(service.reactivateByPlaca("ABC123", "ok"));
    }

    @Test
    void changeStateExitosoYReglasDeNegocio() {
        when(vehicleRepository.findDetailedById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(vehicleRepository.save(any())).thenReturn(vehiculo);
        when(dtoMapperVehicle.toDto(any())).thenReturn(vehicleResponse);
        doNothing().when(stateTransitionValidator).validateTransition(any(), any());

        assertNotNull(service.changeState(vehiculo.getIdVehiculo(), "EN_MANTENIMIENTO", "taller", "ops"));
        assertEquals(EstadoVehiculo.EN_MANTENIMIENTO, vehiculo.getEstadoVehiculo());

        assertThrows(BusinessException.class,
                () -> service.changeState(vehiculo.getIdVehiculo(), "EN_MANTENIMIENTO", "dup", "ops"));

        vehiculo.setEstadoVehiculo(EstadoVehiculo.DISPONIBLE);
        assertThrows(BusinessException.class,
                () -> service.changeState(vehiculo.getIdVehiculo(), "RESERVADO", "manual", "ops"));

        assertThrows(BusinessException.class,
                () -> service.changeState(vehiculo.getIdVehiculo(), "NO_EXISTE", "x", "ops"));
    }

    @Test
    void changeStateConCascadaDeReservas() {
        vehiculo.setEstadoVehiculo(EstadoVehiculo.RESERVADO);
        ReservaVehiculo activa = TestDataFactory.reserva(vehiculo, EstadoReserva.CONFIRMADA);
        ReservaVehiculo futura = TestDataFactory.reserva(vehiculo, EstadoReserva.PENDIENTE);
        futura.setFechaInicio(java.time.LocalDateTime.now().plusDays(2));
        futura.setFechaFin(java.time.LocalDateTime.now().plusDays(3));

        when(vehicleRepository.findDetailedById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(reservaRepository.findByVehiculo_IdVehiculoAndEstadoReservaIn(any(), anyList()))
                .thenReturn(List.of(activa, futura));
        when(vehicleRepository.save(any())).thenReturn(vehiculo);
        when(dtoMapperVehicle.toDto(any())).thenReturn(vehicleResponse);
        doNothing().when(stateTransitionValidator).validateTransition(any(), any());

        service.changeState(vehiculo.getIdVehiculo(), "FUERA_DE_SERVICIO", "siniestro", "ops");
        verify(sagaService, times(2)).compensarPorReservaId(any(), anyString());
    }

    @Test
    void updateEstadoByPlaca() {
        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("ABC123"))
                .thenReturn(Optional.of(vehiculo));
        when(vehicleRepository.findDetailedById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(vehicleRepository.save(any())).thenReturn(vehiculo);
        when(dtoMapperVehicle.toDto(any())).thenReturn(vehicleResponse);
        doNothing().when(stateTransitionValidator).validateTransition(any(), any());

        EstadoCambioRequest request = new EstadoCambioRequest("EN_MANTENIMIENTO", "motivo largo", "ops", null);
        assertNotNull(service.updateEstadoByPlaca("ABC123", request));
    }

    @Test
    void disponibilidadEHistorial() {
        when(vehicleRepository.findById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(availabilityPolicy.isAvailable(vehiculo)).thenReturn(true);
        assertTrue(service.isAvailable(vehiculo.getIdVehiculo()));
        assertTrue(service.getDisponibilidad(vehiculo.getIdVehiculo()).disponible());

        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("ABC123"))
                .thenReturn(Optional.of(vehiculo));
        assertTrue(service.getDisponibilidadByPlaca("ABC123").disponible());

        when(vehicleRepository.existsById(vehiculo.getIdVehiculo())).thenReturn(true);
        when(historialEstadoRepository.findByVehiculo_IdVehiculoOrderByRegistradoEnDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(historialEstadoRepository.findAllByOrderByRegistradoEnDesc(any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(historialEstadoRepository.findByVehiculo_NumeroPlacaIgnoreCaseOrderByRegistradoEnDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        assertEquals(0, service.getHistorialByVehiculoId(vehiculo.getIdVehiculo(), PageRequest.of(0, 10)).getTotalElements());
        assertEquals(0, service.findAllHistorialGlobal(PageRequest.of(0, 10)).getTotalElements());
        assertEquals(0, service.getHistorialByPlaca("ABC123", PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void findDisponiblesByNombreTipo() {
        when(vehicleRepository.findByEstadoVehiculoAndActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase(
                eq(EstadoVehiculo.DISPONIBLE), eq("Fur"), any()))
                .thenReturn(new PageImpl<>(List.of(vehiculo)));
        when(dtoMapperVehicle.toDto(vehiculo)).thenReturn(vehicleResponse);
        assertEquals(1, service.findDisponiblesByNombreTipo("Fur", PageRequest.of(0, 5)).getTotalElements());
    }

    @Test
    void updateByPlaca() {
        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("ABC123"))
                .thenReturn(Optional.of(vehiculo));
        when(vehicleRepository.findDetailedById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(vehicleRepository.save(any())).thenReturn(vehiculo);
        when(dtoMapperVehicle.toDto(any())).thenReturn(vehicleResponse);

        assertNotNull(service.updateByPlaca("ABC123", TestDataFactory.vehicleUpdateRequest(1L)));
    }

    @Test
    void createRechazaRtmCortoYTipoInexistente() {
        VehicleRequest rtmCorto = new VehicleRequest(
                1L, "XYZ999", "M", "M", 2020, "Rojo", "C1", "M1", 0, "Cali", "Sede",
                "DISPONIBLE", LocalDate.now().plusMonths(6), LocalDate.now().plusDays(2),
                LocalDate.now().minusDays(1));
        assertThrows(IllegalArgumentException.class, () -> service.create(rtmCorto));

        when(tipoVehiculoRepository.findById(99L)).thenReturn(Optional.empty());
        VehicleRequest okDocs = TestDataFactory.vehicleRequest();
        VehicleRequest sinTipo = new VehicleRequest(
                99L, okDocs.numeroPlaca(), okDocs.marca(), okDocs.modelo(), okDocs.anioFabricacion(),
                okDocs.color(), okDocs.numeroChasis(), okDocs.numeroMotor(), okDocs.kilometraje(),
                okDocs.ciudadOperacion(), okDocs.sedeOperacion(), okDocs.estadoVehiculo(),
                okDocs.fechaSoat(), okDocs.fechaRtm(), okDocs.fechaUltimoMant());
        assertThrows(ResourceNotFoundException.class, () -> service.create(sinTipo));
    }

    @Test
    void changeStateDesdeMantenimientoActualizaFechaMant() {
        vehiculo.setEstadoVehiculo(EstadoVehiculo.EN_MANTENIMIENTO);
        when(vehicleRepository.findDetailedById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(vehicleRepository.save(any())).thenReturn(vehiculo);
        when(dtoMapperVehicle.toDto(any())).thenReturn(vehicleResponse);
        doNothing().when(stateTransitionValidator).validateTransition(any(), any());

        service.changeState(vehiculo.getIdVehiculo(), "DISPONIBLE", "salida taller", "ops");
        assertEquals(EstadoVehiculo.DISPONIBLE, vehiculo.getEstadoVehiculo());
        assertNotNull(vehiculo.getFechaUltimoMant());
    }

    @Test
    void historialYNotFoundPaths() {
        when(vehicleRepository.existsById(vehiculo.getIdVehiculo())).thenReturn(false);
        assertThrows(ResourceNotFoundException.class,
                () -> service.getHistorialByVehiculoId(vehiculo.getIdVehiculo(), PageRequest.of(0, 5)));

        when(vehicleRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.isAvailable(UUID.randomUUID()));
        assertThrows(ResourceNotFoundException.class, () -> service.getDisponibilidad(UUID.randomUUID()));

        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("ZZZ")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findByPlaca("ZZZ"));
        assertThrows(ResourceNotFoundException.class, () -> service.getDisponibilidadByPlaca("ZZZ"));
        assertThrows(ResourceNotFoundException.class, () -> service.deleteByPlaca("ZZZ"));
        assertThrows(ResourceNotFoundException.class,
                () -> service.updateEstadoByPlaca("ZZZ",
                        new EstadoCambioRequest("EN_MANTENIMIENTO", "motivo largo", "ops", null)));

        when(vehicleRepository.findDetailedById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.changeState(UUID.randomUUID(), "EN_MANTENIMIENTO", "x", "ops"));
        assertThrows(ResourceNotFoundException.class,
                () -> service.update(UUID.randomUUID(), TestDataFactory.vehicleUpdateRequest(1L)));
        assertThrows(ResourceNotFoundException.class,
                () -> service.reactivarVehiculo(UUID.randomUUID(), "x"));

        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoFalse("ZZZ")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.reactivateByPlaca("ZZZ", "x"));

        when(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("ABC123")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.updateByPlaca("ABC123", TestDataFactory.vehicleUpdateRequest(1L)));
    }

    @Test
    void updateConTipoInexistente() {
        VehicleUpdateRequest request = TestDataFactory.vehicleUpdateRequest(99L);
        when(vehicleRepository.findDetailedById(vehiculo.getIdVehiculo())).thenReturn(Optional.of(vehiculo));
        when(tipoVehiculoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.update(vehiculo.getIdVehiculo(), request));
    }
}
