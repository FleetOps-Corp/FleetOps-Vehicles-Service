package com.fleetops.vehicles.service;

import com.fleetops.vehicles.dto.response.DisponibilidadResponse;
import com.fleetops.vehicles.dto.response.VehicleResponse;
import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.mapper.DtoMapperHistorial;
import com.fleetops.vehicles.mapper.DtoMapperVehicle;
import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.TipoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.repositories.TipoVehiculoRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import com.fleetops.vehicles.services.application.VehicleServiceImpl;
import com.fleetops.vehicles.services.domain.AvailabilityPolicy;
import com.fleetops.vehicles.services.domain.StateTransitionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - VehicleService")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private TipoVehiculoRepository tipoVehiculoRepository;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private HistorialEstadoRepository historialEstadoRepository;

    private VehicleServiceImpl vehicleService;
    private Vehiculo vehiculo;
    private UUID vehiculoId;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(
                vehicleRepository,
                tipoVehiculoRepository,
                reservaRepository,
                historialEstadoRepository,
                new AvailabilityPolicy(),
                new StateTransitionValidator(),
                new DtoMapperVehicle(),
                new DtoMapperHistorial());

        vehiculoId = UUID.randomUUID();
        vehiculo = Vehiculo.builder()
                .idVehiculo(vehiculoId)
                .numeroPlaca("TWA101")
                .marca("Volvo")
                .modelo("FH16")
                .anioFabricacion(2022)
                .color("Blanco")
                .kilometraje(120000)
                .ciudadOperacion("Bogotá")
                .sedeOperacion("Terminal Norte")
                .estadoVehiculo(EstadoVehiculo.DISPONIBLE)
                .activo(true)
                .fechaSoat(LocalDate.now().plusMonths(6))
                .fechaRtm(LocalDate.now().plusMonths(6))
                .fechaUltimoMant(LocalDate.now().minusMonths(1))
                .creadoEn(LocalDateTime.now())
                .tipoVehiculo(TipoVehiculo.builder()
                        .idTipoVehiculo(1L)
                        .nombreTipo("Camion Carga Pesada")
                        .descripcion("Carga pesada")
                        .capacidadCarga(20000.0)
                        .build())
                .build();
    }

    @Test
    @DisplayName("Retornar vehículo por placa cuando existe y está activo")
    void findByPlacaExitoso() {
        given(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("twa101"))
                .willReturn(Optional.of(vehiculo));

        VehicleResponse result = vehicleService.findByPlaca("twa101");

        assertEquals("TWA101", result.numeroPlaca());
        assertEquals("DISPONIBLE", result.estadoVehiculo());
    }

    @Test
    @DisplayName("Lanzar ResourceNotFoundException al buscar placa inexistente")
    void findByPlacaInexistente() {
        given(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("XXX999"))
                .willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.findByPlaca("XXX999"));
    }

    @Test
    @DisplayName("Lanzar ResourceNotFoundException al buscar id inexistente")
    void findByIdInexistente() {
        UUID id = UUID.randomUUID();
        given(vehicleRepository.findByIdVehiculoAndActivoTrue(id)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.findById(id));
    }

    @Test
    @DisplayName("getDisponibilidad retorna true para vehículo DISPONIBLE y activo")
    void getDisponibilidadTrue() {
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));

        DisponibilidadResponse result = vehicleService.getDisponibilidad(vehiculoId);

        assertTrue(result.disponible());
        assertEquals("DISPONIBLE", result.estadoVehiculo());
    }

    @Test
    @DisplayName("getDisponibilidad retorna false para vehículo en mantenimiento")
    void getDisponibilidadFalseEnMantenimiento() {
        vehiculo.setEstadoVehiculo(EstadoVehiculo.EN_MANTENIMIENTO);
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));

        DisponibilidadResponse result = vehicleService.getDisponibilidad(vehiculoId);

        assertFalse(result.disponible());
        assertEquals("EN_MANTENIMIENTO", result.estadoVehiculo());
    }

    @Test
    @DisplayName("getDisponibilidad usa fecha de actualización cuando no es nula")
    void getDisponibilidadUsaFechaActualizacion() {
        LocalDateTime actualizadoEn = LocalDateTime.now().minusHours(2);
        vehiculo.setActualizadoEn(actualizadoEn);
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));

        DisponibilidadResponse result = vehicleService.getDisponibilidad(vehiculoId);

        assertEquals(actualizadoEn, result.actualizadoEn());
    }

    @Test
    @DisplayName("findById retorna vehículo cuando existe y está activo")
    void findByIdExitoso() {
        given(vehicleRepository.findByIdVehiculoAndActivoTrue(vehiculoId)).willReturn(Optional.of(vehiculo));

        VehicleResponse result = vehicleService.findById(vehiculoId);

        assertEquals(vehiculoId, result.idVehiculo());
        assertEquals("TWA101", result.numeroPlaca());
    }

    @Test
    @DisplayName("changeState transiciona DISPONIBLE a EN_MANTENIMIENTO")
    void changeStateExitoso() {
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(vehicleRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        VehicleResponse result = vehicleService.changeState(
                vehiculoId, "EN_MANTENIMIENTO", "Revisión programada", "test-service");

        assertEquals("EN_MANTENIMIENTO", result.estadoVehiculo());
        verify(historialEstadoRepository).save(any());
    }

    @Test
    @DisplayName("changeState lanza BusinessException con estado inválido")
    void changeStateEstadoInvalido() {
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));

        assertThrows(BusinessException.class,
                () -> vehicleService.changeState(vehiculoId, "ESTADO_FALSO", "motivo", "test"));
    }

    @Test
    @DisplayName("changeState lanza IllegalStateException con transición no permitida")
    void changeStateTransicionInvalida() {
        vehiculo.setEstadoVehiculo(EstadoVehiculo.EN_MANTENIMIENTO);
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));

        assertThrows(IllegalStateException.class,
                () -> vehicleService.changeState(vehiculoId, "RESERVADO", "motivo", "test"));
    }

    @Test
    @DisplayName("changeState EN_MANTENIMIENTO -> DISPONIBLE actualiza fecha de último mantenimiento")
    void changeStateActualizaFechaMantenimiento() {
        vehiculo.setEstadoVehiculo(EstadoVehiculo.EN_MANTENIMIENTO);
        LocalDate fechaAnterior = LocalDate.now().minusMonths(3);
        vehiculo.setFechaUltimoMant(fechaAnterior);
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(vehicleRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        vehicleService.changeState(vehiculoId, "DISPONIBLE", "Fin mantenimiento", "taller");

        assertTrue(vehiculo.getFechaUltimoMant().isAfter(fechaAnterior)
                || vehiculo.getFechaUltimoMant().isEqual(LocalDate.now()));
    }

    @Test
    @DisplayName("changeState lanza 404 si el vehículo no existe")
    void changeStateVehiculoNoExiste() {
        UUID id = UUID.randomUUID();
        given(vehicleRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vehicleService.changeState(id, "DISPONIBLE", "motivo", "test"));
    }
}
