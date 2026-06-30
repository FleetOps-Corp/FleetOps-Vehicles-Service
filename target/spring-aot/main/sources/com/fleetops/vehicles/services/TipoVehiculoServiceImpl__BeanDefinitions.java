package com.fleetops.vehicles.services;

import com.fleetops.vehicles.repositories.TipoVehiculoRepository;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link TipoVehiculoServiceImpl}.
 */
@Generated
public class TipoVehiculoServiceImpl__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'tipoVehiculoServiceImpl'.
   */
  private static BeanInstanceSupplier<TipoVehiculoServiceImpl> getTipoVehiculoServiceImplInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<TipoVehiculoServiceImpl>forConstructor(TipoVehiculoRepository.class)
            .withGenerator((registeredBean, args) -> new TipoVehiculoServiceImpl(args.get(0)));
  }

  /**
   * Get the bean definition for 'tipoVehiculoServiceImpl'.
   */
  public static BeanDefinition getTipoVehiculoServiceImplBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TipoVehiculoServiceImpl.class);
    beanDefinition.setInstanceSupplier(getTipoVehiculoServiceImplInstanceSupplier());
    return beanDefinition;
  }
}
