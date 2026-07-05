package com.fleetops.vehicles.services.application;

import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleReleaseEvent;
import com.fleetops.vehicles.dto.response.ReservaResponse;
import com.fleetops.vehicles.dto.response.SagaResponse;
import com.fleetops.vehicles.dto.response.VehicleAssignmentResult;
import com.fleetops.vehicles.dto.response.VehicleReleaseResult;
import com.fleetops.vehicles.models.entities.EstadoReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SagaService {

    ReservaResponse compensarPorReservaId(UUID reservaId, String motivo);

    List<ReservaResponse> cancelarReservasPorPlaca(String placa, String motivo);

    boolean compensarSaga(UUID sagaId, String motivo);

    Optional<ReservaResponse> findReservaById(UUID idReserva);

    Page<ReservaResponse> findAllReservas(Pageable pageable);

    Page<ReservaResponse> findReservasPendientes(Pageable pageable);

    Page<ReservaResponse> findReservasConfirmadas(Pageable pageable);

    Page<ReservaResponse> findReservasFallidas(Pageable pageable);

    Page<ReservaResponse> findReservasCanceladas(Pageable pageable);

    Page<ReservaResponse> findReservasByPlaca(String placa, Pageable pageable);

    Page<ReservaResponse> findReservasByPlacaAndEstado(String placa, EstadoReserva estado, Pageable pageable);

    Page<SagaResponse> findAllSagas(Pageable pageable);

    Page<SagaResponse> findSagasIniciadas(Pageable pageable);

    Page<SagaResponse> findSagasEnProgreso(Pageable pageable);

    Page<SagaResponse> findSagasCompletadas(Pageable pageable);

    Page<SagaResponse> findSagasFallidas(Pageable pageable);

    Page<SagaResponse> findSagasCompensadas(Pageable pageable);

    Page<SagaResponse> findSagasByPlaca(String placa, Pageable pageable);

    Page<SagaResponse> findSagasByPlacaAndEstado(String placa, com.fleetops.vehicles.models.entities.EstadoSaga estado,
            Pageable pageable);

    VehicleAssignmentResult procesarSolicitudAsignacion(VehicleRequestEvent event);

    VehicleReleaseResult procesarLiberacionAsignacion(VehicleReleaseEvent event);
}
