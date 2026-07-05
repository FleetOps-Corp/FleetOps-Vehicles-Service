package com.fleetops.vehicles.services.domain;

import com.fleetops.vehicles.dto.request.ReservaRequest;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DateRangeValidatorTest {

    private DateRangeValidator validator;

    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void setUp() {
        validator = new DateRangeValidator();
    }

    @Test
    void aceptaRequestNuloOFechasNulas() {
        assertTrue(validator.isValid(null, context));
        ReservaRequest parcial = new ReservaRequest("id", "user", null, LocalDateTime.now(), "k");
        assertTrue(validator.isValid(parcial, context));
    }

    @Test
    void aceptaRangoValido() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        ReservaRequest request = new ReservaRequest(
                UUID.randomUUID().toString(), "user", inicio, inicio.plusDays(1), "k");
        assertTrue(validator.isValid(request, context));
    }

    @Test
    void rechazaFinAntesDeInicio() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context);

        LocalDateTime inicio = LocalDateTime.now().plusDays(2);
        ReservaRequest request = new ReservaRequest(
                UUID.randomUUID().toString(), "user", inicio, inicio.minusDays(1), "k");

        assertFalse(validator.isValid(request, context));
        verify(context).disableDefaultConstraintViolation();
    }
}
