package com.fleetops.vehicles.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Tests de integración - Seguridad")
class SecurityIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /actuator/health es público")
    void healthPublico() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /vehiculos sin token retorna 401")
    void vehiculosSinToken() {
        ResponseEntity<String> response = restTemplate.getForEntity("/vehiculos?page=0&size=1", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /vehiculos con token válido retorna 200")
    void vehiculosConToken() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/vehiculos?page=0&size=1",
                HttpMethod.GET,
                getWithAuth(usuarioToken()),
                String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("GET disponibilidad por placa es público")
    void disponibilidadPublica() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/vehiculos/placa/TWA101/disponibilidad", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
