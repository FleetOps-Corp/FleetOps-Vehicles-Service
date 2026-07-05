package com.fleetops.vehicles.services.domain;

import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.repositories.SagaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyValidatorTest {

    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private SagaRepository sagaRepository;
    @InjectMocks
    private IdempotencyValidator validator;

    @Test
    void claveVaciaNoEsDuplicado() {
        assertFalse(validator.isDuplicate(null));
        assertFalse(validator.isDuplicate("  "));
    }

    @Test
    void detectaDuplicadoEnReservaOSaga() {
        when(reservaRepository.existsByClaveIdempotencia("k1")).thenReturn(true);
        when(sagaRepository.existsByClaveIdempotencia("k1")).thenReturn(false);
        assertTrue(validator.isDuplicate("k1"));

        when(reservaRepository.existsByClaveIdempotencia("k2")).thenReturn(false);
        when(sagaRepository.existsByClaveIdempotencia("k2")).thenReturn(true);
        assertTrue(validator.isDuplicate("k2"));
    }

    @Test
    void validateNotDuplicateLanzaSiExiste() {
        when(reservaRepository.existsByClaveIdempotencia("dup")).thenReturn(true);
        when(sagaRepository.existsByClaveIdempotencia("dup")).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> validator.validateNotDuplicate("dup"));
    }

    @Test
    void validateNotDuplicatePasaSiEsNueva() {
        when(reservaRepository.existsByClaveIdempotencia("new")).thenReturn(false);
        when(sagaRepository.existsByClaveIdempotencia("new")).thenReturn(false);
        assertDoesNotThrow(() -> validator.validateNotDuplicate("new"));
    }
}
