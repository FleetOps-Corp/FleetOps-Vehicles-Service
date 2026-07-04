package com.fleetops.vehicles.services.application;

import com.fleetops.vehicles.models.entities.*;
import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import com.fleetops.vehicles.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleStateSchedulerTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private SagaService sagaService;
    @Mock private HistorialEstadoRepository historialEstadoRepository;

    @InjectMocks private VehicleStateScheduler scheduler;

    @Test
    void sincronizarEstadosMarcaReservadoYLibera() {
        Vehiculo disponible = TestDataFactory.vehiculoDisponible();
        ReservaVehiculo confirmada = TestDataFactory.reserva(disponible, EstadoReserva.CONFIRMADA);
        confirmada.setFechaInicio(LocalDateTime.now().minusHours(1));
        confirmada.setFechaFin(LocalDateTime.now().plusHours(1));

        Vehiculo reservado = TestDataFactory.vehiculoDisponible();
        reservado.setEstadoVehiculo(EstadoVehiculo.RESERVADO);

        ReservaVehiculo pendienteVencida = TestDataFactory.reserva(disponible, EstadoReserva.PENDIENTE);
        pendienteVencida.setFechaInicio(LocalDateTime.now().minusMinutes(5));
        pendienteVencida.setFechaFin(LocalDateTime.now().plusHours(1));

        when(reservaRepository.findCurrentlyActiveReservations(any(), anyList()))
                .thenReturn(List.of(confirmada, pendienteVencida));
        when(vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(EstadoVehiculo.RESERVADO))
                .thenReturn(List.of(reservado));
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scheduler.sincronizarEstadosPorAgenda();

        assertEquals(EstadoVehiculo.RESERVADO, disponible.getEstadoVehiculo());
        assertEquals(EstadoVehiculo.DISPONIBLE, reservado.getEstadoVehiculo());
        verify(sagaService).compensarPorReservaId(eq(pendienteVencida.getIdReserva()), anyString());
        verify(historialEstadoRepository, atLeastOnce()).save(any());
    }

    @Test
    void cancelarReservasExpiradas() {
        ReservaVehiculo expirada = TestDataFactory.reserva(TestDataFactory.vehiculoDisponible(), EstadoReserva.PENDIENTE);
        when(reservaRepository.findByEstadoReservaAndCreadoEnBefore(eq(EstadoReserva.PENDIENTE), any()))
                .thenReturn(List.of(expirada));

        scheduler.cancelarReservasExpiradas();
        verify(sagaService).compensarPorReservaId(eq(expirada.getIdReserva()), contains("Timeout"));
    }

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

    @Test
    void cancelarReservasExpiradasSinResultados() {
        when(reservaRepository.findByEstadoReservaAndCreadoEnBefore(eq(EstadoReserva.PENDIENTE), any()))
                .thenReturn(List.of());
        scheduler.cancelarReservasExpiradas();
        verify(sagaService, never()).compensarPorReservaId(any(), anyString());
    }
}
