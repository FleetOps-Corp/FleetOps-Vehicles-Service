package org.springframework.boot.health.autoconfigure.actuate.endpoint;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link HealthEndpointAutoConfiguration}.
 */
@Generated
public class HealthEndpointAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'healthEndpointAutoConfiguration'.
   */
  public static BeanDefinition getHealthEndpointAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(HealthEndpointAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(HealthEndpointAutoConfiguration::new);
    return beanDefinition;
  }
}
