package com.fleetops.vehicles.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("Tests unitarios - GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        given(request.getRequestURI()).willReturn("/api/v1/vehiculos/test");
    }

    @Test
    @DisplayName("ResourceNotFoundException retorna 404")
    void handleResourceNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("Vehículo", "id", "123"), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    @DisplayName("DuplicateResourceException retorna 409")
    void handleDuplicateResource() {
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResourceException(
                new DuplicateResourceException("Placa duplicada"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("BusinessException retorna 422")
    void handleBusinessException() {
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(
                new BusinessException("SOAT vencido"), request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("SOAT vencido", response.getBody().getMessage());
    }

    @Test
    @DisplayName("OptimisticLockException retorna 409")
    void handleOptimisticLock() {
        ResponseEntity<ErrorResponse> response = handler.handleOptimisticLockException(
                new OptimisticLockException(), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("MethodArgumentNotValidException retorna 400 con lista de errores")
    void handleValidationException() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        given(bindingResult.getFieldErrors()).willReturn(
                List.of(new FieldError("obj", "placa", "La placa es obligatoria")));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
    }

    @Test
    @DisplayName("Exception genérica retorna 500")
    void handleGlobalException() {
        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(
                new RuntimeException("error interno"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("ReservaConflictException retorna 400 con agenda de conflictos")
    void handleReservaConflict() {
        var agenda = List.of(new com.fleetops.vehicles.dto.response.AgendaReservaResponse(
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now().plusDays(1),
                "CONFIRMADA"));

        ResponseEntity<Map<String, Object>> response = handler.handleReservaConflictException(
                new ReservaConflictException("Conflicto de fechas", agenda), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().get("reservas"));
    }
}
