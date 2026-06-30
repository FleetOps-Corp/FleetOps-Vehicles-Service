package com.fleetops.vehicles.security;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link JwtValidationFilter}.
 */
@Generated
public class JwtValidationFilter__BeanDefinitions {
  /**
   * Get the bean definition for 'jwtValidationFilter'.
   */
  public static BeanDefinition getJwtValidationFilterBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(JwtValidationFilter.class);
    beanDefinition.setInstanceSupplier(JwtValidationFilter::new);
    return beanDefinition;
  }
}
