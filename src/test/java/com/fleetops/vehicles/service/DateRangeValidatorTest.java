package com.fleetops.vehicles.service;

import com.fleetops.vehicles.dto.request.ReservaRequest;
import com.fleetops.vehicles.services.domain.DateRangeValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - DateRangeValidator")
class DateRangeValidatorTest {

    private DateRangeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DateRangeValidator();
    }

    @Test
    @DisplayName("Acepta request null (otras validaciones lo capturan)")
    void requestNullEsValido() {
        assertTrue(validator.isValid(null, mock(ConstraintValidatorContext.class)));
    }

    @Test
    @DisplayName("Acepta fechas con fin posterior al inicio")
    void fechasValidas() {
        ReservaRequest request = new ReservaRequest(
                UUID.randomUUID().toString(),
                "Juan",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                "clave-1");

        assertTrue(validator.isValid(request, mock(ConstraintValidatorContext.class)));
    }

    @Test
    @DisplayName("Rechaza fin anterior o igual al inicio")
    void fechasInvalidas() {
        ReservaRequest request = new ReservaRequest(
                UUID.randomUUID().toString(),
                "Juan",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(1),
                "clave-2");

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);

        assertFalse(validator.isValid(request, context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Acepta si alguna fecha es null")
    void fechaNullDelegada() {
        ReservaRequest request = new ReservaRequest(
                UUID.randomUUID().toString(),
                "Juan",
                LocalDateTime.now().plusDays(1),
                null,
                "clave-3");

        assertTrue(validator.isValid(request, mock(ConstraintValidatorContext.class)));
    }
}
