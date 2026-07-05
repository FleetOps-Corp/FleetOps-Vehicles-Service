package com.fleetops.vehicles.services.domain;

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateTransitionValidatorTest {

    private StateTransitionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StateTransitionValidator();
    }

    @Test
    void permiteTransicionesValidas() {
        assertTrue(validator.isValidTransition(EstadoVehiculo.DISPONIBLE, EstadoVehiculo.EN_MANTENIMIENTO));
        assertTrue(validator.isValidTransition(EstadoVehiculo.DISPONIBLE, EstadoVehiculo.FUERA_DE_SERVICIO));
        assertTrue(validator.isValidTransition(EstadoVehiculo.EN_MANTENIMIENTO, EstadoVehiculo.DISPONIBLE));
        assertTrue(validator.isValidTransition(EstadoVehiculo.FUERA_DE_SERVICIO, EstadoVehiculo.DISPONIBLE));
    }

    @Test
    void permiteEstadoIgual() {
        assertTrue(validator.isValidTransition(EstadoVehiculo.DISPONIBLE, EstadoVehiculo.DISPONIBLE));
    }

    @Test
    void rechazaNulosYTransicionesInvalidas() {
        assertFalse(validator.isValidTransition(null, EstadoVehiculo.DISPONIBLE));
        assertFalse(validator.isValidTransition(EstadoVehiculo.DISPONIBLE, null));
        assertFalse(validator.isValidTransition(EstadoVehiculo.FUERA_DE_SERVICIO, EstadoVehiculo.EN_MANTENIMIENTO));
        assertTrue(validator.isValidTransition(EstadoVehiculo.EN_MANTENIMIENTO, EstadoVehiculo.FUERA_DE_SERVICIO));
    }

    @Test
    void validateTransitionLanzaExcepcionCuandoEsInvalida() {
        assertThrows(IllegalStateException.class,
                () -> validator.validateTransition(EstadoVehiculo.FUERA_DE_SERVICIO, EstadoVehiculo.EN_MANTENIMIENTO));
    }

    @Test
    void validateTransitionNoLanzaCuandoEsValida() {
        assertDoesNotThrow(() -> validator.validateTransition(EstadoVehiculo.DISPONIBLE, EstadoVehiculo.EN_MANTENIMIENTO));
    }
}
