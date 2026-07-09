package com.fleetops.vehicles.infrastructure.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fleetops.vehicles.dto.response.VehicleReleaseResult;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleReleaseEvent;
import com.fleetops.vehicles.infrastructure.messaging.topics.KafkaTopics;
import com.fleetops.vehicles.services.application.SagaService;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleReleaseConsumer {

    private final SagaService sagaService;

    @KafkaListener(
            topics = KafkaTopics.VEHICLE_RELEASE,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "vehicleReleaseKafkaListenerContainerFactory"
    )
    public void receiveVehicleRelease(VehicleReleaseEvent event) {
        log.info("Solicitud de liberación recibida para asignación {} / saga {}",
                event != null ? event.getIdAsignacion() : null,
                event != null ? event.getIdSaga() : null);

        try {
            VehicleReleaseResult result = sagaService.procesarLiberacionAsignacion(event);

            if (result.isProcessed() && result.isIdempotentReplay()) {
                log.info("Liberación idempotente para asignación {} (reserva {})",
                        result.getIdAsignacion(), result.getIdReserva());
            } else if (result.isProcessed()) {
                log.info("Liberación procesada para asignación {} (reserva {})",
                        result.getIdAsignacion(), result.getIdReserva());
            } else {
                log.warn("Liberación no procesada: {}", result.getMotivo());
            }
        } catch (Exception ex) {
            log.error("Error procesando liberación de asignación {}",
                    event != null ? event.getIdAsignacion() : null, ex);
            throw ex;
        }
    }
}
