package org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link PrometheusMetricsExportAutoConfiguration}.
 */
@Generated
public class PrometheusMetricsExportAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'prometheusMetricsExportAutoConfiguration'.
   */
  public static BeanDefinition getPrometheusMetricsExportAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(PrometheusMetricsExportAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(PrometheusMetricsExportAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'prometheusConfig'.
   */
  private static BeanInstanceSupplier<PrometheusConfig> getPrometheusConfigInstanceSupplier() {
    return BeanInstanceSupplier.<PrometheusConfig>forFactoryMethod(PrometheusMetricsExportAutoConfiguration.class, "prometheusConfig", PrometheusProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusMetricsExportAutoConfiguration", PrometheusMetricsExportAutoConfiguration.class).prometheusConfig(args.get(0)));
  }

  /**
   * Get the bean definition for 'prometheusConfig'.
   */
  public static BeanDefinition getPrometheusConfigBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(PrometheusConfig.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusMetricsExportAutoConfiguration");
    beanDefinition.setInstanceSupplier(getPrometheusConfigInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'prometheusMeterRegistry'.
   */
  private static BeanInstanceSupplier<PrometheusMeterRegistry> getPrometheusMeterRegistryInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<PrometheusMeterRegistry>forFactoryMethod(PrometheusMetricsExportAutoConfiguration.class, "prometheusMeterRegistry", PrometheusConfig.class, PrometheusRegistry.class, Clock.class, ObjectProvider.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusMetricsExportAutoConfiguration", PrometheusMetricsExportAutoConfiguration.class).prometheusMeterRegistry(args.get(0), args.get(1), args.get(2), args.get(3)));
  }

  /**
   * Get the bean definition for 'prometheusMeterRegistry'.
   */
  public static BeanDefinition getPrometheusMeterRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(PrometheusMeterRegistry.class);
    beanDefinition.setDestroyMethodNames("close");
    beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusMetricsExportAutoConfiguration");
    beanDefinition.setInstanceSupplier(getPrometheusMeterRegistryInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'prometheusRegistry'.
   */
  private static BeanInstanceSupplier<PrometheusRegistry> getPrometheusRegistryInstanceSupplier() {
    return BeanInstanceSupplier.<PrometheusRegistry>forFactoryMethod(PrometheusMetricsExportAutoConfiguration.class, "prometheusRegistry")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusMetricsExportAutoConfiguration", PrometheusMetricsExportAutoConfiguration.class).prometheusRegistry());
  }

  /**
   * Get the bean definition for 'prometheusRegistry'.
   */
  public static BeanDefinition getPrometheusRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(PrometheusRegistry.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusMetricsExportAutoConfiguration");
    beanDefinition.setInstanceSupplier(getPrometheusRegistryInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Bean definitions for {@link PrometheusMetricsExportAutoConfiguration.PrometheusScrapeEndpointConfiguration}.
   */
  @Generated
  public static class PrometheusScrapeEndpointConfiguration {
    /**
     * Get the bean definition for 'prometheusScrapeEndpointConfiguration'.
     */
    public static BeanDefinition getPrometheusScrapeEndpointConfigurationBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(PrometheusMetricsExportAutoConfiguration.PrometheusScrapeEndpointConfiguration.class);
      beanDefinition.setInstanceSupplier(PrometheusMetricsExportAutoConfiguration.PrometheusScrapeEndpointConfiguration::new);
      return beanDefinition;
    }

    /**
     * Get the bean instance supplier for 'prometheusEndpoint'.
     */
    private static BeanInstanceSupplier<PrometheusScrapeEndpoint> getPrometheusEndpointInstanceSupplier(
        ) {
      return BeanInstanceSupplier.<PrometheusScrapeEndpoint>forFactoryMethod(PrometheusMetricsExportAutoConfiguration.PrometheusScrapeEndpointConfiguration.class, "prometheusEndpoint", PrometheusRegistry.class, PrometheusConfig.class)
              .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusMetricsExportAutoConfiguration$PrometheusScrapeEndpointConfiguration", PrometheusMetricsExportAutoConfiguration.PrometheusScrapeEndpointConfiguration.class).prometheusEndpoint(args.get(0), args.get(1)));
    }

    /**
     * Get the bean definition for 'prometheusEndpoint'.
     */
    public static BeanDefinition getPrometheusEndpointBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(PrometheusScrapeEndpoint.class);
      beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusMetricsExportAutoConfiguration$PrometheusScrapeEndpointConfiguration");
      beanDefinition.setInstanceSupplier(getPrometheusEndpointInstanceSupplier());
      return beanDefinition;
    }
  }
}
