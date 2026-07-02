package com.fleetops.vehicles.service;

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.TipoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.services.domain.AvailabilityPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests unitarios - AvailabilityPolicy")
class AvailabilityPolicyTest {

    private AvailabilityPolicy availabilityPolicy;
    private Vehiculo vehiculoDisponible;

    @BeforeEach
    void setUp() {
        availabilityPolicy = new AvailabilityPolicy();
        vehiculoDisponible = Vehiculo.builder()
                .numeroPlaca("TST001")
                .estadoVehiculo(EstadoVehiculo.DISPONIBLE)
                .activo(true)
                .fechaSoat(LocalDate.now().plusMonths(6))
                .fechaRtm(LocalDate.now().plusMonths(6))
                .tipoVehiculo(TipoVehiculo.builder().nombreTipo("Test").build())
                .build();
    }

    @Test
    @DisplayName("isAvailable retorna true para vehículo activo en estado DISPONIBLE")
    void disponibleCuandoActivoYEstadoDisponible() {
        assertTrue(availabilityPolicy.isAvailable(vehiculoDisponible));
    }

    @Test
    @DisplayName("isAvailable retorna false cuando el vehículo está inactivo")
    void noDisponibleCuandoInactivo() {
        vehiculoDisponible.setActivo(false);
        assertFalse(availabilityPolicy.isAvailable(vehiculoDisponible));
    }

    @Test
    @DisplayName("isAvailable retorna false cuando el estado no es DISPONIBLE")
    void noDisponibleCuandoEnMantenimiento() {
        vehiculoDisponible.setEstadoVehiculo(EstadoVehiculo.EN_MANTENIMIENTO);
        assertFalse(availabilityPolicy.isAvailable(vehiculoDisponible));
    }

    @Test
    @DisplayName("isAvailableForReservation retorna true con documentos vigentes")
    void aptoParaReservaConDocumentosVigentes() {
        assertTrue(availabilityPolicy.isAvailableForReservation(vehiculoDisponible));
    }

    @Test
    @DisplayName("isAvailableForReservation retorna false con SOAT vencido")
    void noAptoConSoatVencido() {
        vehiculoDisponible.setFechaSoat(LocalDate.now().minusDays(1));
        assertFalse(availabilityPolicy.isAvailableForReservation(vehiculoDisponible));
    }

    @Test
    @DisplayName("isAvailableForReservation retorna false con RTM vencida")
    void noAptoConRtmVencida() {
        vehiculoDisponible.setFechaRtm(LocalDate.now().minusDays(1));
        assertFalse(availabilityPolicy.isAvailableForReservation(vehiculoDisponible));
    }

    @Test
    @DisplayName("isAvailableForReservation ignora la validación de SOAT cuando la fecha es nula")
    void aptoConSoatNulo() {
        vehiculoDisponible.setFechaSoat(null);
        assertTrue(availabilityPolicy.isAvailableForReservation(vehiculoDisponible));
    }

    @Test
    @DisplayName("isAvailableForReservation ignora la validación de RTM cuando la fecha es nula")
    void aptoConRtmNulo() {
        vehiculoDisponible.setFechaRtm(null);
        assertTrue(availabilityPolicy.isAvailableForReservation(vehiculoDisponible));
    }

    @Test
    @DisplayName("isAvailable retorna false cuando el vehículo es nulo")
    void noDisponibleCuandoVehiculoEsNulo() {
        assertFalse(availabilityPolicy.isAvailable(null));
    }
}
