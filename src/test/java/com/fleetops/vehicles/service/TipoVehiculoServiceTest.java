package com.fleetops.vehicles.service;

import com.fleetops.vehicles.dto.request.TipoVehiculoRequest;
import com.fleetops.vehicles.dto.response.TipoVehiculoResponse;
import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.DuplicateResourceException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.mapper.DtoMapperTipoVehiculo;
import com.fleetops.vehicles.models.entities.TipoVehiculo;
import com.fleetops.vehicles.repositories.TipoVehiculoRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import com.fleetops.vehicles.services.application.TipoVehiculoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - TipoVehiculoService")
class TipoVehiculoServiceTest {

    @Mock
    private TipoVehiculoRepository tipoVehiculoRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private TipoVehiculoServiceImpl tipoVehiculoService;

    private TipoVehiculo tipoExistente;
    private TipoVehiculoRequest requestValido;

    @BeforeEach
    void setUp() {
        tipoVehiculoService = new TipoVehiculoServiceImpl(
                tipoVehiculoRepository,
                vehicleRepository,
                new DtoMapperTipoVehiculo());

        tipoExistente = TipoVehiculo.builder()
                .idTipoVehiculo(1L)
                .nombreTipo("Furgon Refrigerado")
                .descripcion("Transporte refrigerado")
                .capacidadCarga(5000.0)
                .creadoEn(LocalDateTime.now())
                .build();

        requestValido = new TipoVehiculoRequest(
                "Camion Pluma",
                "Transporte de maquinaria",
                12000.0);
    }

    @Test
    @DisplayName("Crear tipo de vehículo exitosamente cuando el nombre es único")
    void crearTipoExitosamente() {
        given(tipoVehiculoRepository.existsByNombreTipoIgnoreCase(requestValido.nombreTipo())).willReturn(false);
        given(tipoVehiculoRepository.save(any(TipoVehiculo.class))).willAnswer(invocation -> {
            TipoVehiculo t = invocation.getArgument(0);
            t.setIdTipoVehiculo(99L);
            return t;
        });

        TipoVehiculoResponse result = tipoVehiculoService.create(requestValido);

        assertEquals("Camion Pluma", result.nombreTipo());
        assertEquals(99L, result.idTipoVehiculo());
        verify(tipoVehiculoRepository).save(any(TipoVehiculo.class));
    }

    @Test
    @DisplayName("Lanzar DuplicateResourceException al crear con nombre duplicado")
    void crearConNombreDuplicado() {
        given(tipoVehiculoRepository.existsByNombreTipoIgnoreCase(requestValido.nombreTipo())).willReturn(true);

        assertThrows(DuplicateResourceException.class, () -> tipoVehiculoService.create(requestValido));
        verify(tipoVehiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar tipo exitosamente cuando existe")
    void actualizarExitosamente() {
        TipoVehiculoRequest update = new TipoVehiculoRequest(
                "Furgon Premium",
                "Nueva descripcion",
                6000.0);

        given(tipoVehiculoRepository.findById(1L)).willReturn(Optional.of(tipoExistente));
        given(tipoVehiculoRepository.existsByNombreTipoIgnoreCase(update.nombreTipo())).willReturn(false);
        given(tipoVehiculoRepository.save(any(TipoVehiculo.class))).willAnswer(inv -> inv.getArgument(0));

        TipoVehiculoResponse result = tipoVehiculoService.update(1L, update);

        assertEquals("Furgon Premium", result.nombreTipo());
        verify(tipoVehiculoRepository).save(tipoExistente);
    }

    @Test
    @DisplayName("Lanzar ResourceNotFoundException al actualizar id inexistente")
    void actualizarIdInexistente() {
        given(tipoVehiculoRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tipoVehiculoService.update(99L, requestValido));
        verify(tipoVehiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanzar DuplicateResourceException al actualizar con nombre en uso por otro tipo")
    void actualizarNombreDuplicado() {
        TipoVehiculoRequest update = new TipoVehiculoRequest(
                "Bus Urbano",
                "Descripcion",
                4000.0);

        given(tipoVehiculoRepository.findById(1L)).willReturn(Optional.of(tipoExistente));
        given(tipoVehiculoRepository.existsByNombreTipoIgnoreCase("Bus Urbano")).willReturn(true);

        assertThrows(DuplicateResourceException.class, () -> tipoVehiculoService.update(1L, update));
    }

    @Test
    @DisplayName("Eliminar tipo exitosamente cuando no tiene vehículos activos")
    void eliminarExitosamente() {
        given(tipoVehiculoRepository.findById(1L)).willReturn(Optional.of(tipoExistente));
        given(vehicleRepository.countByTipoVehiculoAndActivoTrue(tipoExistente)).willReturn(0L);

        tipoVehiculoService.delete(1L);

        verify(tipoVehiculoRepository).delete(tipoExistente);
    }

    @Test
    @DisplayName("Lanzar ResourceNotFoundException al eliminar id inexistente")
    void eliminarIdInexistente() {
        given(tipoVehiculoRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tipoVehiculoService.delete(99L));
        verify(tipoVehiculoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Lanzar BusinessException al eliminar tipo con vehículos activos asociados")
    void eliminarConVehiculosActivos() {
        given(tipoVehiculoRepository.findById(1L)).willReturn(Optional.of(tipoExistente));
        given(vehicleRepository.countByTipoVehiculoAndActivoTrue(tipoExistente)).willReturn(3L);

        assertThrows(BusinessException.class, () -> tipoVehiculoService.delete(1L));
        verify(tipoVehiculoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Retornar tipo por id cuando existe")
    void findByIdExitoso() {
        given(tipoVehiculoRepository.findById(1L)).willReturn(Optional.of(tipoExistente));

        TipoVehiculoResponse result = tipoVehiculoService.findById(1L);

        assertEquals("Furgon Refrigerado", result.nombreTipo());
    }

    @Test
    @DisplayName("Lanzar ResourceNotFoundException al buscar id inexistente")
    void findByIdInexistente() {
        given(tipoVehiculoRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tipoVehiculoService.findById(99L));
    }

    @Test
    @DisplayName("Retornar página de tipos de vehículo")
    void findAllRetornaPagina() {
        Page<TipoVehiculo> page = new PageImpl<>(List.of(tipoExistente));
        given(tipoVehiculoRepository.findAll(any(Pageable.class))).willReturn(page);

        Page<TipoVehiculoResponse> result = tipoVehiculoService.findAll(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("Furgon Refrigerado", result.getContent().getFirst().nombreTipo());
    }
}
