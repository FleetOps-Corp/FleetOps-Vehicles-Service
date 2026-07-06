package com.fleetops.vehicles.services.application;

import com.fleetops.vehicles.models.entities.*;
import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import com.fleetops.vehicles.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleStateSchedulerTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private HistorialEstadoRepository historialEstadoRepository;

    @InjectMocks private VehicleStateScheduler scheduler;

    @Test
    void auditarDocumentosInmovilizaPorSoatYRtm() {
        Vehiculo v = TestDataFactory.vehiculoDisponible();
        v.setFechaSoat(LocalDate.now().minusDays(1));
        v.setFechaRtm(LocalDate.now().plusDays(3));

        when(vehicleRepository.findAllByActivoTrueAndEstadoVehiculoNot(EstadoVehiculo.FUERA_DE_SERVICIO))
                .thenReturn(List.of(v));
        when(vehicleRepository.save(any())).thenReturn(v);

        scheduler.auditarVencimientoDocumentosLegales();

        assertEquals(EstadoVehiculo.FUERA_DE_SERVICIO, v.getEstadoVehiculo());
        verify(historialEstadoRepository).save(any());
    }

    @Test
    void auditarDocumentosSoloRtm() {
        Vehiculo v = TestDataFactory.vehiculoDisponible();
        v.setFechaRtm(null);

        when(vehicleRepository.findAllByActivoTrueAndEstadoVehiculoNot(EstadoVehiculo.FUERA_DE_SERVICIO))
                .thenReturn(List.of(v));
        when(vehicleRepository.save(any())).thenReturn(v);

        scheduler.auditarVencimientoDocumentosLegales();
        assertEquals(EstadoVehiculo.FUERA_DE_SERVICIO, v.getEstadoVehiculo());
    }

    @Test
    void auditarDocumentosSoloSoatProximoYSinCambios() {
        Vehiculo proximoSoat = TestDataFactory.vehiculoDisponible();
        proximoSoat.setFechaSoat(LocalDate.now().plusDays(3));

        Vehiculo ok = TestDataFactory.vehiculoDisponible();

        when(vehicleRepository.findAllByActivoTrueAndEstadoVehiculoNot(EstadoVehiculo.FUERA_DE_SERVICIO))
                .thenReturn(List.of(proximoSoat, ok));
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scheduler.auditarVencimientoDocumentosLegales();

        assertEquals(EstadoVehiculo.FUERA_DE_SERVICIO, proximoSoat.getEstadoVehiculo());
        assertEquals(EstadoVehiculo.DISPONIBLE, ok.getEstadoVehiculo());
        verify(vehicleRepository, times(1)).save(proximoSoat);
    }
}
