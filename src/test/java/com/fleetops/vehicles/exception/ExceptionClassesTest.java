package com.fleetops.vehicles.exception;

import com.fleetops.vehicles.dto.response.AgendaReservaResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionClassesTest {

    @Test
    void resourceNotFoundFormateaMensaje() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Vehiculo", "id", "123");
        assertTrue(ex.getMessage().contains("Vehiculo"));
        assertTrue(ex.getMessage().contains("123"));
    }

    @Test
    void duplicateResourceConstructores() {
        assertEquals("msg", new DuplicateResourceException("msg").getMessage());
        assertTrue(new DuplicateResourceException("Vehiculo", "placa", "ABC")
                .getMessage().contains("placa"));
    }

    @Test
    void businessExceptionGuardaMensaje() {
        assertEquals("regla", new BusinessException("regla").getMessage());
    }

    @Test
    void reservaConflictGuardaAgenda() {
        List<AgendaReservaResponse> agenda = List.of(
                new AgendaReservaResponse(LocalDateTime.now(), LocalDateTime.now().plusDays(1), "PENDIENTE"));
        ReservaConflictException ex = new ReservaConflictException("conflicto", agenda);
        assertEquals(1, ex.getReservas().size());
        assertEquals("conflicto", ex.getMessage());
    }

    @Test
    void errorResponseGettersSetters() {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse r = new ErrorResponse(now, 404, "Not Found", "msg", "/path");
        r.setTimestamp(now.plusSeconds(1));
        r.setStatus(500);
        r.setError("Error");
        r.setMessage("nuevo");
        r.setPath("/otro");
        r.setErrorCode("E1");
        r.setErrors(List.of("a"));

        assertEquals(now.plusSeconds(1), r.getTimestamp());
        assertEquals(500, r.getStatus());
        assertEquals("Error", r.getError());
        assertEquals("nuevo", r.getMessage());
        assertEquals("/otro", r.getPath());
        assertEquals("E1", r.getErrorCode());
        assertEquals(1, r.getErrors().size());

        ErrorResponse full = new ErrorResponse(now, 400, "Bad", "m", "/p", List.of("x"), "CODE");
        assertEquals("CODE", full.getErrorCode());
        assertEquals("Bad", full.getError());
        assertEquals("/p", full.getPath());
        assertEquals(now, full.getTimestamp());
    }
}
