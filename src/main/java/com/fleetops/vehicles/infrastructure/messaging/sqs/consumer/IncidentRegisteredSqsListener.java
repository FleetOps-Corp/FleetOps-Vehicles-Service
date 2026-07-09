package com.fleetops.vehicles.infrastructure.messaging.sqs.consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fleetops.vehicles.infrastructure.messaging.sqs.SnsMessageParser;
import com.fleetops.vehicles.infrastructure.messaging.sqs.config.SqsIncidentsProperties;
import com.fleetops.vehicles.services.application.IncidentIntegrationService;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fleetops.sqs.enabled", havingValue = "true")
public class IncidentRegisteredSqsListener {

    private final SnsMessageParser snsMessageParser;
    private final SqsIncidentsProperties sqsIncidentsProperties;
    private final IncidentIntegrationService incidentIntegrationService;

    @SqsListener("${fleetops.sqs.incidents.queue-url}")
    public void onMessage(String sqsBody) {
        log.debug("Mensaje SQS de incidentes recibido");

        SnsMessageParser.ParsedIncidentMessage parsed;
        try {
            parsed = snsMessageParser.parse(sqsBody);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            log.error("Mensaje SQS de incidentes inválido; se descarta: {}", ex.getMessage());
            return;
        }

        String expectedEventType = sqsIncidentsProperties.getEventType();
        if (parsed.eventType() != null
                && !expectedEventType.equalsIgnoreCase(parsed.eventType())) {
            log.info("Evento SQS ignorado: event_type={} (esperado {}).",
                    parsed.eventType(), expectedEventType);
            return;
        }

        incidentIntegrationService.processIncidentRegistered(parsed.payload());
    }
}
