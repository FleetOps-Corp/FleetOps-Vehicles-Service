package com.fleetops.vehicles.infrastructure.messaging.sqs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class SnsMessageParserTest {

    private final SnsMessageParser parser = new SnsMessageParser(new ObjectMapper());

    @Test
    void parseDesempaquetaSobreSnsYEvento() throws Exception {
        String innerMessage = "{\"incident_id\":\"550e8400-e29b-41d4-a716-446655440099\","
                + "\"vehicle_id\":\"CXT401\",\"incident_type\":\"MECANICO\","
                + "\"severity\":\"GRAVE\",\"description\":\"Falla de motor\"}";
        String sqsBody = "{\"Type\":\"Notification\",\"Message\":"
                + new ObjectMapper().writeValueAsString(innerMessage)
                + ",\"MessageAttributes\":{\"event_type\":{\"Type\":\"String\","
                + "\"Value\":\"incident_registered\"}}}";

        SnsMessageParser.ParsedIncidentMessage parsed = parser.parse(sqsBody);

        assertEquals("incident_registered", parsed.eventType());
        assertNotNull(parsed.payload());
        assertEquals("550e8400-e29b-41d4-a716-446655440099", parsed.payload().getIncidentId());
        assertEquals("CXT401", parsed.payload().getVehiclePlate());
        assertEquals("MECANICO", parsed.payload().getIncidentType());
        assertEquals("GRAVE", parsed.payload().getSeverity());
    }
}
