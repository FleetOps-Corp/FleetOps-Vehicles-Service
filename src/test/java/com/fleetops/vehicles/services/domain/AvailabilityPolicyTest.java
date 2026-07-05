package com.fleetops.vehicles.services.domain;

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AvailabilityPolicyTest {

    private AvailabilityPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new AvailabilityPolicy();
    }

    @Test
    void isAvailableRequiereActivoYDisponible() {
        Vehiculo v = TestDataFactory.vehiculoDisponible();
        assertTrue(policy.isAvailable(v));

        v.setActivo(false);
        assertFalse(policy.isAvailable(v));

        v.setActivo(true);
        v.setEstadoVehiculo(EstadoVehiculo.RESERVADO);
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
    void isAvailableForReservationFallaSiNoEstaDisponible() {
        Vehiculo v = TestDataFactory.vehiculoDisponible();
        v.setEstadoVehiculo(EstadoVehiculo.EN_MANTENIMIENTO);
        assertFalse(policy.isAvailableForReservation(v));
    }
}
