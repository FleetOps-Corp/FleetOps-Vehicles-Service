package com.fleetops.vehicles.validators;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link StateTransitionValidator}.
 */
@Generated
public class StateTransitionValidator__BeanDefinitions {
  /**
   * Get the bean definition for 'stateTransitionValidator'.
   */
  public static BeanDefinition getStateTransitionValidatorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(StateTransitionValidator.class);
    beanDefinition.setInstanceSupplier(StateTransitionValidator::new);
    return beanDefinition;
  }
}
