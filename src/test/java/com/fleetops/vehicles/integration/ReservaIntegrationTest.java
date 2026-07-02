package com.fleetops.vehicles.integration;

import com.fleetops.vehicles.dto.request.ReservaRequest;
import com.fleetops.vehicles.dto.response.ReservaResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Tests de integración - Reservas y Saga")
class ReservaIntegrationTest extends BaseIntegrationTest {

    private ReservaRequest nuevaReserva() {
        return new ReservaRequest(
                UUID.randomUUID().toString(),
                "Integracion Test",
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(15),
                "clave-int-" + UUID.randomUUID());
    }

    @Test
    @DisplayName("POST /vehiculos/placa/TWA101/reservas - crea reserva con HTTP 201")
    void crearReservaPorPlaca() {
        ReservaRequest request = nuevaReserva();

        ResponseEntity<ReservaResponse> response = restTemplate.exchange(
                "/vehiculos/placa/TWA101/reservas",
                HttpMethod.POST,
                jsonWithAuth(request, operadorToken()),
                ReservaResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PENDIENTE", response.getBody().estadoReserva());
        assertEquals("TWA101", response.getBody().numeroPlaca());
    }

    @Test
    @DisplayName("Flujo completo: crear reserva, confirmar y consultar por ID")
    void flujoReservaConfirmarYConsultar() {
        ReservaRequest request = nuevaReserva();

        ResponseEntity<ReservaResponse> crear = restTemplate.exchange(
                "/vehiculos/placa/TWA101/reservas",
                HttpMethod.POST,
                jsonWithAuth(request, operadorToken()),
                ReservaResponse.class);

        assertEquals(HttpStatus.CREATED, crear.getStatusCode());
        UUID reservaId = crear.getBody().idReserva();

        ResponseEntity<ReservaResponse> confirmar = restTemplate.exchange(
                "/vehiculos/reservas/" + reservaId + "/confirmar",
                HttpMethod.POST,
                getWithAuth(operadorToken()),
                ReservaResponse.class);

        assertEquals(HttpStatus.OK, confirmar.getStatusCode());
        assertEquals("CONFIRMADA", confirmar.getBody().estadoReserva());

        ResponseEntity<ReservaResponse> consultar = restTemplate.exchange(
                "/vehiculos/reservas/" + reservaId,
                HttpMethod.GET,
                getWithAuth(usuarioToken()),
                ReservaResponse.class);

        assertEquals(HttpStatus.OK, consultar.getStatusCode());
        assertEquals("CONFIRMADA", consultar.getBody().estadoReserva());
    }

    @Test
    @DisplayName("GET /vehiculos/reservas/pendientes - retorna página con HTTP 200")
    void listarReservasPendientes() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/vehiculos/reservas/pendientes?page=0&size=5",
                HttpMethod.GET,
                getWithAuth(usuarioToken()),
                new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("POST reserva con idAsignacionExt inválido retorna HTTP 500")
    void reservaIdAsignacionInvalido() {
        ReservaRequest request = new ReservaRequest(
                "no-es-uuid",
                "Test",
                LocalDateTime.now().plusDays(20),
                LocalDateTime.now().plusDays(25),
                "clave-invalid-" + UUID.randomUUID());

        ResponseEntity<String> response = restTemplate.exchange(
                "/vehiculos/placa/TWA101/reservas",
                HttpMethod.POST,
                jsonWithAuth(request, operadorToken()),
                String.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
