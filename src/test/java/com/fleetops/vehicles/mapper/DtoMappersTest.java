package com.fleetops.vehicles.mapper;

import com.fleetops.vehicles.models.entities.*;
import com.fleetops.vehicles.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DtoMappersTest {

    private final DtoMapperVehicle vehicleMapper = new DtoMapperVehicle();
    private final DtoMapperTipoVehiculo tipoMapper = new DtoMapperTipoVehiculo();
    private final DtoMapperReserva reservaMapper = new DtoMapperReserva();
    private final DtoMapperSaga sagaMapper = new DtoMapperSaga();
    private final DtoMapperHistorial historialMapper = new DtoMapperHistorial();

    @Test
    void mappersRetornanNullSiEntradaEsNull() {
        assertNull(vehicleMapper.toDto(null));
        assertNull(tipoMapper.toDto(null));
        assertNull(reservaMapper.toDto(null));
        assertNull(sagaMapper.toDto(null));
        assertNull(historialMapper.toDto(null));
    }

    @Test
    void mapeaVehiculoYTipo() {
        Vehiculo v = TestDataFactory.vehiculoDisponible();
        var dto = vehicleMapper.toDto(v);
        assertEquals(v.getNumeroPlaca(), dto.numeroPlaca());
        assertEquals("Furgon", dto.nombreTipoVehiculo());
        assertEquals(1500.0, dto.capacidadCarga());

        TipoVehiculo tipo = TestDataFactory.tipoVehiculo();
        var tipoDto = tipoMapper.toDto(tipo);
        assertEquals(1L, tipoDto.idTipoVehiculo());
        assertEquals("Furgon", tipoDto.nombreTipo());
    }

    @Test
    void mapeaReservaYSaga() {
        Vehiculo v = TestDataFactory.vehiculoDisponible();
        SagaVehiculo saga = TestDataFactory.saga(v, EstadoSaga.EN_PROGRESO);
        ReservaVehiculo reserva = TestDataFactory.reserva(v, EstadoReserva.PENDIENTE);
        reserva.setSagaVehiculo(saga);

        var reservaDto = reservaMapper.toDto(reserva);
        assertEquals(reserva.getIdReserva(), reservaDto.idReserva());
        assertEquals(v.getNumeroPlaca(), reservaDto.numeroPlaca());
        assertEquals(saga.getIdSaga(), reservaDto.idSaga());

        var sagaDto = sagaMapper.toDto(saga);
        assertEquals(saga.getIdSaga(), sagaDto.idSaga());
        assertEquals(v.getNumeroPlaca(), sagaDto.numeroPlaca());
    }

    @Test
    void mapeaHistorial() {
        Vehiculo v = TestDataFactory.vehiculoDisponible();
        HistorialEstadoVehiculo h = HistorialEstadoVehiculo.builder()
                .idHistorial(UUID.randomUUID())
                .vehiculo(v)
                .estadoAnterior(EstadoVehiculo.DISPONIBLE.name())
                .estadoNuevo(EstadoVehiculo.EN_MANTENIMIENTO.name())
                .motivoCambio("test")
                .servicioOrigen("test-svc")
                .registradoEn(LocalDateTime.now())
                .build();

        var dto = historialMapper.toDto(h);
        assertEquals(EstadoVehiculo.EN_MANTENIMIENTO.name(), dto.estadoNuevo());
        assertEquals(v.getNumeroPlaca(), dto.numeroPlaca());
        assertEquals("Furgon", dto.nombreTipo());
    }

    @Test
    void mapeaSinRelacionesAnidadas() {
        Vehiculo v = TestDataFactory.vehiculoDisponible();
        v.setTipoVehiculo(null);
        assertNull(vehicleMapper.toDto(v).nombreTipoVehiculo());

        ReservaVehiculo r = TestDataFactory.reserva(v, EstadoReserva.PENDIENTE);
        r.setVehiculo(null);
        r.setSagaVehiculo(null);
        assertNull(reservaMapper.toDto(r).idVehiculo());
        assertNull(reservaMapper.toDto(r).idSaga());
    }
}
