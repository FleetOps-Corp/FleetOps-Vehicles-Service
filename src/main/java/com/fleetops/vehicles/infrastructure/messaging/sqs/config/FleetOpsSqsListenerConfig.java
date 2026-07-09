package com.fleetops.vehicles.infrastructure.messaging.sqs.config;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.QueueNotFoundStrategy;
import io.awspring.cloud.sqs.support.converter.MessagingMessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

/**
 * Evita NPE en SqsAutoConfiguration (awspring 4.0.2 + Boot 3.3) cuando las
 * propiedades del listener no están definidas y queueNotFoundStrategy es null.
 */
@Configuration
@ConditionalOnProperty(name = "fleetops.sqs.enabled", havingValue = "true")
public class FleetOpsSqsListenerConfig {

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
            SqsAsyncClient sqsAsyncClient,
            ObjectProvider<MessagingMessageConverter> messageConverter) {
        SqsMessageListenerContainerFactory<Object> factory = new SqsMessageListenerContainerFactory<>();
        factory.setSqsAsyncClient(sqsAsyncClient);
        factory.configure(options -> {
            options.queueNotFoundStrategy(QueueNotFoundStrategy.CREATE);
            messageConverter.ifAvailable(options::messageConverter);
        });
        return factory;
    }
}
