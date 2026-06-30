package com.fleetops.vehicles.validators;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link AvailabilityPolicy}.
 */
@Generated
public class AvailabilityPolicy__BeanDefinitions {
  /**
   * Get the bean definition for 'availabilityPolicy'.
   */
  public static BeanDefinition getAvailabilityPolicyBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(AvailabilityPolicy.class);
    beanDefinition.setInstanceSupplier(AvailabilityPolicy::new);
    return beanDefinition;
  }
}
