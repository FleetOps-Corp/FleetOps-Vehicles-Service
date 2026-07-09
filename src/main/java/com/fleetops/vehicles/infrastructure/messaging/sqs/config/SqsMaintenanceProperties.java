package com.fleetops.vehicles.infrastructure.messaging.sqs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fleetops.sqs.maintenance")
public class SqsMaintenanceProperties extends SqsProperties {

    private String eventTypeCreated = "maintenance_created";
    private String legacyEventTypeCompleted = "maintenanceFinished";

    public SqsMaintenanceProperties() {
        setEventType("maintenance_completed");
    }

    public String getEventTypeCreated() {
        return eventTypeCreated;
    }

    public void setEventTypeCreated(String eventTypeCreated) {
        this.eventTypeCreated = eventTypeCreated;
    }

    public String getLegacyEventTypeCompleted() {
        return legacyEventTypeCompleted;
    }

    public void setLegacyEventTypeCompleted(String legacyEventTypeCompleted) {
        this.legacyEventTypeCompleted = legacyEventTypeCompleted;
    }
}
