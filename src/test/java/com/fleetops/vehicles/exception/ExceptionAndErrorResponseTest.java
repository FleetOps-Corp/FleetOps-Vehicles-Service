package com.fleetops.vehicles.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Tests unitarios - Excepciones y ErrorResponse")
class ExceptionAndErrorResponseTest {

    @Test
    @DisplayName("ResourceNotFoundException formatea mensaje con recurso y campo")
    void resourceNotFoundFormatted() {
        var ex = new ResourceNotFoundException("Vehículo", "placa", "ABC123");
        assertEquals("Vehículo no encontrado con placa: 'ABC123'", ex.getMessage());
    }

    @Test
    @DisplayName("ResourceNotFoundException acepta mensaje directo")
    void resourceNotFoundSimple() {
        var ex = new ResourceNotFoundException("No existe");
        assertEquals("No existe", ex.getMessage());
    }

    @Test
    @DisplayName("BusinessException conserva mensaje")
    void businessException() {
        assertEquals("Regla violada", new BusinessException("Regla violada").getMessage());
    }

    @Test
    @DisplayName("DuplicateResourceException conserva mensaje")
    void duplicateResourceException() {
        var ex = new DuplicateResourceException("Vehículo", "placa", "XYZ");
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("ReservaConflictException expone agenda")
    void reservaConflictException() {
        var agenda = List.of(new com.fleetops.vehicles.dto.response.AgendaReservaResponse(
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), "PENDIENTE"));
        var ex = new ReservaConflictException("Conflicto", agenda);
        assertEquals(1, ex.getReservas().size());
    }

    @Test
    @DisplayName("ErrorResponse getters y setters")
    void errorResponseAccessors() {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(), 400, "Bad Request", "Detalle", "/path",
                List.of("error1"), "VALIDATION_ERROR");

        assertEquals(400, response.getStatus());
        assertEquals("Detalle", response.getMessage());
        assertEquals(1, response.getErrors().size());
        assertEquals("VALIDATION_ERROR", response.getErrorCode());

        response.setStatus(500);
        assertEquals(500, response.getStatus());
    }

    @Test
    @DisplayName("ErrorResponse constructor básico y todos los getters/setters")
    void errorResponseConstructorBasicoYAccesoresCompletos() {
        LocalDateTime timestamp = LocalDateTime.now();
        ErrorResponse response = new ErrorResponse(timestamp, 404, "Not Found", "No encontrado", "/vehiculos/1");

        assertEquals(timestamp, response.getTimestamp());
        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("No encontrado", response.getMessage());
        assertEquals("/vehiculos/1", response.getPath());

        LocalDateTime nuevoTimestamp = timestamp.plusMinutes(1);
        response.setTimestamp(nuevoTimestamp);
        response.setError("Bad Request");
        response.setMessage("Mensaje actualizado");
        response.setPath("/vehiculos/2");
        response.setErrors(List.of("campo invalido"));
        response.setErrorCode("ERR-001");

        assertEquals(nuevoTimestamp, response.getTimestamp());
        assertEquals("Bad Request", response.getError());
        assertEquals("Mensaje actualizado", response.getMessage());
        assertEquals("/vehiculos/2", response.getPath());
        assertEquals(1, response.getErrors().size());
        assertEquals("ERR-001", response.getErrorCode());
    }
}
