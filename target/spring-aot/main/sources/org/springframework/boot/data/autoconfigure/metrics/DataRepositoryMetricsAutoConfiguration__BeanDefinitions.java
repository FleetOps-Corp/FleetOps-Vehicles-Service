package org.springframework.boot.data.autoconfigure.metrics;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.data.metrics.DefaultRepositoryTagsProvider;
import org.springframework.boot.data.metrics.MetricsRepositoryMethodInvocationListener;
import org.springframework.boot.data.metrics.RepositoryTagsProvider;

/**
 * Bean definitions for {@link DataRepositoryMetricsAutoConfiguration}.
 */
@Generated
public class DataRepositoryMetricsAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'org.springframework.boot.data.autoconfigure.metrics.DataRepositoryMetricsAutoConfiguration'.
   */
  private static BeanInstanceSupplier<DataRepositoryMetricsAutoConfiguration> getDataRepositoryMetricsAutoConfigurationInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<DataRepositoryMetricsAutoConfiguration>forConstructor(DataMetricsProperties.class)
            .withGenerator((registeredBean, args) -> new DataRepositoryMetricsAutoConfiguration(args.get(0)));
  }

  /**
   * Get the bean definition for 'dataRepositoryMetricsAutoConfiguration'.
   */
  public static BeanDefinition getDataRepositoryMetricsAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DataRepositoryMetricsAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(getDataRepositoryMetricsAutoConfigurationInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'repositoryTagsProvider'.
   */
  private static BeanInstanceSupplier<DefaultRepositoryTagsProvider> getRepositoryTagsProviderInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<DefaultRepositoryTagsProvider>forFactoryMethod(DataRepositoryMetricsAutoConfiguration.class, "repositoryTagsProvider")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.data.autoconfigure.metrics.DataRepositoryMetricsAutoConfiguration", DataRepositoryMetricsAutoConfiguration.class).repositoryTagsProvider());
  }

  /**
   * Get the bean definition for 'repositoryTagsProvider'.
   */
  public static BeanDefinition getRepositoryTagsProviderBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DefaultRepositoryTagsProvider.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.data.autoconfigure.metrics.DataRepositoryMetricsAutoConfiguration");
    beanDefinition.setInstanceSupplier(getRepositoryTagsProviderInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'metricsRepositoryMethodInvocationListener'.
   */
  private static BeanInstanceSupplier<MetricsRepositoryMethodInvocationListener> getMetricsRepositoryMethodInvocationListenerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<MetricsRepositoryMethodInvocationListener>forFactoryMethod(DataRepositoryMetricsAutoConfiguration.class, "metricsRepositoryMethodInvocationListener", ObjectProvider.class, RepositoryTagsProvider.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.data.autoconfigure.metrics.DataRepositoryMetricsAutoConfiguration", DataRepositoryMetricsAutoConfiguration.class).metricsRepositoryMethodInvocationListener(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'metricsRepositoryMethodInvocationListener'.
   */
  public static BeanDefinition getMetricsRepositoryMethodInvocationListenerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(MetricsRepositoryMethodInvocationListener.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.data.autoconfigure.metrics.DataRepositoryMetricsAutoConfiguration");
    beanDefinition.setInstanceSupplier(getMetricsRepositoryMethodInvocationListenerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'metricsRepositoryMethodInvocationListenerBeanPostProcessor'.
   */
  private static BeanInstanceSupplier<MetricsRepositoryMethodInvocationListenerBeanPostProcessor> getMetricsRepositoryMethodInvocationListenerBeanPostProcessorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<MetricsRepositoryMethodInvocationListenerBeanPostProcessor>forFactoryMethod(DataRepositoryMetricsAutoConfiguration.class, "metricsRepositoryMethodInvocationListenerBeanPostProcessor", ObjectProvider.class)
            .withGenerator((registeredBean, args) -> DataRepositoryMetricsAutoConfiguration.metricsRepositoryMethodInvocationListenerBeanPostProcessor(args.get(0)));
  }

  /**
   * Get the bean definition for 'metricsRepositoryMethodInvocationListenerBeanPostProcessor'.
   */
  public static BeanDefinition getMetricsRepositoryMethodInvocationListenerBeanPostProcessorBeanDefinition(
      ) {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DataRepositoryMetricsAutoConfiguration.class);
    beanDefinition.setTargetType(MetricsRepositoryMethodInvocationListenerBeanPostProcessor.class);
    beanDefinition.setInstanceSupplier(getMetricsRepositoryMethodInvocationListenerBeanPostProcessorInstanceSupplier());
    return beanDefinition;
  }
}
