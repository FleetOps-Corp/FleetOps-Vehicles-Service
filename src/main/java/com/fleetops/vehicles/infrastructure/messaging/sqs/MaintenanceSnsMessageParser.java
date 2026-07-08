package com.fleetops.vehicles.infrastructure.messaging.sqs;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetops.vehicles.infrastructure.messaging.sqs.dto.MaintenanceEvent;
import com.fleetops.vehicles.infrastructure.messaging.sqs.dto.SnsNotificationEnvelope;

@Component
public class MaintenanceSnsMessageParser {

    private final ObjectMapper objectMapper;

    public MaintenanceSnsMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ParsedMaintenanceMessage parse(String sqsBody) throws JsonProcessingException {

        SnsNotificationEnvelope envelope =
                objectMapper.readValue(sqsBody, SnsNotificationEnvelope.class);

        if (envelope.getMessage() == null || envelope.getMessage().isBlank()) {
            throw new IllegalArgumentException("SNS envelope sin campo Message");
        }

        MaintenanceEvent event = objectMapper.readValue(
                envelope.getMessage(), MaintenanceEvent.class);

        String eventType = null;
        if (envelope.getMessageAttributes() != null) {
            SnsNotificationEnvelope.SnsMessageAttribute attribute =
                    envelope.getMessageAttributes().get("event_type");
            if (attribute != null) {
                eventType = attribute.getValue();
            }
        }

        return new ParsedMaintenanceMessage(eventType, event);
    }

    public record ParsedMaintenanceMessage(
            String eventType,
            MaintenanceEvent payload) {
    }
}