package com.fleetops.vehicles.controller;

import com.fleetops.vehicles.controllers.VehicleController;
import com.fleetops.vehicles.dto.request.EstadoCambioRequest;
import com.fleetops.vehicles.dto.request.TipoVehiculoRequest;
import com.fleetops.vehicles.dto.request.UpdateReservaDatesRequest;
import com.fleetops.vehicles.dto.request.VehicleRequest;
import com.fleetops.vehicles.dto.request.VehicleUpdateRequest;
import com.fleetops.vehicles.dto.response.HistorialEstadoResponse;
import com.fleetops.vehicles.dto.response.ReservaResponse;
import com.fleetops.vehicles.dto.response.SagaResponse;
import com.fleetops.vehicles.dto.response.TipoVehiculoResponse;
import com.fleetops.vehicles.dto.response.VehicleResponse;
import com.fleetops.vehicles.exception.GlobalExceptionHandler;
import com.fleetops.vehicles.models.entities.EstadoReserva;
import com.fleetops.vehicles.models.entities.EstadoSaga;
import com.fleetops.vehicles.services.application.SagaService;
import com.fleetops.vehicles.services.application.TipoVehiculoService;
import com.fleetops.vehicles.services.application.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - VehicleController (endpoints extendidos)")
class VehicleControllerExtendedTest {

    @Mock private VehicleService vehicleService;
    @Mock private SagaService sagaService;
    @Mock private TipoVehiculoService tipoVehiculoService;

    private MockMvc mockMvc;
    private UUID vehiculoId;

