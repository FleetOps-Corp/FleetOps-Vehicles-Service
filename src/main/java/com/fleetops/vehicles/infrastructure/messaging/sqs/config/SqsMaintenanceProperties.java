package com.fleetops.vehicles.infrastructure.messaging.sqs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fleetops.sqs.maintenance")
public class SqsMaintenanceProperties {

    private String queueUrl;

    private String eventType = "maintenance_completed";

    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}