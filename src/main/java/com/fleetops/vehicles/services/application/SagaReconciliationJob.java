package com.fleetops.vehicles.services.application;

import com.fleetops.vehicles.infrastructure.messaging.VehicleAssignmentCoordinator;
import com.fleetops.vehicles.models.entities.ReservaVehiculo;
import com.fleetops.vehicles.repositories.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reconciliación contrato A+: detecta CONFIRMADA sin ACK de Asignaciones y republica confirmado
 * o compensa tras agotar reintentos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SagaReconciliationJob {

    private final ReservaRepository reservaRepository;
    private final SagaService sagaService;
    private final VehicleAssignmentCoordinator assignmentCoordinator;

    @Value("${fleetops.saga.reconciliacion.gracia-minutos:30}")
    private int graciaMinutos;

    @Value("${fleetops.saga.reconciliacion.compensar-despues-minutos:240}")
    private int compensarDespuesMinutos;

    @Value("${fleetops.saga.reconciliacion.max-reconfirmaciones:3}")
    private int maxReconfirmaciones;

    @Scheduled(fixedDelayString = "${fleetops.saga.reconciliacion.intervalo-ms:900000}")
    public void ejecutar() {
        LocalDateTime ahora = LocalDateTime.now();
        republicarConfirmadosPendientes(ahora.minusMinutes(graciaMinutos));
        compensarSinAck(ahora.minusMinutes(compensarDespuesMinutos));
    }

    private void republicarConfirmadosPendientes(LocalDateTime limite) {
        List<ReservaVehiculo> pendientes = reservaRepository.findConfirmadasSinAckAntesDe(
                limite, maxReconfirmaciones);

        for (ReservaVehiculo reserva : pendientes) {
            try {
                assignmentCoordinator.republicarConfirmado(
                        reserva.getIdAsignacionExt(),
                        reserva.getVehiculo().getIdVehiculo(),
                        reserva.getIdReserva());
            } catch (Exception ex) {
                log.error("Error republicando confirmado para asignación {}",
                        reserva.getIdAsignacionExt(), ex);
            }
        }

        if (!pendientes.isEmpty()) {
            log.info("Reconciliación: {} reserva(s) sin ACK — se republicó confirmado", pendientes.size());
        }
    }

    private void compensarSinAck(LocalDateTime limite) {
        List<ReservaVehiculo> paraCompensar = reservaRepository.findConfirmadasSinAckParaCompensar(
                limite, maxReconfirmaciones);

        for (ReservaVehiculo reserva : paraCompensar) {
            try {
                sagaService.compensarPorReservaId(
                        reserva.getIdReserva(),
                        "Auto-compensación: sin ACK de Asignaciones tras "
                                + maxReconfirmaciones + " reintentos de confirmado");
                log.warn("Auto-compensada reserva {} (asignación {}) por falta de ACK",
                        reserva.getIdReserva(), reserva.getIdAsignacionExt());
            } catch (Exception ex) {
                log.error("Error auto-compensando reserva {}", reserva.getIdReserva(), ex);
            }
        }
    }
}
