package com.fleetops.vehicles.infrastructure.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fleetops.vehicles.infrastructure.messaging.VehicleAssignmentCoordinator;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleFailedEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;
import com.fleetops.vehicles.infrastructure.messaging.producer.VehicleEventProducer;
import com.fleetops.vehicles.infrastructure.messaging.topics.KafkaTopics;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleRequestConsumer {

    private final VehicleAssignmentCoordinator assignmentCoordinator;
    private final VehicleEventProducer vehicleEventProducer;

    @KafkaListener(
            topics = KafkaTopics.VEHICLE_REQUEST,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "vehicleRequestKafkaListenerContainerFactory"
    )
    public void receiveVehicleRequest(VehicleRequestEvent event) {
        UUID idAsignacion = event != null ? event.getIdAsignacion() : null;

        log.info("Solicitud de vehículo recibida para la asignación {}", idAsignacion);

        try {
            assignmentCoordinator.procesarSolicitudConPublicacion(event);
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
