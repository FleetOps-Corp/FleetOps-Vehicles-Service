package org.springframework.boot.micrometer.metrics.autoconfigure.ssl;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.ssl.SslBundles;

/**
 * Bean definitions for {@link SslMetricsAutoConfiguration}.
 */
@Generated
public class SslMetricsAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'sslMetricsAutoConfiguration'.
   */
  public static BeanDefinition getSslMetricsAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SslMetricsAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(SslMetricsAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'sslMeterBinder'.
   */
  private static BeanInstanceSupplier<SslMeterBinder> getSslMeterBinderInstanceSupplier() {
    return BeanInstanceSupplier.<SslMeterBinder>forFactoryMethod(SslMetricsAutoConfiguration.class, "sslMeterBinder", SslBundles.class, ObjectProvider.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.metrics.autoconfigure.ssl.SslMetricsAutoConfiguration", SslMetricsAutoConfiguration.class).sslMeterBinder(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'sslMeterBinder'.
   */
  public static BeanDefinition getSslMeterBinderBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SslMeterBinder.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.metrics.autoconfigure.ssl.SslMetricsAutoConfiguration");
    beanDefinition.setInstanceSupplier(getSslMeterBinderInstanceSupplier());
    return beanDefinition;
  }
}
