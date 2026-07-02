package com.fleetops.vehicles.mapper;

import com.fleetops.vehicles.models.entities.EstadoReserva;
import com.fleetops.vehicles.models.entities.EstadoSaga;
import com.fleetops.vehicles.models.entities.HistorialEstadoVehiculo;
import com.fleetops.vehicles.models.entities.ReservaVehiculo;
import com.fleetops.vehicles.models.entities.SagaVehiculo;
import com.fleetops.vehicles.models.entities.TipoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.dto.response.HistorialEstadoResponse;
import com.fleetops.vehicles.dto.response.ReservaResponse;
import com.fleetops.vehicles.dto.response.SagaResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Tests unitarios - DtoMappers")
class DtoMapperTest {

    private final DtoMapperReserva mapperReserva = new DtoMapperReserva();
    private final DtoMapperSaga mapperSaga = new DtoMapperSaga();
    private final DtoMapperVehicle mapperVehicle = new DtoMapperVehicle();
    private final DtoMapperHistorial mapperHistorial = new DtoMapperHistorial();

    @Test
    @DisplayName("DtoMapperReserva mapea entidad completa")
    void mapperReservaCompleto() {
        UUID reservaId = UUID.randomUUID();
        UUID vehiculoId = UUID.randomUUID();
        UUID sagaId = UUID.randomUUID();

        Vehiculo vehiculo = Vehiculo.builder()
                .idVehiculo(vehiculoId)
                .numeroPlaca("ABC123")
                .kilometraje(50000)
                .tipoVehiculo(TipoVehiculo.builder()
                        .nombreTipo("Camion")
                        .descripcion("Pesado")
                        .capacidadCarga(15000.0)
                        .build())
                .build();

        SagaVehiculo saga = SagaVehiculo.builder().idSaga(sagaId).build();

        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(reservaId)
                .vehiculo(vehiculo)
                .sagaVehiculo(saga)
                .estadoReserva(EstadoReserva.CONFIRMADA)
                .idAsignacionExt(UUID.randomUUID())
                .solicitadoPor("Maria")
                .fechaInicio(LocalDateTime.now().plusDays(1))
                .fechaFin(LocalDateTime.now().plusDays(3))
                .claveIdempotencia("clave-x")
                .build();

        ReservaResponse dto = mapperReserva.toDto(reserva);

        assertEquals(reservaId, dto.idReserva());
        assertEquals("ABC123", dto.numeroPlaca());
        assertEquals("CONFIRMADA", dto.estadoReserva());
        assertEquals(sagaId, dto.idSaga());
        assertEquals(15000.0, dto.capacidadCarga());
    }

    @Test
    @DisplayName("DtoMapperReserva retorna null para entrada null")
    void mapperReservaNull() {
        assertNull(mapperReserva.toDto(null));
    }

    @Test
    @DisplayName("DtoMapperReserva navega de forma segura cuando el vehículo y la saga son nulos")
    void mapperReservaSinVehiculoNiSaga() {
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(UUID.randomUUID())
                .vehiculo(null)
                .sagaVehiculo(null)
                .estadoReserva(null)
                .idAsignacionExt(null)
                .solicitadoPor("Carlos")
                .build();

        ReservaResponse dto = mapperReserva.toDto(reserva);

        assertNull(dto.idVehiculo());
        assertNull(dto.estadoReserva());
        assertNull(dto.idAsignacionExt());
        assertNull(dto.idSaga());
        assertNull(dto.numeroPlaca());
        assertNull(dto.nombreTipo());
        assertNull(dto.capacidadCarga());
    }

    @Test
    @DisplayName("DtoMapperReserva navega de forma segura cuando el vehículo no tiene tipo asignado")
    void mapperReservaSinTipoVehiculo() {
        Vehiculo vehiculo = Vehiculo.builder()
                .idVehiculo(UUID.randomUUID())
                .numeroPlaca("SIN-TIPO")
                .kilometraje(1000)
                .tipoVehiculo(null)
                .build();
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(UUID.randomUUID())
                .vehiculo(vehiculo)
                .estadoReserva(EstadoReserva.PENDIENTE)
                .build();

        ReservaResponse dto = mapperReserva.toDto(reserva);

        assertEquals("SIN-TIPO", dto.numeroPlaca());
        assertNull(dto.nombreTipo());
        assertNull(dto.capacidadCarga());
    }

    @Test
    @DisplayName("DtoMapperSaga mapea entidad completa")
    void mapperSagaCompleto() {
        UUID sagaId = UUID.randomUUID();
        Vehiculo vehiculo = Vehiculo.builder()
                .idVehiculo(UUID.randomUUID())
                .numeroPlaca("XYZ999")
                .build();

        SagaVehiculo saga = SagaVehiculo.builder()
                .idSaga(sagaId)
                .vehiculo(vehiculo)
                .tipoOperacion("RESERVA_VEHICULO")
                .estadoSaga(EstadoSaga.EN_PROGRESO)
                .claveIdempotencia("clave-saga")
                .intentos(1)
                .payload("{}")
                .creadoEn(LocalDateTime.now())
                .build();

        SagaResponse dto = mapperSaga.toDto(saga);

        assertEquals(sagaId, dto.idSaga());
        assertEquals("XYZ999", dto.numeroPlaca());
        assertEquals("EN_PROGRESO", dto.estadoSaga());
    }

