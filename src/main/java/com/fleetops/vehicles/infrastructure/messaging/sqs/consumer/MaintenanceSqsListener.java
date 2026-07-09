package com.fleetops.vehicles.infrastructure.messaging.sqs.consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fleetops.vehicles.infrastructure.messaging.sqs.MaintenanceSnsMessageParser;
import com.fleetops.vehicles.infrastructure.messaging.sqs.config.SqsMaintenanceProperties;
import com.fleetops.vehicles.infrastructure.messaging.sqs.dto.MaintenanceEvent;
import com.fleetops.vehicles.services.application.MaintenanceIntegrationService;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fleetops.sqs.enabled", havingValue = "true")
public class MaintenanceSqsListener {

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
            log.error("Mensaje SQS de mantenimiento inválido; se descarta: {}", ex.getMessage());
            return;
        }

        MaintenanceEvent event = parsed.payload();
        String snsEventType = parsed.eventType();

        if (isCreatedEvent(snsEventType, event)) {
            maintenanceIntegrationService.processMaintenanceCreated(event);
            return;
        }

        if (isCompletedEvent(snsEventType, event)) {
            maintenanceIntegrationService.processMaintenanceCompleted(event);
            return;
        }

        log.info("Evento SQS de mantenimiento ignorado: event_type={} status={} (esperado created={} o completed={}/{})",
                snsEventType,
                event != null ? event.status() : null,
                properties.getEventTypeCreated(),
                properties.getEventType(),
                properties.getLegacyEventTypeCompleted());
    }

    private boolean isCreatedEvent(String snsEventType, MaintenanceEvent event) {
        if (snsEventType != null) {
            String expected = properties.getEventTypeCreated();
            return expected != null && expected.equalsIgnoreCase(snsEventType);
        }
        return isStatus(event, "CREATED");
    }

    private boolean isCompletedEvent(String snsEventType, MaintenanceEvent event) {
        if (snsEventType != null) {
            String completed = properties.getEventType();
            String legacy = properties.getLegacyEventTypeCompleted();
            return (completed != null && completed.equalsIgnoreCase(snsEventType))
                    || (legacy != null && legacy.equalsIgnoreCase(snsEventType));
        }
        return isStatus(event, "COMPLETED");
    }

    private boolean isStatus(MaintenanceEvent event, String expected) {
        return event != null
                && event.status() != null
                && expected.equalsIgnoreCase(event.status().trim());
    }
}
