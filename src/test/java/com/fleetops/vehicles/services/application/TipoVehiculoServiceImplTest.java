package com.fleetops.vehicles.services.application;

import com.fleetops.vehicles.dto.request.TipoVehiculoRequest;
import com.fleetops.vehicles.dto.response.TipoVehiculoResponse;
import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.DuplicateResourceException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.mapper.DtoMapperTipoVehiculo;
import com.fleetops.vehicles.models.entities.TipoVehiculo;
import com.fleetops.vehicles.repositories.TipoVehiculoRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import com.fleetops.vehicles.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoVehiculoServiceImplTest {

    @Mock private TipoVehiculoRepository tipoVehiculoRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private DtoMapperTipoVehiculo dtoMapperTipoVehiculo;
    @InjectMocks private TipoVehiculoServiceImpl service;

    private TipoVehiculo tipo;
    private TipoVehiculoResponse response;

    @BeforeEach
    void setUp() {
        tipo = TestDataFactory.tipoVehiculo();
        response = new TipoVehiculoResponse(1L, "Furgon", "Carga liviana", 1500.0, tipo.getCreadoEn(), null);
    }

    @Test
    void findAllYFindById() {
        when(tipoVehiculoRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(tipo)));
        when(dtoMapperTipoVehiculo.toDto(tipo)).thenReturn(response);

        Page<TipoVehiculoResponse> page = service.findAll(PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());

        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipo));
        assertEquals("Furgon", service.findById(1L).nombreTipo());

        when(tipoVehiculoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void createExitosoYDuplicado() {
        TipoVehiculoRequest request = TestDataFactory.tipoRequest();
        when(tipoVehiculoRepository.existsByNombreTipoIgnoreCase("Furgon")).thenReturn(false);
        when(tipoVehiculoRepository.save(any())).thenAnswer(inv -> {
            TipoVehiculo t = inv.getArgument(0);
            t.setIdTipoVehiculo(1L);
            return t;
        });
        when(dtoMapperTipoVehiculo.toDto(any())).thenReturn(response);

        assertEquals(1L, service.create(request).idTipoVehiculo());

        when(tipoVehiculoRepository.existsByNombreTipoIgnoreCase("Furgon")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.create(request));
    }

    @Test
    void updateExitosoYNombreDuplicado() {
        TipoVehiculoRequest request = new TipoVehiculoRequest("Camioneta", "desc", 2000.0);
        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipo));
        when(tipoVehiculoRepository.existsByNombreTipoIgnoreCase("Camioneta")).thenReturn(false);
        when(tipoVehiculoRepository.save(any())).thenReturn(tipo);
        when(dtoMapperTipoVehiculo.toDto(any())).thenReturn(response);

        assertNotNull(service.update(1L, request));

        TipoVehiculo otro = TestDataFactory.tipoVehiculo();
        otro.setNombreTipo("Furgon");
        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(otro));
        when(tipoVehiculoRepository.existsByNombreTipoIgnoreCase("Camioneta")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.update(1L, request));
    }

    @Test
    void deleteBloqueadoSiHayVehiculosActivos() {
        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipo));
        when(vehicleRepository.countByTipoVehiculoAndActivoTrue(tipo)).thenReturn(2L);
        assertThrows(BusinessException.class, () -> service.delete(1L));
        verify(tipoVehiculoRepository, never()).delete(any());
    }

    @Test
    void deleteExitoso() {
        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipo));
        when(vehicleRepository.countByTipoVehiculoAndActivoTrue(tipo)).thenReturn(0L);
        service.delete(1L);
        verify(tipoVehiculoRepository).delete(tipo);
    }
}
