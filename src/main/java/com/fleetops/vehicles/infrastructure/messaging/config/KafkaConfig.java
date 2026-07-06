package com.fleetops.vehicles.infrastructure.messaging.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.fleetops.vehicles.infrastructure.messaging.dto.AssignmentCompletedEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private Map<String, Object> consumerProps() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);

        props.put(
                org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG,
                groupId);

        props.put(
                org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);

        props.put(
                org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest");

        return props;
    }

    @Bean
    ConsumerFactory<String, VehicleRequestEvent> vehicleRequestConsumerFactory() {

        JsonDeserializer<VehicleRequestEvent> deserializer =
                new JsonDeserializer<>(VehicleRequestEvent.class);

        deserializer.addTrustedPackages(
                "com.fleetops.vehicles.infrastructure.messaging.dto");

        return new DefaultKafkaConsumerFactory<>(
                consumerProps(),
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, VehicleRequestEvent>
    vehicleRequestKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, VehicleRequestEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(vehicleRequestConsumerFactory());

        return factory;
    }

    @Bean
    ConsumerFactory<String, AssignmentCompletedEvent> assignmentCompletedConsumerFactory() {

        JsonDeserializer<AssignmentCompletedEvent> deserializer =
                new JsonDeserializer<>(AssignmentCompletedEvent.class);

        deserializer.addTrustedPackages(
                "com.fleetops.vehicles.infrastructure.messaging.dto");

        return new DefaultKafkaConsumerFactory<>(
                consumerProps(),
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, AssignmentCompletedEvent>
    assignmentCompletedKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, AssignmentCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                assignmentCompletedConsumerFactory());

        return factory;
    }
}