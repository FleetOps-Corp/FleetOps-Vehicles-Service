package com.fleetops.vehicles.controllers;

import com.fleetops.vehicles.dto.request.*;
import com.fleetops.vehicles.dto.response.*;
import com.fleetops.vehicles.models.entities.EstadoReserva;
import com.fleetops.vehicles.models.entities.EstadoSaga;
import com.fleetops.vehicles.services.application.SagaService;
import com.fleetops.vehicles.services.application.TipoVehiculoService;
import com.fleetops.vehicles.services.application.VehicleService;
import com.fleetops.vehicles.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock private VehicleService vehicleService;
    @Mock private SagaService sagaService;
    @Mock private TipoVehiculoService tipoVehiculoService;
    @InjectMocks private VehicleController controller;

    @Test
    void tiposVehiculoEndpoints() {
        TipoVehiculoResponse tipo = new TipoVehiculoResponse(1L, "Furgon", "d", 1.0, LocalDateTime.now(), null);
        when(tipoVehiculoService.create(any())).thenReturn(tipo);
        when(tipoVehiculoService.update(eq(1L), any())).thenReturn(tipo);
        when(tipoVehiculoService.findAll(any())).thenReturn(new PageImpl<>(List.of(tipo)));
        when(tipoVehiculoService.findById(1L)).thenReturn(tipo);

        assertEquals(HttpStatus.CREATED, controller.createTipoVehiculo(TestDataFactory.tipoRequest()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.updateTipoVehiculo(1L, TestDataFactory.tipoRequest()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.findAllTiposVehiculo(PageRequest.of(0, 10)).getStatusCode());
        assertEquals(HttpStatus.OK, controller.findTipoVehiculoById(1L).getStatusCode());

        doNothing().when(tipoVehiculoService).delete(1L);
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteTipoVehiculo(1L).getStatusCode());
    }

    @Test
    void vehiculosCrudYEstados() {
        VehicleResponse vehicle = mock(VehicleResponse.class);
        when(vehicleService.create(any())).thenReturn(vehicle);
        when(vehicleService.update(any(), any())).thenReturn(vehicle);
        when(vehicleService.updateByPlaca(anyString(), any())).thenReturn(vehicle);
        when(vehicleService.findAll(any())).thenReturn(new PageImpl<>(List.of(vehicle)));
        when(vehicleService.findById(any())).thenReturn(vehicle);
        when(vehicleService.findByPlaca(anyString())).thenReturn(vehicle);
        when(vehicleService.findDisponibles(any())).thenReturn(new PageImpl<>(List.of()));
        when(vehicleService.findReservados(any())).thenReturn(new PageImpl<>(List.of()));
        when(vehicleService.findMantenimiento(any())).thenReturn(new PageImpl<>(List.of()));
        when(vehicleService.findFueraServicio(any())).thenReturn(new PageImpl<>(List.of()));
        when(vehicleService.getDeletedVehicles(0, 10)).thenReturn(new PageImpl<>(List.of()));
        when(vehicleService.changeState(any(), anyString(), anyString(), anyString())).thenReturn(vehicle);
        when(vehicleService.updateEstadoByPlaca(anyString(), any())).thenReturn(vehicle);
        when(vehicleService.softDelete(any())).thenReturn(true);
        when(vehicleService.reactivarVehiculo(any(), anyString())).thenReturn(vehicle);
        when(vehicleService.reactivateByPlaca(anyString(), anyString())).thenReturn(vehicle);
        when(vehicleService.getDisponibilidad(any())).thenReturn(
                new DisponibilidadResponse(UUID.randomUUID(), "DISPONIBLE", true, LocalDateTime.now()));
        when(vehicleService.getDisponibilidadByPlaca(anyString())).thenReturn(
                new DisponibilidadResponse(UUID.randomUUID(), "DISPONIBLE", true, LocalDateTime.now()));
        when(vehicleService.getHistorialByVehiculoId(any(), any())).thenReturn(new PageImpl<>(List.of()));
        when(vehicleService.getHistorialByPlaca(anyString(), any())).thenReturn(new PageImpl<>(List.of()));
        when(vehicleService.findAllHistorialGlobal(any())).thenReturn(new PageImpl<>(List.of()));
        when(vehicleService.findDisponiblesByNombreTipo(anyString(), any())).thenReturn(new PageImpl<>(List.of()));

        UUID id = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);

        assertEquals(HttpStatus.CREATED, controller.create(TestDataFactory.vehicleRequest()).getStatusCode());
        assertEquals(HttpStatus.OK, controller.update(id, TestDataFactory.vehicleUpdateRequest(1L)).getStatusCode());
        assertEquals(HttpStatus.OK, controller.updateByPlaca("ABC123", TestDataFactory.vehicleUpdateRequest(1L)).getStatusCode());
        assertEquals(HttpStatus.OK, controller.findAll(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.findById(id).getStatusCode());
        assertEquals(HttpStatus.OK, controller.findByPlaca("ABC123").getStatusCode());
        assertEquals(HttpStatus.OK, controller.findDisponibles(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.findReservados(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.findMantenimiento(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.findFueraServicio(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.listarInactivos(0, 10).getStatusCode());

        EstadoCambioRequest estado = new EstadoCambioRequest("EN_MANTENIMIENTO", "motivo largo", "ops", null);
        assertEquals(HttpStatus.OK, controller.changeState(id, estado).getStatusCode());
        assertEquals(HttpStatus.OK, controller.updateEstadoByPlaca("ABC123", estado).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.softDelete(id).getStatusCode());
        assertEquals(HttpStatus.OK, controller.reactivar(id, "motivo").getStatusCode());
        assertEquals(HttpStatus.OK, controller.reactivateByPlaca("ABC123", "motivo").getStatusCode());
        assertEquals(HttpStatus.OK, controller.getDisponibilidad(id).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getDisponibilidadByPlaca("ABC123").getStatusCode());
        when(vehicleService.getDisponibilidadEnRango(any(), any(), any()))
                .thenReturn(new DisponibilidadRangoResponse(
                        id, "ABC123", "DISPONIBLE", true, true, true,
                        LocalDate.now(), LocalDate.now().plusDays(1), null, LocalDateTime.now()));
        assertEquals(HttpStatus.OK,
                controller.getDisponibilidadEnRango(id, LocalDate.now(), LocalDate.now().plusDays(1)).getStatusCode());
        when(vehicleService.getDisponibilidadEnRangoByPlaca(anyString(), any(), any()))
                .thenReturn(new DisponibilidadRangoResponse(
                        id, "ABC123", "DISPONIBLE", true, true, true,
                        LocalDate.now(), LocalDate.now().plusDays(1), null, LocalDateTime.now()));
        assertEquals(HttpStatus.OK, controller.getDisponibilidadEnRangoByPlaca(
                "ABC123", LocalDate.now(), LocalDate.now().plusDays(1)).getStatusCode());
        when(vehicleService.findDisponiblesEnRango(anyString(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(vehicle)));
        assertEquals(HttpStatus.OK, controller.getDisponiblesEnRango(
                "Fur", LocalDate.now(), LocalDate.now().plusDays(1), pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getHistorial(id, pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getHistorialByPlaca("ABC123", pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getHistorialGlobal(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getDisponiblesByNombreTipo("Fur", pageable).getStatusCode());

        doNothing().when(vehicleService).deleteByPlaca("ABC123");
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteByPlaca("ABC123").getStatusCode());
    }

    @Test
    void reservasYSagasEndpoints() {
        ReservaResponse reserva = mock(ReservaResponse.class);
        SagaResponse saga = mock(SagaResponse.class);
        UUID id = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);

        when(sagaService.findReservaById(any())).thenReturn(Optional.of(reserva));
        when(sagaService.compensarPorReservaId(any(), anyString())).thenReturn(reserva);
        when(sagaService.cancelarReservasPorPlaca(anyString(), anyString())).thenReturn(List.of(reserva));
        when(sagaService.findAllReservas(any())).thenReturn(new PageImpl<>(List.of(reserva)));
        when(sagaService.findReservasPendientes(any())).thenReturn(new PageImpl<>(List.of()));
        when(sagaService.findReservasConfirmadas(any())).thenReturn(new PageImpl<>(List.of()));
        when(sagaService.findReservasCanceladas(any())).thenReturn(new PageImpl<>(List.of()));
        when(sagaService.findReservasFallidas(any())).thenReturn(new PageImpl<>(List.of()));
        when(sagaService.findReservasByPlaca(anyString(), any())).thenReturn(new PageImpl<>(List.of()));
        when(sagaService.findReservasByPlacaAndEstado(anyString(), any(), any())).thenReturn(new PageImpl<>(List.of()));
        when(sagaService.findAllSagas(any())).thenReturn(new PageImpl<>(List.of(saga)));
        when(sagaService.findSagasIniciadas(any())).thenReturn(new PageImpl<>(List.of()));
        when(sagaService.findSagasEnProgreso(any())).thenReturn(new PageImpl<>(List.of()));
        when(sagaService.findSagasCompletadas(any())).thenReturn(new PageImpl<>(List.of()));
        when(sagaService.findSagasFallidas(any())).thenReturn(new PageImpl<>(List.of()));
        when(sagaService.findSagasCompensadas(any())).thenReturn(new PageImpl<>(List.of()));
        when(sagaService.findSagasByPlaca(anyString(), any())).thenReturn(new PageImpl<>(List.of()));
        when(sagaService.findSagasByPlacaAndEstado(anyString(), any(), any())).thenReturn(new PageImpl<>(List.of()));

        assertEquals(HttpStatus.OK, controller.obtenerReserva(id).getStatusCode());
        assertEquals(HttpStatus.OK, controller.compensarReserva(id, "motivo").getStatusCode());
        assertEquals(HttpStatus.OK, controller.compensarReservasMasivas("ABC123", "motivo").getStatusCode());

        assertEquals(HttpStatus.OK, controller.getAllReservas(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getReservasPendientes(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getReservasConfirmadas(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getReservasCanceladas(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getReservasFallidas(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getReservasByPlaca("ABC123", pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getReservasByPlacaAndEstado("ABC123", EstadoReserva.PENDIENTE, pageable).getStatusCode());

        assertEquals(HttpStatus.OK, controller.getAllSagas(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getSagasIniciadas(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getSagasEnProgreso(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getSagasCompletadas(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getSagasFallidas(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getSagasCompensadas(pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getSagasByPlaca("ABC123", pageable).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getSagasByPlacaAndEstado("ABC123", EstadoSaga.COMPLETADA, pageable).getStatusCode());
    }

    @Test
    void obtenerReservaNotFoundYSoftDeleteFalse() {
        assertEquals(HttpStatus.NOT_FOUND, controller.obtenerReserva(UUID.randomUUID()).getStatusCode());

        when(vehicleService.softDelete(any())).thenReturn(false);
        assertEquals(HttpStatus.NOT_FOUND, controller.softDelete(UUID.randomUUID()).getStatusCode());
    }

    @Test
    void compensarReservasMasivasSinResultados() {
        when(sagaService.cancelarReservasPorPlaca("ABC123", "motivo")).thenReturn(List.of());
        var response = controller.compensarReservasMasivas("ABC123", "motivo");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("message").toString().contains("No se encontraron"));
    }
}
