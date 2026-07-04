package com.fleetops.vehicles.exception;

import com.fleetops.vehicles.dto.response.AgendaReservaResponse;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/vehiculos");
    }

    @Test
    void manejaNotFoundDuplicateYBusiness() {
        assertEquals(HttpStatus.NOT_FOUND,
                handler.handleResourceNotFoundException(new ResourceNotFoundException("x"), request).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                handler.handleDuplicateResourceException(new DuplicateResourceException("dup"), request).getStatusCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
                handler.handleBusinessException(new BusinessException("biz"), request).getStatusCode());
    }

    @Test
    void manejaOptimisticLockYValidacion() {
        assertEquals(HttpStatus.CONFLICT,
                handler.handleOptimisticLockException(new OptimisticLockException(), request).getStatusCode());

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("obj", "campo", "obligatorio")));

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(1, response.getBody().getErrors().size());
    }

    @Test
    void manejaExcepcionGeneralYReservaConflict() {
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                handler.handleGlobalException(new RuntimeException("boom"), request).getStatusCode());

        ReservaConflictException conflict = new ReservaConflictException("agenda",
                List.of(new AgendaReservaResponse(LocalDateTime.now(), LocalDateTime.now().plusDays(1), "PENDIENTE")));
        ResponseEntity<Map<String, Object>> response = handler.handleReservaConflictException(conflict, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().containsKey("reservas"));
    }

    @Test
    void manejaDataIntegrityConMensajesEspecificos() {
        assertMessageContains("placa", "numero_placa");
        assertMessageContains("chasis", "numero_chasis");
        assertMessageContains("motor", "numero_motor");
        assertMessageContains("idempotencia", "clave_idempotencia");
        assertMessageContains("asignación", "uq_reservas_id_asignacion_ext");

        DataIntegrityViolationException generica = new DataIntegrityViolationException("otro",
                new RuntimeException("constraint desconocido"));
        assertEquals(HttpStatus.CONFLICT,
                handler.handleDataIntegrityViolationException(generica, request).getStatusCode());
    }

    @Test
    void manejaIllegalArgumentYIllegalState() {
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handleIllegalArgumentException(new IllegalArgumentException("bad"), request).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                handler.handleIllegalStateException(new IllegalStateException("state"), request).getStatusCode());
    }

    private void assertMessageContains(String expectedFragment, String causeFragment) {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("conflict",
                new RuntimeException("Detail: Key (" + causeFragment + ") already exists"));
        ErrorResponse body = handler.handleDataIntegrityViolationException(ex, request).getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().toLowerCase().contains(expectedFragment.toLowerCase())
                || body.getMessage().toLowerCase().contains("asignacion")
                || body.getMessage().toLowerCase().contains("asignación"));
    }
}
