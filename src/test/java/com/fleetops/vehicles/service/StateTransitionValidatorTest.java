package com.fleetops.vehicles.service;

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.services.domain.StateTransitionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests unitarios - StateTransitionValidator")
class StateTransitionValidatorTest {

    private StateTransitionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StateTransitionValidator();
    }

    @Test
    @DisplayName("Permite transición DISPONIBLE -> EN_MANTENIMIENTO")
    void transicionDisponibleAMantenimiento() {
        assertTrue(validator.isValidTransition(EstadoVehiculo.DISPONIBLE, EstadoVehiculo.EN_MANTENIMIENTO));
        validator.validateTransition(EstadoVehiculo.DISPONIBLE, EstadoVehiculo.EN_MANTENIMIENTO);
    }

    @Test
    @DisplayName("Permite transición EN_MANTENIMIENTO -> DISPONIBLE")
    void transicionMantenimientoADisponible() {
        assertTrue(validator.isValidTransition(EstadoVehiculo.EN_MANTENIMIENTO, EstadoVehiculo.DISPONIBLE));
    }

    @Test
    @DisplayName("Rechaza transición EN_MANTENIMIENTO -> RESERVADO")
    void transicionMantenimientoAReservadoInvalida() {
        assertFalse(validator.isValidTransition(EstadoVehiculo.EN_MANTENIMIENTO, EstadoVehiculo.RESERVADO));
        assertThrows(IllegalStateException.class,
                () -> validator.validateTransition(EstadoVehiculo.EN_MANTENIMIENTO, EstadoVehiculo.RESERVADO));
    }

    @Test
    @DisplayName("Permite permanecer en el mismo estado")
    void mismoEstadoEsValido() {
        assertTrue(validator.isValidTransition(EstadoVehiculo.DISPONIBLE, EstadoVehiculo.DISPONIBLE));
    }

    @Test
    @DisplayName("Rechaza transición si algún estado es null")
    void estadosNulosInvalidos() {
        assertFalse(validator.isValidTransition(null, EstadoVehiculo.DISPONIBLE));
        assertFalse(validator.isValidTransition(EstadoVehiculo.DISPONIBLE, null));
    }

    @Test
    @DisplayName("FUERA_DE_SERVICIO solo puede pasar a DISPONIBLE")
    void fueraDeServicioSoloADisponible() {
        assertTrue(validator.isValidTransition(EstadoVehiculo.FUERA_DE_SERVICIO, EstadoVehiculo.DISPONIBLE));
        assertFalse(validator.isValidTransition(EstadoVehiculo.FUERA_DE_SERVICIO, EstadoVehiculo.RESERVADO));
    }
}
