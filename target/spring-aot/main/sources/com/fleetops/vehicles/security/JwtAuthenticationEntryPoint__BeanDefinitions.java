package com.fleetops.vehicles.security;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link JwtAuthenticationEntryPoint}.
 */
@Generated
public class JwtAuthenticationEntryPoint__BeanDefinitions {
  /**
   * Get the bean definition for 'jwtAuthenticationEntryPoint'.
   */
  public static BeanDefinition getJwtAuthenticationEntryPointBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(JwtAuthenticationEntryPoint.class);
    beanDefinition.setInstanceSupplier(JwtAuthenticationEntryPoint::new);
    return beanDefinition;
  }
}
