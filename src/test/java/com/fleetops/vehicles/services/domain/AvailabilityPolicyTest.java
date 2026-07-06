package com.fleetops.vehicles.services.domain;

import com.fleetops.vehicles.models.entities.EstadoReserva;
import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityPolicyTest {

    @Mock
    private ReservaRepository reservaRepository;

    private AvailabilityPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new AvailabilityPolicy(reservaRepository);
    }

    @Test
    void isAvailableRequiereActivoYOperativo() {
        Vehiculo v = TestDataFactory.vehiculoDisponible();
        assertTrue(policy.isAvailable(v));

        v.setActivo(false);
        assertFalse(policy.isAvailable(v));

        v.setActivo(true);
        v.setEstadoVehiculo(EstadoVehiculo.EN_MANTENIMIENTO);
        assertFalse(policy.isAvailable(v));

        assertFalse(policy.isAvailable(null));
    }

    @Test
    void isAvailableForReservationRechazaDocumentosVencidos() {
        Vehiculo v = TestDataFactory.vehiculoDisponible();
        assertTrue(policy.isAvailableForReservation(v));

        v.setFechaSoat(LocalDate.now().minusDays(1));
        assertFalse(policy.isAvailableForReservation(v));

        v.setFechaSoat(LocalDate.now().plusMonths(1));
        v.setFechaRtm(LocalDate.now().minusDays(1));
        assertFalse(policy.isAvailableForReservation(v));
    }

    @Test
    void isAvailableForReservationFallaSiNoEstaOperativo() {
        Vehiculo v = TestDataFactory.vehiculoDisponible();
        v.setEstadoVehiculo(EstadoVehiculo.EN_MANTENIMIENTO);
        assertFalse(policy.isAvailableForReservation(v));
    }

    @Test
    void isAssignableRechazaConflictosYDocumentosProximos() {
        Vehiculo v = TestDataFactory.vehiculoDisponible();
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fin = inicio.plusDays(2);

        when(reservaRepository.obtenerReservasConflictivas(any(), anyList(), any(), any())).thenReturn(List.of());
        assertTrue(policy.isAssignable(v, inicio, fin));

        when(reservaRepository.obtenerReservasConflictivas(any(), eq(List.of(EstadoReserva.CONFIRMADA)), any(), any()))
                .thenReturn(List.of(TestDataFactory.reserva(v, EstadoReserva.CONFIRMADA)));
        assertFalse(policy.isAssignable(v, inicio, fin));

        Vehiculo soatProximo = TestDataFactory.vehiculoDisponible();
        soatProximo.setFechaSoat(LocalDate.now().plusDays(3));
        assertFalse(policy.isAssignable(soatProximo, inicio, fin));
    }
}
