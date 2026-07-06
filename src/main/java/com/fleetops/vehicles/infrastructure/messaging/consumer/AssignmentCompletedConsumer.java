package com.fleetops.vehicles.infrastructure.messaging.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fleetops.vehicles.infrastructure.messaging.dto.AssignmentCompletedEvent;
import com.fleetops.vehicles.infrastructure.messaging.topics.KafkaTopics;
import com.fleetops.vehicles.services.application.SagaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentCompletedConsumer {

    private final SagaService sagaService;

    @KafkaListener(
        topics = KafkaTopics.ASSIGNMENT_COMPLETED,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "assignmentCompletedKafkaListenerContainerFactory"
    )
    public void receive(AssignmentCompletedEvent event) {

        log.info(
            "Asignación {} completada. Confirmando reserva.",
            event.getIdAsignacion()
        );

        sagaService.confirmarReservaPorAsignacion(
                event.getIdAsignacion());

    }

}
