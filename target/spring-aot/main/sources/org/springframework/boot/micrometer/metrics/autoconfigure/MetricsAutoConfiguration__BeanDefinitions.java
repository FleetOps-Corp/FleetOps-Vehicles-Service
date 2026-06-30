package org.springframework.boot.micrometer.metrics.autoconfigure;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationHandlerGroup;
import org.springframework.context.ApplicationContext;

/**
 * Bean definitions for {@link MetricsAutoConfiguration}.
 */
@Generated
public class MetricsAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'metricsAutoConfiguration'.
   */
  public static BeanDefinition getMetricsAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(MetricsAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(MetricsAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'micrometerClock'.
   */
  private static BeanInstanceSupplier<Clock> getMicrometerClockInstanceSupplier() {
    return BeanInstanceSupplier.<Clock>forFactoryMethod(MetricsAutoConfiguration.class, "micrometerClock")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration", MetricsAutoConfiguration.class).micrometerClock());
  }

  /**
   * Get the bean definition for 'micrometerClock'.
   */
  public static BeanDefinition getMicrometerClockBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(Clock.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration");
    beanDefinition.setInstanceSupplier(getMicrometerClockInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'meterRegistryPostProcessor'.
   */
  private static BeanInstanceSupplier<MeterRegistryPostProcessor> getMeterRegistryPostProcessorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<MeterRegistryPostProcessor>forFactoryMethod(MetricsAutoConfiguration.class, "meterRegistryPostProcessor", ApplicationContext.class, ObjectProvider.class, ObjectProvider.class, ObjectProvider.class, ObjectProvider.class)
            .withGenerator((registeredBean, args) -> MetricsAutoConfiguration.meterRegistryPostProcessor(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4)));
  }

  /**
   * Get the bean definition for 'meterRegistryPostProcessor'.
   */
  public static BeanDefinition getMeterRegistryPostProcessorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(MetricsAutoConfiguration.class);
    beanDefinition.setTargetType(MeterRegistryPostProcessor.class);
    beanDefinition.setInstanceSupplier(getMeterRegistryPostProcessorInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'propertiesMeterFilter'.
   */
  private static BeanInstanceSupplier<PropertiesMeterFilter> getPropertiesMeterFilterInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<PropertiesMeterFilter>forFactoryMethod(MetricsAutoConfiguration.class, "propertiesMeterFilter", MetricsProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration", MetricsAutoConfiguration.class).propertiesMeterFilter(args.get(0)));
  }

  /**
   * Get the bean definition for 'propertiesMeterFilter'.
   */
  public static BeanDefinition getPropertiesMeterFilterBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(PropertiesMeterFilter.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration");
    beanDefinition.setInstanceSupplier(getPropertiesMeterFilterInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'meterRegistryCloser'.
   */
  private static BeanInstanceSupplier<MetricsAutoConfiguration.MeterRegistryCloser> getMeterRegistryCloserInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<MetricsAutoConfiguration.MeterRegistryCloser>forFactoryMethod(MetricsAutoConfiguration.class, "meterRegistryCloser", ApplicationContext.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration", MetricsAutoConfiguration.class).meterRegistryCloser(args.get(0)));
  }

  /**
   * Get the bean definition for 'meterRegistryCloser'.
   */
  public static BeanDefinition getMeterRegistryCloserBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(MetricsAutoConfiguration.MeterRegistryCloser.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration");
    beanDefinition.setInstanceSupplier(getMeterRegistryCloserInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'metricsObservationHandlerGroup'.
   */
  private static BeanInstanceSupplier<ObservationHandlerGroup> getMetricsObservationHandlerGroupInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ObservationHandlerGroup>forFactoryMethod(MetricsAutoConfiguration.class, "metricsObservationHandlerGroup")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration", MetricsAutoConfiguration.class).metricsObservationHandlerGroup());
  }

  /**
   * Get the bean definition for 'metricsObservationHandlerGroup'.
   */
  public static BeanDefinition getMetricsObservationHandlerGroupBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ObservationHandlerGroup.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration");
    beanDefinition.setInstanceSupplier(getMetricsObservationHandlerGroupInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'defaultMeterObservationHandler'.
   */
  private static BeanInstanceSupplier<DefaultMeterObservationHandler> getDefaultMeterObservationHandlerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<DefaultMeterObservationHandler>forFactoryMethod(MetricsAutoConfiguration.class, "defaultMeterObservationHandler", ObjectProvider.class, Clock.class, MetricsProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration", MetricsAutoConfiguration.class).defaultMeterObservationHandler(args.get(0), args.get(1), args.get(2)));
  }

  /**
   * Get the bean definition for 'defaultMeterObservationHandler'.
   */
  public static BeanDefinition getDefaultMeterObservationHandlerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DefaultMeterObservationHandler.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration");
    beanDefinition.setInstanceSupplier(getDefaultMeterObservationHandlerInstanceSupplier());
    return beanDefinition;
  }
}
