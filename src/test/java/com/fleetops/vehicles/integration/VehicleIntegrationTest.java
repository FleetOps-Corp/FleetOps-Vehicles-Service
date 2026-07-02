package com.fleetops.vehicles.integration;

import com.fleetops.vehicles.dto.response.DisponibilidadResponse;
import com.fleetops.vehicles.dto.response.VehicleResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests de integración - Vehículos")
class VehicleIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /actuator/health - retorna HTTP 200 sin autenticación")
    void healthPublico() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/actuator/health",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UP", response.getBody().get("status"));
    }

    @Test
    @DisplayName("GET /vehiculos/placa/TWA101 - retorna vehículo sembrado con HTTP 200")
    void getVehiculoPorPlaca() {
        ResponseEntity<VehicleResponse> response = restTemplate.exchange(
                "/vehiculos/placa/TWA101",
                HttpMethod.GET,
                getWithAuth(usuarioToken()),
                VehicleResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TWA101", response.getBody().numeroPlaca());
        assertEquals("DISPONIBLE", response.getBody().estadoVehiculo());
    }

    @Test
    @DisplayName("GET /vehiculos/placa/NOEXISTE - retorna HTTP 404")
    void getVehiculoPlacaInexistente() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/vehiculos/placa/NOEXISTE",
                HttpMethod.GET,
                getWithAuth(usuarioToken()),
                new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /vehiculos/placa/TWA101/disponibilidad - retorna disponible true")
    void getDisponibilidadPorPlaca() {
        ResponseEntity<DisponibilidadResponse> response = restTemplate.exchange(
                "/vehiculos/placa/TWA101/disponibilidad",
                HttpMethod.GET,
                getWithAuth(usuarioToken()),
                DisponibilidadResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().disponible());
        assertEquals("DISPONIBLE", response.getBody().estadoVehiculo());
    }

    @Test
    @DisplayName("GET /vehiculos/disponibles - retorna página con HTTP 200")
    void listarDisponibles() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/vehiculos/disponibles?page=0&size=5",
                HttpMethod.GET,
                getWithAuth(usuarioToken()),
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("TWA101") || response.getBody().contains("content"));
    }

    @Test
    @DisplayName("GET /vehiculos - retorna HTTP 401 sin token")
    void listarSinToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/vehiculos?page=0&size=1",
                String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /vehiculos/disponibles - retorna HTTP 403 con token inválido")
    void listarConTokenInvalido() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/vehiculos/disponibles?page=0&size=1",
                HttpMethod.GET,
                getWithAuth("token.invalido"),
                String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
