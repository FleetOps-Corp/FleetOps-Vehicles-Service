package com.fleetops.vehicles.infrastructure.messaging.sqs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class MaintenanceSnsMessageParserTest {

    private final MaintenanceSnsMessageParser parser =
            new MaintenanceSnsMessageParser(new ObjectMapper().registerModule(new JavaTimeModule()));

    @Test
    void parseDesempaquetaEventoDeCreacionConInstantUtc() throws Exception {
        UUID maintenanceId = UUID.fromString("9f26d4de-d43b-4d9e-a8d8-cba72b9d96d1");
        UUID vehicleId = UUID.fromString("bc5d79f4-0ef7-43dd-9038-6382d51d58e0");

        String innerMessage = "{\"maintenanceId\":\"" + maintenanceId + "\","
                + "\"vehicleId\":\"" + vehicleId + "\","
                + "\"maintenanceType\":\"CORRECTIVE\","
                + "\"status\":\"CREATED\","
                + "\"occurredAt\":\"2026-07-08T10:30:00Z\"}";
        String sqsBody = buildSqsBody(innerMessage, "maintenance_created");

        MaintenanceSnsMessageParser.ParsedMaintenanceMessage parsed = parser.parse(sqsBody);

        assertEquals("maintenance_created", parsed.eventType());
        assertEquals("CREATED", parsed.payload().status());
        assertEquals(maintenanceId, parsed.payload().maintenanceId());
        assertEquals(vehicleId, parsed.payload().vehicleId());
    }

    @Test
    void parseDesempaquetaSobreSnsYEventoConInstantUtc() throws Exception {
        UUID maintenanceId = UUID.fromString("9f26d4de-d43b-4d9e-a8d8-cba72b9d96d1");
        UUID vehicleId = UUID.fromString("bc5d79f4-0ef7-43dd-9038-6382d51d58e0");

        String innerMessage = "{\"maintenanceId\":\"" + maintenanceId + "\","
                + "\"vehicleId\":\"" + vehicleId + "\","
                + "\"maintenanceType\":\"CORRECTIVE\","
                + "\"status\":\"COMPLETED\","
                + "\"occurredAt\":\"2026-07-08T12:45:00Z\"}";
        String sqsBody = buildSqsBody(innerMessage, "maintenance_completed");

        MaintenanceSnsMessageParser.ParsedMaintenanceMessage parsed = parser.parse(sqsBody);

        assertEquals("maintenance_completed", parsed.eventType());
        assertNotNull(parsed.payload());
        assertEquals(maintenanceId, parsed.payload().maintenanceId());
        assertEquals(vehicleId, parsed.payload().vehicleId());
        assertEquals("CORRECTIVE", parsed.payload().maintenanceType());
        assertEquals("COMPLETED", parsed.payload().status());
        assertEquals(Instant.parse("2026-07-08T12:45:00Z"), parsed.payload().occurredAt());
    }

    private String buildSqsBody(String innerMessage, String eventType) throws Exception {
        return "{\"Type\":\"Notification\",\"Message\":"
                + new ObjectMapper().writeValueAsString(innerMessage)
                + ",\"MessageAttributes\":{\"event_type\":{\"Type\":\"String\","
                + "\"Value\":\"" + eventType + "\"}}}";
    }
}
