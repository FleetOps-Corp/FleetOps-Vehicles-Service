package com.fleetops.vehicles.models.mapper;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link DtoMapperReserva}.
 */
@Generated
public class DtoMapperReserva__BeanDefinitions {
  /**
   * Get the bean definition for 'dtoMapperReserva'.
   */
  public static BeanDefinition getDtoMapperReservaBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DtoMapperReserva.class);
    beanDefinition.setInstanceSupplier(DtoMapperReserva::new);
    return beanDefinition;
  }
}
