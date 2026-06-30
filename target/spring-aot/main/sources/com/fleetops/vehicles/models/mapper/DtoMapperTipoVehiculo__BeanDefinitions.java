package com.fleetops.vehicles.models.mapper;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link DtoMapperTipoVehiculo}.
 */
@Generated
public class DtoMapperTipoVehiculo__BeanDefinitions {
  /**
   * Get the bean definition for 'dtoMapperTipoVehiculo'.
   */
  public static BeanDefinition getDtoMapperTipoVehiculoBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DtoMapperTipoVehiculo.class);
    beanDefinition.setInstanceSupplier(DtoMapperTipoVehiculo::new);
    return beanDefinition;
  }
}
