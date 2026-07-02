package com.fleetops.vehicles.service;

import com.fleetops.vehicles.dto.request.VehicleRequest;
import com.fleetops.vehicles.dto.request.VehicleUpdateRequest;
import com.fleetops.vehicles.dto.response.VehicleResponse;
import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.DuplicateResourceException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.mapper.DtoMapperHistorial;
import com.fleetops.vehicles.mapper.DtoMapperVehicle;
import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.HistorialEstadoVehiculo;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - VehicleService CRUD y consultas")
class VehicleServiceCrudTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private TipoVehiculoRepository tipoVehiculoRepository;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private HistorialEstadoRepository historialEstadoRepository;

    private VehicleServiceImpl vehicleService;
    private TipoVehiculo tipoVehiculo;
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
        tipoVehiculo = TipoVehiculo.builder()
                .idTipoVehiculo(1L)
                .nombreTipo("Camion Carga Pesada")
                .descripcion("Carga")
                .capacidadCarga(20000.0)
                .build();

        vehiculo = Vehiculo.builder()
                .idVehiculo(vehiculoId)
                .numeroPlaca("NEW123")
                .marca("Volvo")
                .modelo("FH16")
                .anioFabricacion(2022)
                .color("Blanco")
                .numeroChasis("CHS123456")
                .numeroMotor("MOT123456")
                .kilometraje(50000)
                .ciudadOperacion("Bogotá")
                .sedeOperacion("Terminal")
                .estadoVehiculo(EstadoVehiculo.DISPONIBLE)
                .activo(true)
                .fechaSoat(LocalDate.now().plusMonths(6))
                .fechaRtm(LocalDate.now().plusMonths(6))
                .fechaUltimoMant(LocalDate.now().minusMonths(1))
                .creadoEn(LocalDateTime.now())
                .tipoVehiculo(tipoVehiculo)
                .build();
    }

    private VehicleRequest vehicleRequest() {
        return new VehicleRequest(
                1L, "ABC123", "Volvo", "FH16", 2022, "Blanco",
                "CHS999999", "MOT999999", 10000,
                "Bogotá", "Terminal Norte", "DISPONIBLE",
                LocalDate.now().plusMonths(6),
                LocalDate.now().plusMonths(6),
                LocalDate.now().minusMonths(1));
    }

    private VehicleUpdateRequest vehicleUpdateRequest() {
        return new VehicleUpdateRequest(
                1L, "ABC123", "Volvo", "FH16", 2022, "Rojo",
                "CHS999999", "MOT999999", 15000,
                "Medellín", "Terminal Sur", "DISPONIBLE",
                LocalDate.now().plusMonths(6),
                LocalDate.now().plusMonths(6),
                LocalDate.now().minusMonths(1));
    }

    @Test
    @DisplayName("create registra vehículo nuevo exitosamente")
    void createExitoso() {
        VehicleRequest request = vehicleRequest();
        given(vehicleRepository.existsByNumeroPlacaIgnoreCase(any())).willReturn(false);
        given(vehicleRepository.existsByNumeroChasisIgnoreCase(any())).willReturn(false);
        given(vehicleRepository.existsByNumeroMotorIgnoreCase(any())).willReturn(false);
        given(tipoVehiculoRepository.findById(1L)).willReturn(Optional.of(tipoVehiculo));
        given(vehicleRepository.save(any())).willAnswer(inv -> {
            Vehiculo v = inv.getArgument(0);
            v.setIdVehiculo(vehiculoId);
            return v;
        });

        VehicleResponse result = vehicleService.create(request);

        assertEquals("ABC123", result.numeroPlaca());
        verify(historialEstadoRepository).save(any());
    }

    @Test
    @DisplayName("create lanza DuplicateResourceException si la placa existe")
    void createPlacaDuplicada() {
        given(vehicleRepository.existsByNumeroPlacaIgnoreCase("ABC123")).willReturn(true);

        assertThrows(DuplicateResourceException.class, () -> vehicleService.create(vehicleRequest()));
    }

    @Test
    @DisplayName("create lanza DuplicateResourceException si el chasis existe")
    void createChasisDuplicado() {
        given(vehicleRepository.existsByNumeroPlacaIgnoreCase(any())).willReturn(false);
        given(vehicleRepository.existsByNumeroChasisIgnoreCase(any())).willReturn(true);

        assertThrows(DuplicateResourceException.class, () -> vehicleService.create(vehicleRequest()));
    }

    @Test
    @DisplayName("create lanza DuplicateResourceException si el motor existe")
    void createMotorDuplicado() {
        given(vehicleRepository.existsByNumeroPlacaIgnoreCase(any())).willReturn(false);
        given(vehicleRepository.existsByNumeroChasisIgnoreCase(any())).willReturn(false);
        given(vehicleRepository.existsByNumeroMotorIgnoreCase(any())).willReturn(true);

        assertThrows(DuplicateResourceException.class, () -> vehicleService.create(vehicleRequest()));
    }

    @Test
    @DisplayName("create lanza BusinessException con estado inválido")
    void createEstadoInvalido() {
        VehicleRequest request = new VehicleRequest(
                1L, "ABC123", "Volvo", "FH16", 2022, "Blanco",
                "CHS999999", "MOT999999", 10000,
                "Bogotá", "Terminal", "ESTADO_FALSO",
                LocalDate.now().plusMonths(6),
                LocalDate.now().plusMonths(6),
                LocalDate.now().minusMonths(1));
        given(vehicleRepository.existsByNumeroPlacaIgnoreCase(any())).willReturn(false);
        given(vehicleRepository.existsByNumeroChasisIgnoreCase(any())).willReturn(false);
        given(vehicleRepository.existsByNumeroMotorIgnoreCase(any())).willReturn(false);
        given(tipoVehiculoRepository.findById(1L)).willReturn(Optional.of(tipoVehiculo));

        assertThrows(BusinessException.class, () -> vehicleService.create(request));
    }

    @Test
    @DisplayName("update actualiza vehículo existente")
    void updateExitoso() {
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(vehicleRepository.existsByNumeroPlacaIgnoreCaseAndIdVehiculoNot(any(), eq(vehiculoId)))
                .willReturn(false);
        given(vehicleRepository.existsByNumeroChasisIgnoreCaseAndIdVehiculoNot(any(), eq(vehiculoId)))
                .willReturn(false);
        given(vehicleRepository.existsByNumeroMotorIgnoreCaseAndIdVehiculoNot(any(), eq(vehiculoId)))
                .willReturn(false);
        given(vehicleRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        VehicleResponse result = vehicleService.update(vehiculoId, vehicleUpdateRequest());

        assertEquals("Rojo", result.color());
        assertEquals("Medellín", result.ciudadOperacion());
    }

    @Test
    @DisplayName("update lanza DuplicateResourceException si la placa pertenece a otro vehículo")
    void updatePlacaDuplicada() {
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(vehicleRepository.existsByNumeroPlacaIgnoreCaseAndIdVehiculoNot(any(), eq(vehiculoId)))
                .willReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> vehicleService.update(vehiculoId, vehicleUpdateRequest()));
    }

    @Test
    @DisplayName("update lanza DuplicateResourceException si el chasis pertenece a otro vehículo")
    void updateChasisDuplicado() {
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(vehicleRepository.existsByNumeroPlacaIgnoreCaseAndIdVehiculoNot(any(), eq(vehiculoId)))
                .willReturn(false);
        given(vehicleRepository.existsByNumeroChasisIgnoreCaseAndIdVehiculoNot(any(), eq(vehiculoId)))
                .willReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> vehicleService.update(vehiculoId, vehicleUpdateRequest()));
    }

    @Test
    @DisplayName("update lanza DuplicateResourceException si el motor pertenece a otro vehículo")
    void updateMotorDuplicado() {
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(vehicleRepository.existsByNumeroPlacaIgnoreCaseAndIdVehiculoNot(any(), eq(vehiculoId)))
                .willReturn(false);
        given(vehicleRepository.existsByNumeroChasisIgnoreCaseAndIdVehiculoNot(any(), eq(vehiculoId)))
                .willReturn(false);
        given(vehicleRepository.existsByNumeroMotorIgnoreCaseAndIdVehiculoNot(any(), eq(vehiculoId)))
                .willReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> vehicleService.update(vehiculoId, vehicleUpdateRequest()));
    }

    @Test
    @DisplayName("softDelete desactiva vehículo sin reservas pendientes")
    void softDeleteExitoso() {
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(reservaRepository.findReservaPendienteByVehiculoId(vehiculoId)).willReturn(Optional.empty());
        given(vehicleRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        assertTrue(vehicleService.softDelete(vehiculoId));
        assertFalse(vehiculo.getActivo());
        assertEquals(EstadoVehiculo.FUERA_DE_SERVICIO, vehiculo.getEstadoVehiculo());
    }

    @Test
    @DisplayName("softDelete lanza BusinessException si hay reserva pendiente")
    void softDeleteConReservaPendiente() {
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(reservaRepository.findReservaPendienteByVehiculoId(vehiculoId))
                .willReturn(Optional.of(com.fleetops.vehicles.models.entities.ReservaVehiculo.builder().build()));

        assertThrows(BusinessException.class, () -> vehicleService.softDelete(vehiculoId));
    }

    @Test
    @DisplayName("deleteByPlaca desactiva vehículo DISPONIBLE")
    void deleteByPlacaExitoso() {
        given(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("abc123"))
                .willReturn(Optional.of(vehiculo));
        given(vehicleRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        vehicleService.deleteByPlaca("abc123");

        assertFalse(vehiculo.getActivo());
    }

    @Test
    @DisplayName("deleteByPlaca rechaza vehículo no DISPONIBLE")
    void deleteByPlacaNoDisponible() {
        vehiculo.setEstadoVehiculo(EstadoVehiculo.RESERVADO);
        given(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("abc123"))
                .willReturn(Optional.of(vehiculo));

        assertThrows(BusinessException.class, () -> vehicleService.deleteByPlaca("abc123"));
    }

    @Test
    @DisplayName("reactivarVehiculo reactiva vehículo inactivo")
    void reactivarExitoso() {
        vehiculo.setActivo(false);
        vehiculo.setEstadoVehiculo(EstadoVehiculo.FUERA_DE_SERVICIO);
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(vehicleRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        VehicleResponse result = vehicleService.reactivarVehiculo(vehiculoId, "Reingreso a flota");

        assertTrue(result.activo());
        assertEquals("FUERA_DE_SERVICIO", result.estadoVehiculo());
    }

    @Test
    @DisplayName("findDisponibles retorna página de vehículos disponibles")
    void findDisponibles() {
        Page<Vehiculo> page = new PageImpl<>(List.of(vehiculo));
        given(vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(eq(EstadoVehiculo.DISPONIBLE), any()))
                .willReturn(page);

        Page<VehicleResponse> result = vehicleService.findDisponibles(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("DISPONIBLE", result.getContent().get(0).estadoVehiculo());
    }

    @Test
    @DisplayName("isAvailable retorna true para vehículo disponible y activo")
    void isAvailableTrue() {
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));

        assertTrue(vehicleService.isAvailable(vehiculoId));
    }

    @Test
    @DisplayName("getHistorialByVehiculoId retorna página de historial")
    void getHistorialPorId() {
        HistorialEstadoVehiculo historial = HistorialEstadoVehiculo.builder()
                .vehiculo(vehiculo)
                .estadoAnterior("DISPONIBLE")
                .estadoNuevo("EN_MANTENIMIENTO")
                .motivoCambio("Mantenimiento")
                .servicioOrigen("test")
                .registradoEn(LocalDateTime.now())
                .build();

        given(vehicleRepository.existsById(vehiculoId)).willReturn(true);
        given(historialEstadoRepository.findByVehiculo_IdVehiculoOrderByRegistradoEnDesc(eq(vehiculoId), any()))
                .willReturn(new PageImpl<>(List.of(historial)));

        var result = vehicleService.getHistorialByVehiculoId(vehiculoId, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("EN_MANTENIMIENTO", result.getContent().get(0).estadoNuevo());
    }

    @Test
    @DisplayName("findAll retorna todos los vehículos activos paginados")
    void findAllActivos() {
        given(vehicleRepository.findAllByActivoTrue(any()))
                .willReturn(new PageImpl<>(List.of(vehiculo)));

        Page<VehicleResponse> result = vehicleService.findAll(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("findReservados retorna vehículos en estado RESERVADO")
    void findReservados() {
        vehiculo.setEstadoVehiculo(EstadoVehiculo.RESERVADO);
        given(vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(eq(EstadoVehiculo.RESERVADO), any()))
                .willReturn(new PageImpl<>(List.of(vehiculo)));

        var result = vehicleService.findReservados(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("findMantenimiento y findFueraServicio retornan páginas")
    void findPorEstadoOperativo() {
        given(vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(eq(EstadoVehiculo.EN_MANTENIMIENTO), any()))
                .willReturn(new PageImpl<>(List.of(vehiculo)));
        given(vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(eq(EstadoVehiculo.FUERA_DE_SERVICIO), any()))
                .willReturn(new PageImpl<>(List.of(vehiculo)));

        assertEquals(1, vehicleService.findMantenimiento(PageRequest.of(0, 5)).getTotalElements());
        assertEquals(1, vehicleService.findFueraServicio(PageRequest.of(0, 5)).getTotalElements());
    }

    @Test
    @DisplayName("getDeletedVehicles retorna inactivos")
    void getDeletedVehicles() {
        vehiculo.setActivo(false);
        given(vehicleRepository.findAllByActivoFalse(any())).willReturn(new PageImpl<>(List.of(vehiculo)));

        var result = vehicleService.getDeletedVehicles(0, 10);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("updateByPlaca delega en update")
    void updateByPlaca() {
        given(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("twa101"))
                .willReturn(Optional.of(vehiculo));
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(vehicleRepository.existsByNumeroPlacaIgnoreCaseAndIdVehiculoNot(any(), eq(vehiculoId)))
                .willReturn(false);
        given(vehicleRepository.existsByNumeroChasisIgnoreCaseAndIdVehiculoNot(any(), eq(vehiculoId)))
                .willReturn(false);
        given(vehicleRepository.existsByNumeroMotorIgnoreCaseAndIdVehiculoNot(any(), eq(vehiculoId)))
                .willReturn(false);
        given(vehicleRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        VehicleResponse result = vehicleService.updateByPlaca("twa101", vehicleUpdateRequest());
        assertEquals("Rojo", result.color());
    }

    @Test
    @DisplayName("updateEstadoByPlaca delega en changeState")
    void updateEstadoByPlaca() {
        given(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("twa101"))
                .willReturn(Optional.of(vehiculo));
        given(vehicleRepository.findById(vehiculoId)).willReturn(Optional.of(vehiculo));
        given(vehicleRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        var request = new com.fleetops.vehicles.dto.request.EstadoCambioRequest(
                "EN_MANTENIMIENTO", "revision programada", "taller", null);
        VehicleResponse result = vehicleService.updateEstadoByPlaca("twa101", request);
        assertEquals("EN_MANTENIMIENTO", result.estadoVehiculo());
    }

    @Test
    @DisplayName("getDisponibilidadByPlaca retorna DTO")
    void getDisponibilidadByPlaca() {
        given(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("twa101"))
                .willReturn(Optional.of(vehiculo));

        var result = vehicleService.getDisponibilidadByPlaca("twa101");
        assertTrue(result.disponible());
    }

    @Test
    @DisplayName("getDisponibilidadByPlaca usa fecha de actualización cuando no es nula")
    void getDisponibilidadByPlacaUsaFechaActualizacion() {
        LocalDateTime actualizadoEn = LocalDateTime.now().minusHours(3);
        vehiculo.setActualizadoEn(actualizadoEn);
        given(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue("twa101"))
                .willReturn(Optional.of(vehiculo));

        var result = vehicleService.getDisponibilidadByPlaca("twa101");
        assertEquals(actualizadoEn, result.actualizadoEn());
    }

    @Test
    @DisplayName("findDisponiblesByNombreTipo filtra por tipo")
    void findDisponiblesByNombreTipo() {
        given(vehicleRepository.findByEstadoVehiculoAndActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase(
                eq(EstadoVehiculo.DISPONIBLE), eq("camion"), any()))
                .willReturn(new PageImpl<>(List.of(vehiculo)));

        var result = vehicleService.findDisponiblesByNombreTipo("camion", PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("reactivateByPlaca reactiva vehículo inactivo")
    void reactivateByPlaca() {
        vehiculo.setActivo(false);
        given(vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoFalse("twa101"))
                .willReturn(Optional.of(vehiculo));
        given(vehicleRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        VehicleResponse result = vehicleService.reactivateByPlaca("twa101", "reingreso");
        assertTrue(result.activo());
    }

    @Test
    @DisplayName("getHistorialByPlaca retorna página de historial filtrado por placa")
    void getHistorialPorPlaca() {
        HistorialEstadoVehiculo historial = HistorialEstadoVehiculo.builder()
                .vehiculo(vehiculo)
                .estadoAnterior("DISPONIBLE")
                .estadoNuevo("RESERVADO")
                .motivoCambio("Reserva de cliente")
                .servicioOrigen("test")
                .registradoEn(LocalDateTime.now())
                .build();

        given(historialEstadoRepository.findByVehiculo_NumeroPlacaIgnoreCaseOrderByRegistradoEnDesc(
                eq("NEW123"), any()))
                .willReturn(new PageImpl<>(List.of(historial)));

        var result = vehicleService.getHistorialByPlaca("NEW123", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("RESERVADO", result.getContent().get(0).estadoNuevo());
    }

    @Test
    @DisplayName("findAllHistorialGlobal retorna historial paginado")
    void findAllHistorialGlobal() {
        HistorialEstadoVehiculo historial = HistorialEstadoVehiculo.builder()
                .vehiculo(vehiculo)
                .estadoAnterior("DISPONIBLE")
                .estadoNuevo("RESERVADO")
                .motivoCambio("Reserva")
                .servicioOrigen("test")
                .registradoEn(LocalDateTime.now())
                .build();
        given(historialEstadoRepository.findAllByOrderByRegistradoEnDesc(any()))
                .willReturn(new PageImpl<>(List.of(historial)));

        assertEquals(1, vehicleService.findAllHistorialGlobal(PageRequest.of(0, 10)).getTotalElements());
    }
}
