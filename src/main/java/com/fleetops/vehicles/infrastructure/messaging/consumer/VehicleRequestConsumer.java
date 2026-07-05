package com.fleetops.vehicles.infrastructure.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;
import com.fleetops.vehicles.infrastructure.messaging.topics.KafkaTopics;
import com.fleetops.vehicles.services.application.SagaService;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleRequestConsumer {

    private final SagaService sagaService;

    @KafkaListener(
            topics = KafkaTopics.VEHICLE_REQUEST,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void receiveVehicleRequest(VehicleRequestEvent event){

        log.info(
                "Solicitud de vehículo recibida para la asignación {}",
                event.getIdAsignacion()
        );

        // Se implementará en el PR #2
        sagaService.procesarSolicitudAsignacion(event);

    }

}