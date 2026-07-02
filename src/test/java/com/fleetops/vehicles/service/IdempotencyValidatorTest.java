package com.fleetops.vehicles.service;

import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.repositories.SagaRepository;
import com.fleetops.vehicles.services.domain.IdempotencyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - IdempotencyValidator")
class IdempotencyValidatorTest {

    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private SagaRepository sagaRepository;

    private IdempotencyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IdempotencyValidator(reservaRepository, sagaRepository);
    }

    @Test
    @DisplayName("Clave vacía o null no se considera duplicada")
    void claveVaciaNoEsDuplicada() {
        assertFalse(validator.isDuplicate(null));
        assertFalse(validator.isDuplicate(""));
        assertFalse(validator.isDuplicate("   "));
    }

    @Test
    @DisplayName("Detecta duplicado en reservas")
    void duplicadoEnReserva() {
        given(reservaRepository.existsByClaveIdempotencia("clave-abc")).willReturn(true);
        given(sagaRepository.existsByClaveIdempotencia("clave-abc")).willReturn(false);

        assertTrue(validator.isDuplicate("clave-abc"));
        assertThrows(IllegalStateException.class, () -> validator.validateNotDuplicate("clave-abc"));
    }

    @Test
    @DisplayName("Detecta duplicado en sagas")
    void duplicadoEnSaga() {
        given(reservaRepository.existsByClaveIdempotencia("clave-saga")).willReturn(false);
        given(sagaRepository.existsByClaveIdempotencia("clave-saga")).willReturn(true);

        assertTrue(validator.isDuplicate("clave-saga"));
    }

    @Test
    @DisplayName("Clave nueva pasa validación")
    void claveNuevaValida() {
        given(reservaRepository.existsByClaveIdempotencia("nueva-clave")).willReturn(false);
        given(sagaRepository.existsByClaveIdempotencia("nueva-clave")).willReturn(false);

        assertFalse(validator.isDuplicate("nueva-clave"));
        validator.validateNotDuplicate("nueva-clave");
    }
}
