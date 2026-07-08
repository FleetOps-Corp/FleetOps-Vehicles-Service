package com.fleetops.vehicles.infrastructure.messaging.sqs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fleetops.sqs.maintenance")
public class SqsMaintenanceProperties extends SqsProperties {

    public SqsMaintenanceProperties() {
        setEventType("maintenance_completed");
    }

}