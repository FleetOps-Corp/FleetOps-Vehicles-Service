package com.fleetops.vehicles.services;

import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.repositories.SagaRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import com.fleetops.vehicles.validators.AvailabilityPolicy;
import com.fleetops.vehicles.validators.IdempotencyValidator;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link SagaServiceImpl}.
 */
@Generated
public class SagaServiceImpl__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'sagaServiceImpl'.
   */
  private static BeanInstanceSupplier<SagaServiceImpl> getSagaServiceImplInstanceSupplier() {
    return BeanInstanceSupplier.<SagaServiceImpl>forConstructor(VehicleRepository.class, ReservaRepository.class, SagaRepository.class, HistorialEstadoRepository.class, AvailabilityPolicy.class, IdempotencyValidator.class)
            .withGenerator((registeredBean, args) -> new SagaServiceImpl(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5)));
  }

  /**
   * Get the bean definition for 'sagaServiceImpl'.
   */
  public static BeanDefinition getSagaServiceImplBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SagaServiceImpl.class);
    beanDefinition.setInstanceSupplier(getSagaServiceImplInstanceSupplier());
    return beanDefinition;
  }
}
