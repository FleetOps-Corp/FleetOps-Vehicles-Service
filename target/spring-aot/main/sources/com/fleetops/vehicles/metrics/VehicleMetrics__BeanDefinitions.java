package com.fleetops.vehicles.metrics;

import com.fleetops.vehicles.repositories.VehicleRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link VehicleMetrics}.
 */
@Generated
public class VehicleMetrics__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'vehicleMetrics'.
   */
  private static BeanInstanceSupplier<VehicleMetrics> getVehicleMetricsInstanceSupplier() {
    return BeanInstanceSupplier.<VehicleMetrics>forConstructor(VehicleRepository.class, MeterRegistry.class)
            .withGenerator((registeredBean, args) -> new VehicleMetrics(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'vehicleMetrics'.
   */
  public static BeanDefinition getVehicleMetricsBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(VehicleMetrics.class);
    beanDefinition.setInstanceSupplier(getVehicleMetricsInstanceSupplier());
    return beanDefinition;
  }
}
