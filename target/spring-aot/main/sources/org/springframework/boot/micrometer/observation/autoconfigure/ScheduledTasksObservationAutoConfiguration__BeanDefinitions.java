package org.springframework.boot.micrometer.observation.autoconfigure;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link ScheduledTasksObservationAutoConfiguration}.
 */
@Generated
public class ScheduledTasksObservationAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'scheduledTasksObservationAutoConfiguration'.
   */
  public static BeanDefinition getScheduledTasksObservationAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ScheduledTasksObservationAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(ScheduledTasksObservationAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'observabilitySchedulingConfigurer'.
   */
  private static BeanInstanceSupplier<ScheduledTasksObservationAutoConfiguration.ObservabilitySchedulingConfigurer> getObservabilitySchedulingConfigurerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ScheduledTasksObservationAutoConfiguration.ObservabilitySchedulingConfigurer>forFactoryMethod(ScheduledTasksObservationAutoConfiguration.class, "observabilitySchedulingConfigurer", ObservationRegistry.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.observation.autoconfigure.ScheduledTasksObservationAutoConfiguration", ScheduledTasksObservationAutoConfiguration.class).observabilitySchedulingConfigurer(args.get(0)));
  }

  /**
   * Get the bean definition for 'observabilitySchedulingConfigurer'.
   */
  public static BeanDefinition getObservabilitySchedulingConfigurerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ScheduledTasksObservationAutoConfiguration.ObservabilitySchedulingConfigurer.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.observation.autoconfigure.ScheduledTasksObservationAutoConfiguration");
    beanDefinition.setInstanceSupplier(getObservabilitySchedulingConfigurerInstanceSupplier());
    return beanDefinition;
  }
}
