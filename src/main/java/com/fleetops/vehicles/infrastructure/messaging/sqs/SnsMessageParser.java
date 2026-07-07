package com.fleetops.vehicles.infrastructure.messaging.sqs;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetops.vehicles.infrastructure.messaging.sqs.dto.IncidentRegisteredEvent;
import com.fleetops.vehicles.infrastructure.messaging.sqs.dto.SnsNotificationEnvelope;

@Component
public class SnsMessageParser {

    private final ObjectMapper objectMapper;

    public SnsMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ParsedIncidentMessage parse(String sqsBody) throws JsonProcessingException {
        SnsNotificationEnvelope envelope = objectMapper.readValue(sqsBody, SnsNotificationEnvelope.class);
        if (envelope.getMessage() == null || envelope.getMessage().isBlank()) {
            throw new IllegalArgumentException("SNS envelope sin campo Message");
        }

        IncidentRegisteredEvent event = objectMapper.readValue(
                envelope.getMessage(), IncidentRegisteredEvent.class);

        String eventType = null;
        if (envelope.getMessageAttributes() != null) {
            SnsNotificationEnvelope.SnsMessageAttribute attribute =
                    envelope.getMessageAttributes().get("event_type");
            if (attribute != null) {
                eventType = attribute.getValue();
            }
        }

        return new ParsedIncidentMessage(eventType, event);
    }

    public record ParsedIncidentMessage(String eventType, IncidentRegisteredEvent payload) {
    }
}
