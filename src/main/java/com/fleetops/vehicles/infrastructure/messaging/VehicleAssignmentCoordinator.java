package com.fleetops.vehicles.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fleetops.vehicles.dto.response.VehicleAssignmentResult;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleConfirmedEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleFailedEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;
import com.fleetops.vehicles.infrastructure.messaging.topics.KafkaTopics;
import com.fleetops.vehicles.services.application.SagaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Coordina persistencia y publicación Kafka en una sola transacción encadenada (contrato A+).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleAssignmentCoordinator {

    private final SagaService sagaService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional("chainedKafkaTransactionManager")
    public VehicleAssignmentResult procesarSolicitudConPublicacion(VehicleRequestEvent event) {
        VehicleAssignmentResult result = sagaService.procesarSolicitudAsignacion(event);

        if (result.isSuccess()) {
            kafkaTemplate.send(
                    KafkaTopics.VEHICLE_CONFIRMED,
                    VehicleConfirmedEvent.builder()
                            .idAsignacion(result.getIdAsignacion())
                            .idVehiculo(result.getIdVehiculo())
                            .build());

            if (result.isIdempotentReplay()) {
                log.info("Reintento idempotente: republicado confirmado para asignación {}",
                        result.getIdAsignacion());
            } else {
                log.info("Vehículo {} confirmado para la asignación {}",
                        result.getIdVehiculo(), result.getIdAsignacion());
            }
        } else {
            kafkaTemplate.send(
                    KafkaTopics.VEHICLE_FAILED,
                    VehicleFailedEvent.builder()
                            .idAsignacion(result.getIdAsignacion())
                            .motivo(result.getMotivo())
                            .build());
            log.warn("No fue posible asignar vehículo para la asignación {}. Motivo: {}",
                    result.getIdAsignacion(), result.getMotivo());
        }

        return result;
    }

    @Transactional("chainedKafkaTransactionManager")
    public void republicarConfirmado(java.util.UUID idAsignacion, java.util.UUID idVehiculo, java.util.UUID idReserva) {
        kafkaTemplate.send(
                KafkaTopics.VEHICLE_CONFIRMED,
                VehicleConfirmedEvent.builder()
                        .idAsignacion(idAsignacion)
                        .idVehiculo(idVehiculo)
                        .build());
        sagaService.incrementarReconfirmacion(idReserva);
        log.warn("Reconciliación: republicado confirmado para asignación {}", idAsignacion);
    }
}
