package com.fleetops.vehicles.infrastructure.messaging.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

import com.fleetops.vehicles.infrastructure.messaging.dto.AssignmentCompletedEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleReleaseEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${fleetops.kafka.producer.transaction-id-prefix:vehicles-tx-}")
    private String transactionIdPrefix;

    private Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    // ── Producer transaccional (atomicidad DB + Kafka, contrato A+) ───────────

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionIdPrefix);

        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(config);
        factory.setTransactionIdPrefix(transactionIdPrefix);
        return factory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean(name = "chainedKafkaTransactionManager")
    public PlatformTransactionManager chainedKafkaTransactionManager(
            @Qualifier("transactionManager") PlatformTransactionManager jpaTransactionManager,
            ProducerFactory<String, Object> producerFactory) {
        KafkaTransactionManager kafkaTransactionManager = new KafkaTransactionManager<>(producerFactory);
        return new ChainedKafkaTransactionManager<>(jpaTransactionManager, kafkaTransactionManager);
    }

    // ── Consumers ─────────────────────────────────────────────────────────────

    @Bean
    ConsumerFactory<String, VehicleRequestEvent> vehicleRequestConsumerFactory() {

        JsonDeserializer<VehicleRequestEvent> deserializer =
                new JsonDeserializer<>(VehicleRequestEvent.class);

        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages("com.fleetops.vehicles.infrastructure.messaging.dto");

        return new DefaultKafkaConsumerFactory<>(
                consumerProps(), new StringDeserializer(), deserializer);
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
        
        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages("com.fleetops.vehicles.infrastructure.messaging.dto");

        return new DefaultKafkaConsumerFactory<>(
                consumerProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, AssignmentCompletedEvent>
    assignmentCompletedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AssignmentCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(assignmentCompletedConsumerFactory());
        return factory;
    }

    @Bean
    ConsumerFactory<String, VehicleReleaseEvent> vehicleReleaseConsumerFactory() {
        JsonDeserializer<VehicleReleaseEvent> deserializer =
                new JsonDeserializer<>(VehicleReleaseEvent.class);

        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages("com.fleetops.vehicles.infrastructure.messaging.dto");
        return new DefaultKafkaConsumerFactory<>(
                consumerProps(), new StringDeserializer(), deserializer);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, VehicleReleaseEvent>
    vehicleReleaseKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, VehicleReleaseEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(vehicleReleaseConsumerFactory());
        return factory;
    }
}
