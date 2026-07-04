package com.fleetops.vehicles.support;

import com.fleetops.vehicles.dto.request.ReservaRequest;
import com.fleetops.vehicles.dto.request.TipoVehiculoRequest;
import com.fleetops.vehicles.dto.request.VehicleRequest;
import com.fleetops.vehicles.dto.request.VehicleUpdateRequest;
import com.fleetops.vehicles.models.entities.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static TipoVehiculo tipoVehiculo() {
        TipoVehiculo tipo = new TipoVehiculo();
        tipo.setIdTipoVehiculo(1L);
        tipo.setNombreTipo("Furgon");
        tipo.setDescripcion("Carga liviana");
        tipo.setCapacidadCarga(1500.0);
        tipo.setCreadoEn(LocalDateTime.now());
        return tipo;
    }

    public static Vehiculo vehiculoDisponible() {
        Vehiculo v = new Vehiculo();
        v.setIdVehiculo(UUID.randomUUID());
        v.setNumeroPlaca("ABC123");
        v.setMarca("Chevrolet");
        v.setModelo("NPR");
        v.setAnioFabricacion(2020);
        v.setColor("Blanco");
        v.setNumeroChasis("CHASIS001");
        v.setNumeroMotor("MOTOR001");
        v.setKilometraje(10000);
        v.setCiudadOperacion("Cali");
        v.setSedeOperacion("Patio Norte");
        v.setEstadoVehiculo(EstadoVehiculo.DISPONIBLE);
        v.setFechaSoat(LocalDate.now().plusMonths(6));
        v.setFechaRtm(LocalDate.now().plusMonths(6));
        v.setFechaUltimoMant(LocalDate.now().minusMonths(1));
        v.setActivo(true);
        v.setCreadoEn(LocalDateTime.now());
        v.setTipoVehiculo(tipoVehiculo());
        return v;
    }

    public static VehicleRequest vehicleRequest() {
        LocalDate docs = LocalDate.now().plusMonths(6);
        return new VehicleRequest(
                1L, "ABC123", "Chevrolet", "NPR", 2020, "Blanco",
                "CHASIS001", "MOTOR001", 10000, "Cali", "Patio Norte",
                "DISPONIBLE", docs, docs, LocalDate.now().minusMonths(1));
    }

    public static VehicleUpdateRequest vehicleUpdateRequest(Long tipoId) {
        LocalDate docs = LocalDate.now().plusMonths(6);
        return new VehicleUpdateRequest(
                tipoId, "ABC123", "Chevrolet", "NPR", 2020, "Blanco",
                "CHASIS001", "MOTOR001", 12000, "Cali", "Patio Norte",
                "DISPONIBLE", docs, docs, LocalDate.now().minusMonths(1));
    }

    public static TipoVehiculoRequest tipoRequest() {
        return new TipoVehiculoRequest("Furgon", "Carga liviana", 1500.0);
    }

    public static ReservaRequest reservaRequest() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        return new ReservaRequest(
                UUID.randomUUID().toString(),
                "operador@fleetops.com",
                inicio,
                inicio.plusDays(2),
                "idem-" + UUID.randomUUID());
    }

    public static ReservaVehiculo reserva(Vehiculo vehiculo, EstadoReserva estado) {
        ReservaVehiculo r = new ReservaVehiculo();
        r.setIdReserva(UUID.randomUUID());
        r.setVehiculo(vehiculo);
        r.setEstadoReserva(estado);
        r.setIdAsignacionExt(UUID.randomUUID());
        r.setClaveIdempotencia("idem-" + UUID.randomUUID());
        r.setSolicitadoPor("operador");
        r.setFechaInicio(LocalDateTime.now().minusHours(1));
        r.setFechaFin(LocalDateTime.now().plusHours(2));
        r.setCreadoEn(LocalDateTime.now().minusMinutes(1));
        return r;
    }

    public static SagaVehiculo saga(Vehiculo vehiculo, EstadoSaga estado) {
        SagaVehiculo s = new SagaVehiculo();
        s.setIdSaga(UUID.randomUUID());
        s.setVehiculo(vehiculo);
        s.setTipoOperacion("RESERVA_VEHICULO");
        s.setEstadoSaga(estado);
        s.setClaveIdempotencia("saga-" + UUID.randomUUID());
        s.setIntentos(1);
        s.setCreadoEn(LocalDateTime.now());
        s.setActualizadoEn(LocalDateTime.now());
        return s;
    }
}
