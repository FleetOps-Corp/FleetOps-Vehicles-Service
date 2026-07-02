package com.fleetops.vehicles.integration;

import com.fleetops.vehicles.dto.request.TipoVehiculoRequest;
import com.fleetops.vehicles.dto.response.TipoVehiculoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests de integración - Tipos de vehículo")
class TipoVehiculoIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /vehiculos/tipos-vehiculo/1 - retorna tipo sembrado por Flyway con HTTP 200")
    void getTipoPorIdSembrado() {
        ResponseEntity<TipoVehiculoResponse> response = restTemplate.exchange(
                "/vehiculos/tipos-vehiculo/1",
                HttpMethod.GET,
                getWithAuth(usuarioToken()),
                TipoVehiculoResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Camion Carga Pesada", response.getBody().nombreTipo());
    }

    @Test
    @DisplayName("GET /vehiculos/tipos-vehiculo/99999 - retorna HTTP 404")
    void getTipoPorIdInexistente() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/vehiculos/tipos-vehiculo/99999",
                HttpMethod.GET,
                getWithAuth(usuarioToken()),
                new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /vehiculos/tipos-vehiculo - crea tipo y retorna HTTP 201")
    void crearTipoExitosamente() {
        String nombreUnico = "Tipo Test " + UUID.randomUUID();
        TipoVehiculoRequest request = new TipoVehiculoRequest(
                nombreUnico,
                "Descripcion de prueba de integracion",
                1500.5);

        ResponseEntity<TipoVehiculoResponse> response = restTemplate.exchange(
                "/vehiculos/tipos-vehiculo",
                HttpMethod.POST,
                jsonWithAuth(request, adminToken()),
                TipoVehiculoResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(nombreUnico, response.getBody().nombreTipo());
    }

    @Test
    @DisplayName("POST /vehiculos/tipos-vehiculo - retorna HTTP 409 con nombre duplicado")
    void crearTipoDuplicado() {
        TipoVehiculoRequest request = new TipoVehiculoRequest(
                "Camion Carga Pesada",
                "Intento duplicado",
                1000.0);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/vehiculos/tipos-vehiculo",
                HttpMethod.POST,
                jsonWithAuth(request, adminToken()),
                new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /vehiculos/tipos-vehiculo - retorna HTTP 403 sin rol ADMIN")
    void crearTipoSinPermisos() {
        TipoVehiculoRequest request = new TipoVehiculoRequest(
                "Tipo Sin Permiso",
                "Descripcion",
                500.0);

        ResponseEntity<String> response = restTemplate.exchange(
                "/vehiculos/tipos-vehiculo",
                HttpMethod.POST,
                jsonWithAuth(request, usuarioToken()),
                String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /vehiculos/tipos-vehiculo - retorna lista paginada con HTTP 200")
    void listarTipos() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/vehiculos/tipos-vehiculo?page=0&size=5",
                HttpMethod.GET,
                getWithAuth(usuarioToken()),
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("content"));
    }
}
