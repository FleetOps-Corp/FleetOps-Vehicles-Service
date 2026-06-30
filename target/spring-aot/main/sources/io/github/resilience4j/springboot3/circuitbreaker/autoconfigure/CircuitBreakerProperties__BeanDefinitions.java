package io.github.resilience4j.springboot3.circuitbreaker.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link CircuitBreakerProperties}.
 */
@Generated
public class CircuitBreakerProperties__BeanDefinitions {
  /**
   * Get the bean definition for 'circuitBreakerProperties'.
   */
  public static BeanDefinition getCircuitBreakerPropertiesBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CircuitBreakerProperties.class);
    beanDefinition.setInstanceSupplier(CircuitBreakerProperties::new);
    return beanDefinition;
  }
}
