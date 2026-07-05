package com.fleetops.vehicles.infrastructure.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fleetops.vehicles.dto.response.VehicleAssignmentResult;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleConfirmedEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleFailedEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;
import com.fleetops.vehicles.infrastructure.messaging.producer.VehicleEventProducer;
import com.fleetops.vehicles.infrastructure.messaging.topics.KafkaTopics;
import com.fleetops.vehicles.services.application.SagaService;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleRequestConsumer {

    private final SagaService sagaService;
    private final VehicleEventProducer vehicleEventProducer;

    @KafkaListener(
            topics = KafkaTopics.VEHICLE_REQUEST,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void receiveVehicleRequest(VehicleRequestEvent event) {
        UUID idAsignacion = event != null ? event.getIdAsignacion() : null;

        log.info("Solicitud de vehículo recibida para la asignación {}", idAsignacion);

        try {
            VehicleAssignmentResult result = sagaService.procesarSolicitudAsignacion(event);

            if (result.isSuccess()) {
                vehicleEventProducer.publishVehicleConfirmed(
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
                vehicleEventProducer.publishVehicleFailed(
                        VehicleFailedEvent.builder()
                                .idAsignacion(result.getIdAsignacion())
                                .motivo(result.getMotivo())
                                .build());

                log.warn("No fue posible asignar vehículo para la asignación {}. Motivo: {}",
                        result.getIdAsignacion(), result.getMotivo());
            }

        } catch (Exception ex) {
            log.error("Error procesando solicitud de asignación {}", idAsignacion, ex);

            if (idAsignacion != null) {
                vehicleEventProducer.publishVehicleFailed(
                        VehicleFailedEvent.builder()
                                .idAsignacion(idAsignacion)
                                .motivo("Error interno al procesar la asignación: " + ex.getMessage())
                                .build());
            }
        }
    }
}
