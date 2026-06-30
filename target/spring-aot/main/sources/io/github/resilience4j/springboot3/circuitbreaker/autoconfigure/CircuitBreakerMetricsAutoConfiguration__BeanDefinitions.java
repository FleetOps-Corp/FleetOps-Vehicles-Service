package io.github.resilience4j.springboot3.circuitbreaker.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link CircuitBreakerMetricsAutoConfiguration}.
 */
@Generated
public class CircuitBreakerMetricsAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'circuitBreakerMetricsAutoConfiguration'.
   */
  public static BeanDefinition getCircuitBreakerMetricsAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CircuitBreakerMetricsAutoConfiguration.class);
    beanDefinition.setTargetType(CircuitBreakerMetricsAutoConfiguration.class);
    ConfigurationClassUtils.initializeConfigurationClass(CircuitBreakerMetricsAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(CircuitBreakerMetricsAutoConfiguration$$SpringCGLIB$$0::new);
    return beanDefinition;
  }
}
