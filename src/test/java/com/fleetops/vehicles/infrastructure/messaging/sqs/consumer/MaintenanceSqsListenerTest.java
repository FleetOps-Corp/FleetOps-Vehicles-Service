package com.fleetops.vehicles.infrastructure.messaging.sqs.consumer;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fleetops.vehicles.infrastructure.messaging.sqs.MaintenanceSnsMessageParser;
import com.fleetops.vehicles.infrastructure.messaging.sqs.config.SqsMaintenanceProperties;
import com.fleetops.vehicles.infrastructure.messaging.sqs.dto.MaintenanceEvent;
import com.fleetops.vehicles.services.application.MaintenanceIntegrationService;

@ExtendWith(MockitoExtension.class)
class MaintenanceSqsListenerTest {

    @Mock
    private MaintenanceSnsMessageParser snsMessageParser;

    @Mock
    private SqsMaintenanceProperties properties;

    @Mock
    private MaintenanceIntegrationService maintenanceIntegrationService;

    @InjectMocks
    private MaintenanceSqsListener listener;

    private MaintenanceEvent createdEvent() {
        return new MaintenanceEvent(
                UUID.fromString("9f26d4de-d43b-4d9e-a8d8-cba72b9d96d1"),
                UUID.fromString("bc5d79f4-0ef7-43dd-9038-6382d51d58e0"),
                "CORRECTIVE",
                "CREATED",
                Instant.parse("2026-07-08T10:30:00Z"));
    }

    private MaintenanceEvent completedEvent() {
        return new MaintenanceEvent(
                UUID.fromString("9f26d4de-d43b-4d9e-a8d8-cba72b9d96d1"),
                UUID.fromString("bc5d79f4-0ef7-43dd-9038-6382d51d58e0"),
                "CORRECTIVE",
                "COMPLETED",
                Instant.parse("2026-07-08T12:45:00Z"));
    }

    @Test
    void enrutaCreacionPorEventTypeSns() throws Exception {
        MaintenanceEvent event = createdEvent();
        when(properties.getEventTypeCreated()).thenReturn("maintenance_created");
        when(snsMessageParser.parse("body")).thenReturn(
                new MaintenanceSnsMessageParser.ParsedMaintenanceMessage("maintenance_created", event));

        listener.onMessage("body");

        verify(maintenanceIntegrationService).processMaintenanceCreated(event);
        verify(maintenanceIntegrationService, never()).processMaintenanceCompleted(event);
    }

    @Test
    void enrutaFinalizacionPorEventTypeSns() throws Exception {
        MaintenanceEvent event = completedEvent();
        when(properties.getEventType()).thenReturn("maintenance_completed");
        when(properties.getLegacyEventTypeCompleted()).thenReturn("maintenanceFinished");
        when(snsMessageParser.parse("body")).thenReturn(
                new MaintenanceSnsMessageParser.ParsedMaintenanceMessage("maintenance_completed", event));

        listener.onMessage("body");

        verify(maintenanceIntegrationService).processMaintenanceCompleted(event);
        verify(maintenanceIntegrationService, never()).processMaintenanceCreated(event);
    }

    @Test
    void enrutaFinalizacionPorLegacyMaintenanceFinished() throws Exception {
        MaintenanceEvent event = completedEvent();
        when(properties.getEventType()).thenReturn("maintenance_completed");
        when(properties.getLegacyEventTypeCompleted()).thenReturn("maintenanceFinished");
        when(snsMessageParser.parse("body")).thenReturn(
                new MaintenanceSnsMessageParser.ParsedMaintenanceMessage("maintenanceFinished", event));

        listener.onMessage("body");

        verify(maintenanceIntegrationService).processMaintenanceCompleted(event);
    }

    @Test
    void enrutaCreacionPorStatusCuandoNoHayEventType() throws Exception {
        MaintenanceEvent event = createdEvent();
        when(snsMessageParser.parse("body")).thenReturn(
                new MaintenanceSnsMessageParser.ParsedMaintenanceMessage(null, event));

        listener.onMessage("body");

        verify(maintenanceIntegrationService).processMaintenanceCreated(event);
    }

    @Test
    void enrutaFinalizacionPorStatusCuandoNoHayEventType() throws Exception {
        MaintenanceEvent event = completedEvent();
        when(snsMessageParser.parse("body")).thenReturn(
                new MaintenanceSnsMessageParser.ParsedMaintenanceMessage(null, event));

        listener.onMessage("body");

        verify(maintenanceIntegrationService).processMaintenanceCompleted(event);
    }

    @Test
    void ignoraEventoConEventTypeDesconocido() throws Exception {
        MaintenanceEvent event = createdEvent();
        when(properties.getEventTypeCreated()).thenReturn("maintenance_created");
        when(properties.getEventType()).thenReturn("maintenance_completed");
        when(properties.getLegacyEventTypeCompleted()).thenReturn("maintenanceFinished");
        when(snsMessageParser.parse("body")).thenReturn(
                new MaintenanceSnsMessageParser.ParsedMaintenanceMessage("otro_evento", event));

        listener.onMessage("body");

        verify(maintenanceIntegrationService, never()).processMaintenanceCreated(event);
        verify(maintenanceIntegrationService, never()).processMaintenanceCompleted(event);
    }

    @Test
    void descartaMensajeInvalidoSinInvocarServicio() throws Exception {
        when(snsMessageParser.parse("malformado"))
                .thenThrow(new JsonProcessingException("JSON inválido") { });

        listener.onMessage("malformado");

        verify(maintenanceIntegrationService, never()).processMaintenanceCreated(org.mockito.ArgumentMatchers.any());
        verify(maintenanceIntegrationService, never()).processMaintenanceCompleted(org.mockito.ArgumentMatchers.any());
    }
}
