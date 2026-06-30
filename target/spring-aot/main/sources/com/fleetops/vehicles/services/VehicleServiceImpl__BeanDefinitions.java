package com.fleetops.vehicles.services;

import com.fleetops.vehicles.models.mapper.DtoMapperHistorial;
import com.fleetops.vehicles.models.mapper.DtoMapperVehicle;
import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.TipoVehiculoRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import com.fleetops.vehicles.validators.StateTransitionValidator;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link VehicleServiceImpl}.
 */
@Generated
public class VehicleServiceImpl__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'vehicleServiceImpl'.
   */
  private static BeanInstanceSupplier<VehicleServiceImpl> getVehicleServiceImplInstanceSupplier() {
    return BeanInstanceSupplier.<VehicleServiceImpl>forConstructor(VehicleRepository.class, TipoVehiculoRepository.class, HistorialEstadoRepository.class, DtoMapperVehicle.class, DtoMapperHistorial.class, StateTransitionValidator.class)
            .withGenerator((registeredBean, args) -> new VehicleServiceImpl(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5)));
  }

  /**
   * Get the bean definition for 'vehicleServiceImpl'.
   */
  public static BeanDefinition getVehicleServiceImplBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(VehicleServiceImpl.class);
    beanDefinition.setInstanceSupplier(getVehicleServiceImplInstanceSupplier());
    return beanDefinition;
  }
}
