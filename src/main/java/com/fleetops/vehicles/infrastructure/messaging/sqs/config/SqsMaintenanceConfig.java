package com.fleetops.vehicles.infrastructure.messaging.sqs.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SqsMaintenanceProperties.class)
@ConditionalOnProperty(name = "fleetops.sqs.enabled", havingValue = "true")
public class SqsMaintenanceConfig {
}