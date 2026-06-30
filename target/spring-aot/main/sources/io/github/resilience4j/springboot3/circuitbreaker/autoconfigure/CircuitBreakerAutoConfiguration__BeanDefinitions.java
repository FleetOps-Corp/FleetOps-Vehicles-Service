package io.github.resilience4j.springboot3.circuitbreaker.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link CircuitBreakerAutoConfiguration}.
 */
@Generated
public class CircuitBreakerAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'circuitBreakerAutoConfiguration'.
   */
  public static BeanDefinition getCircuitBreakerAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CircuitBreakerAutoConfiguration.class);
    beanDefinition.setTargetType(CircuitBreakerAutoConfiguration.class);
    ConfigurationClassUtils.initializeConfigurationClass(CircuitBreakerAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(CircuitBreakerAutoConfiguration$$SpringCGLIB$$0::new);
    return beanDefinition;
  }

  /**
   * Bean definitions for {@link CircuitBreakerAutoConfiguration.CircuitBreakerEndpointAutoConfiguration}.
   */
  @Generated
  public static class CircuitBreakerEndpointAutoConfiguration {
    /**
     * Get the bean definition for 'circuitBreakerEndpointAutoConfiguration'.
     */
    public static BeanDefinition getCircuitBreakerEndpointAutoConfigurationBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(CircuitBreakerAutoConfiguration.CircuitBreakerEndpointAutoConfiguration.class);
      beanDefinition.setTargetType(CircuitBreakerAutoConfiguration.CircuitBreakerEndpointAutoConfiguration.class);
      ConfigurationClassUtils.initializeConfigurationClass(CircuitBreakerAutoConfiguration.CircuitBreakerEndpointAutoConfiguration.class);
      beanDefinition.setInstanceSupplier(CircuitBreakerAutoConfiguration$CircuitBreakerEndpointAutoConfiguration$$SpringCGLIB$$0::new);
      return beanDefinition;
    }
  }
}