    @BeforeEach
    void setUp() {
        vehiculoId = UUID.randomUUID();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new VehicleController(vehicleService, sagaService, tipoVehiculoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    private VehicleResponse vehiculo() {
        return new VehicleResponse(vehiculoId, "TWA101", "Volvo", "FH16", 2022, "Blanco", 100000,
                "Bogotá", "Terminal", "DISPONIBLE", true,
                LocalDate.now().plusMonths(6), LocalDate.now().plusMonths(6),
                LocalDate.now().minusMonths(1), LocalDateTime.now(), LocalDateTime.now(),
                "Camion", 20000.0, "Carga");
    }

    @Test
    @DisplayName("CRUD vehículos y listados por estado")
    void crudYListadosVehiculos() throws Exception {
        given(vehicleService.findAll(any())).willReturn(pageOf(vehiculo()));
        given(vehicleService.findById(vehiculoId)).willReturn(vehiculo());
        given(vehicleService.findDisponibles(any())).willReturn(pageOf(vehiculo()));
        given(vehicleService.findReservados(any())).willReturn(emptyPage());
        given(vehicleService.findMantenimiento(any())).willReturn(emptyPage());
        given(vehicleService.findFueraServicio(any())).willReturn(emptyPage());
        given(vehicleService.getDeletedVehicles(0, 10)).willReturn(emptyPage());
        given(vehicleService.findDisponiblesByNombreTipo(eq("Camion"), any()))
                .willReturn(pageOf(vehiculo()));
        given(vehicleService.create(any(VehicleRequest.class))).willReturn(vehiculo());
        given(vehicleService.update(eq(vehiculoId), any(VehicleUpdateRequest.class))).willReturn(vehiculo());
        given(vehicleService.updateByPlaca(eq("TWA101"), any(VehicleUpdateRequest.class))).willReturn(vehiculo());
        given(vehicleService.softDelete(vehiculoId)).willReturn(true);
        doNothing().when(vehicleService).deleteByPlaca("TWA101");
        given(vehicleService.reactivarVehiculo(eq(vehiculoId), any())).willReturn(vehiculo());
        given(vehicleService.reactivateByPlaca(eq("TWA101"), any())).willReturn(vehiculo());

        mockMvc.perform(get("/vehiculos")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/" + vehiculoId)).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/disponibles")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/reservados")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/mantenimiento")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/fueradeservicio")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/inactivos?page=0&size=10")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/disponibles/tipo/nombre/Camion")).andExpect(status().isOk());

        String vehicleBody = """
                {"idTipoVehiculo":1,"numeroPlaca":"ABC123","marca":"Volvo","modelo":"FH16",
                "anioFabricacion":2022,"color":"Blanco","numeroChasis":"CHS123456789",
                "numeroMotor":"MOT123456789","kilometraje":1000,"ciudadOperacion":"Bogota",
                "sedeOperacion":"Terminal","estadoVehiculo":"DISPONIBLE",
                "fechaSoat":"%s","fechaRtm":"%s","fechaUltimoMant":"%s"}
                """.formatted(
                LocalDate.now().plusMonths(6), LocalDate.now().plusMonths(6), LocalDate.now().minusMonths(1));

        mockMvc.perform(post("/vehiculos").contentType(MediaType.APPLICATION_JSON).content(vehicleBody))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/vehiculos/" + vehiculoId).contentType(MediaType.APPLICATION_JSON).content(vehicleBody))
                .andExpect(status().isOk());
        mockMvc.perform(put("/vehiculos/placa/TWA101").contentType(MediaType.APPLICATION_JSON).content(vehicleBody))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/vehiculos/" + vehiculoId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/vehiculos/placa/TWA101")).andExpect(status().isNoContent());
        mockMvc.perform(post("/vehiculos/" + vehiculoId + "/reactivar?motivo=test"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/vehiculos/placa/TWA101/reactivar?motivo=test"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Cambio de estado e historial")
    void estadoEHistorial() throws Exception {
        given(vehicleService.changeState(eq(vehiculoId), any(), any(), any())).willReturn(vehiculo());
        given(vehicleService.updateEstadoByPlaca(eq("TWA101"), any(EstadoCambioRequest.class)))
                .willReturn(vehiculo());
        given(vehicleService.getHistorialByVehiculoId(eq(vehiculoId), any()))
                .willReturn(pageOf(historial()));
        given(vehicleService.getHistorialByPlaca(eq("TWA101"), any()))
                .willReturn(pageOf(historial()));
        given(vehicleService.findAllHistorialGlobal(any()))
                .willReturn(pageOf(historial()));

        String estadoBody = """
                {"nuevoEstado":"EN_MANTENIMIENTO","motivoCambio":"revision programada","servicioOrigen":"test"}
                """;

        mockMvc.perform(patch("/vehiculos/" + vehiculoId + "/estado")
                        .contentType(MediaType.APPLICATION_JSON).content(estadoBody))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/vehiculos/placa/TWA101/estado")
                        .contentType(MediaType.APPLICATION_JSON).content(estadoBody))
                .andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/" + vehiculoId + "/historial")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/placa/TWA101/historial")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/historial")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/" + vehiculoId + "/disponibilidad")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Tipos de vehículo CRUD")
    void tiposVehiculo() throws Exception {
        TipoVehiculoResponse tipo = new TipoVehiculoResponse(
                1L, "Camion", "Carga", 20000.0, LocalDateTime.now(), LocalDateTime.now());
        given(tipoVehiculoService.create(any(TipoVehiculoRequest.class))).willReturn(tipo);
        given(tipoVehiculoService.update(eq(1L), any(TipoVehiculoRequest.class))).willReturn(tipo);
        given(tipoVehiculoService.findById(1L)).willReturn(tipo);
        given(tipoVehiculoService.findAll(any())).willReturn(pageOf(tipo));
        doNothing().when(tipoVehiculoService).delete(1L);

        String tipoBody = """
                {"nombreTipo":"Camion","descripcion":"Carga pesada","capacidadCarga":20000}
                """;

        mockMvc.perform(post("/vehiculos/tipos-vehiculo").contentType(MediaType.APPLICATION_JSON).content(tipoBody))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/vehiculos/tipos-vehiculo/1").contentType(MediaType.APPLICATION_JSON).content(tipoBody))
                .andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/tipos-vehiculo/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreTipo").value("Camion"));
        mockMvc.perform(get("/vehiculos/tipos-vehiculo")).andExpect(status().isOk());
        mockMvc.perform(delete("/vehiculos/tipos-vehiculo/1")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Reservas, sagas y compensación")
    void reservasYSagas() throws Exception {
        UUID reservaId = UUID.randomUUID();
        UUID sagaId = UUID.randomUUID();
        ReservaResponse reserva = new ReservaResponse(
                reservaId, vehiculoId, "PENDIENTE", UUID.randomUUID().toString(), "Juan",
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10),
                "clave-x", sagaId, "TWA101", "Camion", "Carga", 100000, 20000.0);
        SagaResponse saga = new SagaResponse(
                sagaId, vehiculoId, "RESERVA_VEHICULO", "EN_PROGRESO", "clave-x",
                0, "{}", null, null, LocalDateTime.now(), LocalDateTime.now(), "TWA101");

        given(sagaService.iniciarReserva(eq(vehiculoId), any())).willReturn(reserva);
        given(sagaService.findAllReservas(any())).willReturn(pageOf(reserva));
        given(sagaService.findReservasPendientes(any())).willReturn(pageOf(reserva));
        given(sagaService.findReservasConfirmadas(any())).willReturn(emptyPage());
        given(sagaService.findReservasFallidas(any())).willReturn(emptyPage());
        given(sagaService.findReservasCanceladas(any())).willReturn(emptyPage());
        given(sagaService.findReservasByPlaca(eq("TWA101"), any())).willReturn(pageOf(reserva));
        given(sagaService.findReservasByPlacaAndEstado(eq("TWA101"), eq(EstadoReserva.PENDIENTE), any()))
                .willReturn(pageOf(reserva));
        given(sagaService.actualizarFechasReserva(eq(reservaId), any(UpdateReservaDatesRequest.class)))
                .willReturn(reserva);
        given(sagaService.compensarPorReservaId(reservaId, "fallo")).willReturn(true);
        given(sagaService.confirmarReservaPorPlaca("TWA101")).willReturn(List.of(reserva));
        given(sagaService.findAllSagas(any())).willReturn(pageOf(saga));
        given(sagaService.findSagasIniciadas(any())).willReturn(emptyPage());
        given(sagaService.findSagasEnProgreso(any())).willReturn(pageOf(saga));
        given(sagaService.findSagasCompletadas(any())).willReturn(emptyPage());
        given(sagaService.findSagasFallidas(any())).willReturn(emptyPage());
        given(sagaService.findSagasCompensadas(any())).willReturn(emptyPage());
        given(sagaService.findSagasByPlaca(eq("TWA101"), any())).willReturn(pageOf(saga));
        given(sagaService.findSagasByPlacaAndEstado(eq("TWA101"), eq(EstadoSaga.EN_PROGRESO), any()))
                .willReturn(pageOf(saga));

        String reservaBody = """
                {"idAsignacionExt":"%s","solicitadoPor":"Juan",
                "fechaInicio":"%s","fechaFin":"%s","claveIdempotencia":"clave-xyz"}
                """.formatted(UUID.randomUUID(), LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10));

        mockMvc.perform(post("/vehiculos/" + vehiculoId + "/reservas")
                        .contentType(MediaType.APPLICATION_JSON).content(reservaBody))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/vehiculos/reservas")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/reservas/pendientes")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/reservas/confirmadas")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/reservas/fallidas")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/reservas/canceladas")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/reservas/placa/TWA101")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/reservas/placa/TWA101/estado/PENDIENTE")).andExpect(status().isOk());

        String fechasBody = """
                {"fechaInicio":"%s","fechaFin":"%s"}
                """.formatted(LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(11));
        mockMvc.perform(put("/vehiculos/reservas/" + reservaId + "/fechas")
                        .contentType(MediaType.APPLICATION_JSON).content(fechasBody))
                .andExpect(status().isOk());

        mockMvc.perform(post("/vehiculos/reservas/" + reservaId + "/compensar?motivo=fallo"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/vehiculos/placa/TWA101/reservas/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placa").value("TWA101"));

        mockMvc.perform(get("/vehiculos/sagas")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/sagas/iniciadas")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/sagas/en-progreso")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/sagas/completadas")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/sagas/fallidas")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/sagas/compensadas")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/sagas/placa/TWA101")).andExpect(status().isOk());
        mockMvc.perform(get("/vehiculos/sagas/placa/TWA101/estado/EN_PROGRESO")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("softDelete retorna 404 cuando el vehículo no existe")
    void softDeleteVehiculoInexistente() throws Exception {
        given(vehicleService.softDelete(vehiculoId)).willReturn(false);

        mockMvc.perform(delete("/vehiculos/" + vehiculoId)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("compensarReserva retorna 404 cuando la reserva no existe")
    void compensarReservaInexistente() throws Exception {
        UUID reservaId = UUID.randomUUID();
        given(sagaService.compensarPorReservaId(reservaId, "sin-reserva")).willReturn(false);

        mockMvc.perform(post("/vehiculos/reservas/" + reservaId + "/compensar?motivo=sin-reserva"))
                .andExpect(status().isNotFound());
    }

    private HistorialEstadoResponse historial() {
        return new HistorialEstadoResponse(
                UUID.randomUUID(), vehiculoId, "DISPONIBLE", "EN_MANTENIMIENTO",
                "Mantenimiento", "test", null, LocalDateTime.now(),
                "TWA101", "Camion", 100000, 20000.0, "Carga");
    }

    private <T> Page<T> pageOf(T item) {
        return new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
    }

    private <T> Page<T> emptyPage() {
        return new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    }
}
