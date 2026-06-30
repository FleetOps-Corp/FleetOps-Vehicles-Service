package com.fleetops.vehicles.models.mapper;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link DtoMapperVehicle}.
 */
@Generated
public class DtoMapperVehicle__BeanDefinitions {
  /**
   * Get the bean definition for 'dtoMapperVehicle'.
   */
  public static BeanDefinition getDtoMapperVehicleBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DtoMapperVehicle.class);
    beanDefinition.setInstanceSupplier(DtoMapperVehicle::new);
    return beanDefinition;
  }
}
