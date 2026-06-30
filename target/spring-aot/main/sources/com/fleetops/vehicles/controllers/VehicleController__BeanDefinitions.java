package com.fleetops.vehicles.controllers;

import com.fleetops.vehicles.services.SagaService;
import com.fleetops.vehicles.services.TipoVehiculoService;
import com.fleetops.vehicles.services.VehicleService;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link VehicleController}.
 */
@Generated
public class VehicleController__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'vehicleController'.
   */
  private static BeanInstanceSupplier<VehicleController> getVehicleControllerInstanceSupplier() {
    return BeanInstanceSupplier.<VehicleController>forConstructor(VehicleService.class, SagaService.class, TipoVehiculoService.class)
            .withGenerator((registeredBean, args) -> new VehicleController(args.get(0), args.get(1), args.get(2)));
  }

  /**
   * Get the bean definition for 'vehicleController'.
   */
  public static BeanDefinition getVehicleControllerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(VehicleController.class);
    beanDefinition.setInstanceSupplier(getVehicleControllerInstanceSupplier());
    return beanDefinition;
  }
}
