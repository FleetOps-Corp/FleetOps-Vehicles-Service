package com.fleetops.vehicles.infrastructure.messaging.sqs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fleetops.sqs.incidents")
public class SqsIncidentsProperties extends SqsProperties {

    public SqsIncidentsProperties() {
        setEventType("incident_registered");
    }

}
