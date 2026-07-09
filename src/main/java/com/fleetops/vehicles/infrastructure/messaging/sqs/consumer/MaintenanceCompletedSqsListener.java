package com.fleetops.vehicles.infrastructure.messaging.sqs.consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fleetops.vehicles.infrastructure.messaging.sqs.MaintenanceSnsMessageParser;
import com.fleetops.vehicles.infrastructure.messaging.sqs.config.SqsMaintenanceProperties;
import com.fleetops.vehicles.services.application.MaintenanceIntegrationService;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fleetops.sqs.enabled", havingValue = "true")
public class MaintenanceCompletedSqsListener {

    private final MaintenanceSnsMessageParser snsMessageParser;
    private final SqsMaintenanceProperties properties;
    private final MaintenanceIntegrationService maintenanceIntegrationService;

    @SqsListener("${fleetops.sqs.maintenance.queue-url}")
    public void onMessage(String sqsBody) {

        log.debug("Mensaje SQS de mantenimiento recibido");

        MaintenanceSnsMessageParser.ParsedMaintenanceMessage parsed;

        try {
            parsed = snsMessageParser.parse(sqsBody);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            log.error("Mensaje inválido: {}", ex.getMessage());
            return;
        }

        String eventType = parsed.eventType();

        if (eventType != null
                && !properties.getEventType().equalsIgnoreCase(eventType)
                && !"maintenanceFinished".equalsIgnoreCase(eventType)) {

            log.info(
                    "Evento ignorado {} (esperado {} o maintenanceFinished)",
                    eventType,
                    properties.getEventType());

            return;
        }

        maintenanceIntegrationService.processMaintenanceCompleted(parsed.payload());
    }
}