package com.fleetops.vehicles.controller;

import com.fleetops.vehicles.controllers.VehicleController;
import com.fleetops.vehicles.dto.request.ReservaRequest;
import com.fleetops.vehicles.dto.response.DisponibilidadResponse;
import com.fleetops.vehicles.dto.response.ReservaResponse;
import com.fleetops.vehicles.dto.response.VehicleResponse;
import com.fleetops.vehicles.exception.GlobalExceptionHandler;
import com.fleetops.vehicles.services.application.SagaService;
import com.fleetops.vehicles.services.application.TipoVehiculoService;
import com.fleetops.vehicles.services.application.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - VehicleController")
class VehicleControllerTest {

    @Mock
    private VehicleService vehicleService;
    @Mock
    private SagaService sagaService;
    @Mock
    private TipoVehiculoService tipoVehiculoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        VehicleController controller = new VehicleController(vehicleService, sagaService, tipoVehiculoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    private VehicleResponse vehiculoResponse() {
        return new VehicleResponse(
                UUID.randomUUID(), "TWA101", "Volvo", "FH16", 2022, "Blanco", 100000,
                "Bogotá", "Terminal", "DISPONIBLE", true,
                LocalDate.now().plusMonths(6), LocalDate.now().plusMonths(6),
                LocalDate.now().minusMonths(1), LocalDateTime.now(), LocalDateTime.now(),
                "Camion", 20000.0, "Carga pesada");
    }

    @Test
    @DisplayName("GET /vehiculos/placa/{placa} retorna 200")
    void getPorPlaca() throws Exception {
        given(vehicleService.findByPlaca("TWA101")).willReturn(vehiculoResponse());

        mockMvc.perform(get("/vehiculos/placa/TWA101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroPlaca").value("TWA101"));
    }

    @Test
    @DisplayName("GET /vehiculos/placa/{placa}/disponibilidad retorna disponible")
    void getDisponibilidadPorPlaca() throws Exception {
        given(vehicleService.getDisponibilidadByPlaca("TWA101"))
                .willReturn(new DisponibilidadResponse(
                        UUID.randomUUID(), "DISPONIBLE", true, LocalDateTime.now()));

        mockMvc.perform(get("/vehiculos/placa/TWA101/disponibilidad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test
    @DisplayName("POST /vehiculos/placa/{placa}/reservas retorna 201")
    void crearReservaPorPlaca() throws Exception {
        UUID reservaId = UUID.randomUUID();
        ReservaResponse reserva = new ReservaResponse(
                reservaId, vehiculoResponse().idVehiculo(), "PENDIENTE",
                UUID.randomUUID().toString(), "Juan",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3),
                "clave-ctrl", UUID.randomUUID(), "TWA101", "Camion", "Carga", 100000, 20000.0);

        given(sagaService.iniciarReservaByPlaca(eq("TWA101"), any(ReservaRequest.class)))
                .willReturn(reserva);

        String body = """
                {
                  "idAsignacionExt": "%s",
                  "solicitadoPor": "Juan",
                  "fechaInicio": "%s",
                  "fechaFin": "%s",
                  "claveIdempotencia": "clave-ctrl-test"
                }
                """.formatted(
                UUID.randomUUID(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3));

        mockMvc.perform(post("/vehiculos/placa/TWA101/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estadoReserva").value("PENDIENTE"));
    }

    @Test
    @DisplayName("GET /vehiculos/reservas/{id} retorna 404 si no existe")
    void getReservaPorIdNoEncontrada() throws Exception {
        UUID reservaId = UUID.randomUUID();
        given(sagaService.findReservaById(reservaId)).willReturn(Optional.empty());

        mockMvc.perform(get("/vehiculos/reservas/" + reservaId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /vehiculos/reservas/{id} retorna reserva")
    void getReservaPorId() throws Exception {
        UUID reservaId = UUID.randomUUID();
        ReservaResponse reserva = new ReservaResponse(
                reservaId, UUID.randomUUID(), "CONFIRMADA",
                UUID.randomUUID().toString(), "Ana",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3),
                "clave-get", UUID.randomUUID(), "TWA101", "Camion", "Carga", 100000, 20000.0);

        given(sagaService.findReservaById(reservaId)).willReturn(Optional.of(reserva));

        mockMvc.perform(get("/vehiculos/reservas/" + reservaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoReserva").value("CONFIRMADA"));
    }

    @Test
    @DisplayName("POST /vehiculos/reservas/{id}/confirmar retorna 200")
    void confirmarReserva() throws Exception {
        UUID reservaId = UUID.randomUUID();
        ReservaResponse reserva = new ReservaResponse(
                reservaId, UUID.randomUUID(), "CONFIRMADA",
                UUID.randomUUID().toString(), "Ana",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3),
                "clave-conf", UUID.randomUUID(), "TWA101", "Camion", "Carga", 100000, 20000.0);

        given(sagaService.confirmarReserva(reservaId)).willReturn(Optional.of(reserva));

        mockMvc.perform(post("/vehiculos/reservas/" + reservaId + "/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroPlaca").value("TWA101"));
    }
}
