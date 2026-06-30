package com.fleetops.vehicles.models.mapper;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link DtoMapperHistorial}.
 */
@Generated
public class DtoMapperHistorial__BeanDefinitions {
  /**
   * Get the bean definition for 'dtoMapperHistorial'.
   */
  public static BeanDefinition getDtoMapperHistorialBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DtoMapperHistorial.class);
    beanDefinition.setInstanceSupplier(DtoMapperHistorial::new);
    return beanDefinition;
  }
}
