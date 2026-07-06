package com.fleetops.vehicles.infrastructure.messaging.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleConfirmedEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleFailedEvent;
import com.fleetops.vehicles.infrastructure.messaging.topics.KafkaTopics;

@Component
@RequiredArgsConstructor
public class VehicleEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishVehicleConfirmed(VehicleConfirmedEvent event) {

        kafkaTemplate.send(
                KafkaTopics.VEHICLE_CONFIRMED,
                event
        );

    }

    public void publishVehicleFailed(VehicleFailedEvent event) {

        kafkaTemplate.send(
                KafkaTopics.VEHICLE_FAILED,
                event
        );

    }

}