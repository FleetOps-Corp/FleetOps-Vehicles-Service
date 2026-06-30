package com.fleetops.vehicles;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link VehiclesApplication}.
 */
@Generated
public class VehiclesApplication__BeanDefinitions {
  /**
   * Get the bean definition for 'vehiclesApplication'.
   */
  public static BeanDefinition getVehiclesApplicationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(VehiclesApplication.class);
    beanDefinition.setInstanceSupplier(VehiclesApplication::new);
    return beanDefinition;
  }
}
