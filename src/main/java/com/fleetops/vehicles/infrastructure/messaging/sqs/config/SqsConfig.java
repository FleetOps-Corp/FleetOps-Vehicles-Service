package com.fleetops.vehicles.infrastructure.messaging.sqs.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
@ConditionalOnProperty(name = "fleetops.sqs.enabled", havingValue = "true")
public class SqsConfig {
    
    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
            SqsAsyncClient sqsAsyncClient) {

        return SqsMessageListenerContainerFactory.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }
}
