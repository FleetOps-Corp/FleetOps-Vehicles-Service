package com.fleetops.vehicles.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Tests unitarios - Resilience4jConfig")
class Resilience4jConfigTest {

    @Test
    @DisplayName("circuitBreakerRegistry expone la configuración esperada del disyuntor")
    void circuitBreakerRegistryConfiguradoCorrectamente() {
        Resilience4jConfig config = new Resilience4jConfig();

        CircuitBreakerRegistry registry = config.circuitBreakerRegistry();
        assertNotNull(registry);

        CircuitBreaker circuitBreaker = registry.circuitBreaker("vehiculos");
        CircuitBreakerConfig cbConfig = circuitBreaker.getCircuitBreakerConfig();

        assertEquals(10, cbConfig.getSlidingWindowSize());
        assertEquals(5, cbConfig.getMinimumNumberOfCalls());
        assertEquals(50.0f, cbConfig.getFailureRateThreshold());
        assertEquals(3, cbConfig.getPermittedNumberOfCallsInHalfOpenState());
    }
}
