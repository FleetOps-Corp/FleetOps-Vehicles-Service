package org.springframework.boot.health.autoconfigure.actuate.endpoint;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.health.application.LivenessStateHealthIndicator;
import org.springframework.boot.health.application.ReadinessStateHealthIndicator;
import org.springframework.core.env.Environment;

/**
 * Bean definitions for {@link AvailabilityProbesAutoConfiguration}.
 */
@Generated
public class AvailabilityProbesAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'availabilityProbesAutoConfiguration'.
   */
  public static BeanDefinition getAvailabilityProbesAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(AvailabilityProbesAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(AvailabilityProbesAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'livenessStateHealthIndicator'.
   */
  private static BeanInstanceSupplier<LivenessStateHealthIndicator> getLivenessStateHealthIndicatorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<LivenessStateHealthIndicator>forFactoryMethod(AvailabilityProbesAutoConfiguration.class, "livenessStateHealthIndicator", ApplicationAvailability.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.health.autoconfigure.actuate.endpoint.AvailabilityProbesAutoConfiguration", AvailabilityProbesAutoConfiguration.class).livenessStateHealthIndicator(args.get(0)));
  }

  /**
   * Get the bean definition for 'livenessStateHealthIndicator'.
   */
  public static BeanDefinition getLivenessStateHealthIndicatorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(LivenessStateHealthIndicator.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.health.autoconfigure.actuate.endpoint.AvailabilityProbesAutoConfiguration");
    beanDefinition.setInstanceSupplier(getLivenessStateHealthIndicatorInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'readinessStateHealthIndicator'.
   */
  private static BeanInstanceSupplier<ReadinessStateHealthIndicator> getReadinessStateHealthIndicatorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ReadinessStateHealthIndicator>forFactoryMethod(AvailabilityProbesAutoConfiguration.class, "readinessStateHealthIndicator", ApplicationAvailability.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.health.autoconfigure.actuate.endpoint.AvailabilityProbesAutoConfiguration", AvailabilityProbesAutoConfiguration.class).readinessStateHealthIndicator(args.get(0)));
  }

  /**
   * Get the bean definition for 'readinessStateHealthIndicator'.
   */
  public static BeanDefinition getReadinessStateHealthIndicatorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ReadinessStateHealthIndicator.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.health.autoconfigure.actuate.endpoint.AvailabilityProbesAutoConfiguration");
    beanDefinition.setInstanceSupplier(getReadinessStateHealthIndicatorInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'availabilityProbesHealthEndpointGroupsPostProcessor'.
   */
  private static BeanInstanceSupplier<AvailabilityProbesHealthEndpointGroupsPostProcessor> getAvailabilityProbesHealthEndpointGroupsPostProcessorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<AvailabilityProbesHealthEndpointGroupsPostProcessor>forFactoryMethod(AvailabilityProbesAutoConfiguration.class, "availabilityProbesHealthEndpointGroupsPostProcessor", Environment.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.health.autoconfigure.actuate.endpoint.AvailabilityProbesAutoConfiguration", AvailabilityProbesAutoConfiguration.class).availabilityProbesHealthEndpointGroupsPostProcessor(args.get(0)));
  }

  /**
   * Get the bean definition for 'availabilityProbesHealthEndpointGroupsPostProcessor'.
   */
  public static BeanDefinition getAvailabilityProbesHealthEndpointGroupsPostProcessorBeanDefinition(
      ) {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(AvailabilityProbesHealthEndpointGroupsPostProcessor.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.health.autoconfigure.actuate.endpoint.AvailabilityProbesAutoConfiguration");
    beanDefinition.setInstanceSupplier(getAvailabilityProbesHealthEndpointGroupsPostProcessorInstanceSupplier());
    return beanDefinition;
  }
}