    @Test
    @DisplayName("DtoMapperVehicle mapea vehículo con tipo")
    void mapperVehicleCompleto() {
        Vehiculo vehiculo = Vehiculo.builder()
                .idVehiculo(UUID.randomUUID())
                .numeroPlaca("TWA101")
                .marca("Volvo")
                .modelo("FH16")
                .estadoVehiculo(com.fleetops.vehicles.models.entities.EstadoVehiculo.DISPONIBLE)
                .activo(true)
                .tipoVehiculo(TipoVehiculo.builder()
                        .idTipoVehiculo(1L)
                        .nombreTipo("Camion")
                        .build())
                .build();

        var dto = mapperVehicle.toDto(vehiculo);

        assertEquals("TWA101", dto.numeroPlaca());
        assertEquals("DISPONIBLE", dto.estadoVehiculo());
        assertEquals("Camion", dto.nombreTipoVehiculo());
    }

    @Test
    @DisplayName("DtoMapperSaga navega de forma segura cuando el vehículo es nulo")
    void mapperSagaSinVehiculo() {
        SagaVehiculo saga = SagaVehiculo.builder()
                .idSaga(UUID.randomUUID())
                .vehiculo(null)
                .tipoOperacion("RESERVA_VEHICULO")
                .estadoSaga(EstadoSaga.INICIADA)
                .build();

        SagaResponse dto = mapperSaga.toDto(saga);

        assertNull(dto.idVehiculo());
        assertNull(dto.numeroPlaca());
    }

    @Test
    @DisplayName("DtoMapperVehicle navega de forma segura cuando no hay tipo ni estado")
    void mapperVehicleSinTipoNiEstado() {
        Vehiculo vehiculo = Vehiculo.builder()
                .idVehiculo(UUID.randomUUID())
                .numeroPlaca("SIN-TIPO2")
                .estadoVehiculo(null)
                .tipoVehiculo(null)
                .build();

        var dto = mapperVehicle.toDto(vehiculo);

        assertNull(dto.estadoVehiculo());
        assertNull(dto.nombreTipoVehiculo());
        assertNull(dto.capacidadCarga());
        assertNull(dto.descripcionTipo());
    }

    @Test
    @DisplayName("DtoMapperHistorial mapea entidad completa con vehículo y tipo")
    void mapperHistorialCompleto() {
        UUID vehiculoId = UUID.randomUUID();
        Vehiculo vehiculo = Vehiculo.builder()
                .idVehiculo(vehiculoId)
                .numeroPlaca("HIS123")
                .kilometraje(30000)
                .tipoVehiculo(TipoVehiculo.builder()
                        .nombreTipo("Furgon")
                        .descripcion("Refrigerado")
                        .capacidadCarga(8000.0)
                        .build())
                .build();

        HistorialEstadoVehiculo historial = HistorialEstadoVehiculo.builder()
                .idHistorial(UUID.randomUUID())
                .vehiculo(vehiculo)
                .estadoAnterior("DISPONIBLE")
                .estadoNuevo("EN_MANTENIMIENTO")
                .motivoCambio("Revisión")
                .servicioOrigen("fleetops-vehicles")
                .registradoEn(LocalDateTime.now())
                .build();

        HistorialEstadoResponse dto = mapperHistorial.toDto(historial);

        assertEquals(vehiculoId, dto.idVehiculo());
        assertEquals("HIS123", dto.numeroPlaca());
        assertEquals("Furgon", dto.nombreTipo());
        assertEquals(8000.0, dto.capacidadCarga());
        assertEquals("EN_MANTENIMIENTO", dto.estadoNuevo());
    }

    @Test
    @DisplayName("DtoMapperHistorial retorna null para entrada null")
    void mapperHistorialNull() {
        assertNull(mapperHistorial.toDto(null));
    }

    @Test
    @DisplayName("DtoMapperHistorial navega de forma segura cuando el vehículo es nulo")
    void mapperHistorialSinVehiculo() {
        HistorialEstadoVehiculo historial = HistorialEstadoVehiculo.builder()
                .idHistorial(UUID.randomUUID())
                .vehiculo(null)
                .estadoAnterior(null)
                .estadoNuevo("DISPONIBLE")
                .build();

        HistorialEstadoResponse dto = mapperHistorial.toDto(historial);

        assertNull(dto.idVehiculo());
        assertNull(dto.numeroPlaca());
        assertNull(dto.nombreTipo());
        assertNull(dto.capacidadCarga());
    }

    @Test
    @DisplayName("DtoMapperTipoVehiculo mapea tipo")
    void mapperTipoVehiculo() {
        var mapper = new com.fleetops.vehicles.mapper.DtoMapperTipoVehiculo();
        TipoVehiculo tipo = TipoVehiculo.builder()
                .idTipoVehiculo(2L)
                .nombreTipo("Furgon")
                .descripcion("Refrigerado")
                .capacidadCarga(5000.0)
                .creadoEn(LocalDateTime.now())
                .build();

        var dto = mapper.toDto(tipo);
        assertEquals("Furgon", dto.nombreTipo());
        assertNull(mapper.toDto(null));
    }
}
