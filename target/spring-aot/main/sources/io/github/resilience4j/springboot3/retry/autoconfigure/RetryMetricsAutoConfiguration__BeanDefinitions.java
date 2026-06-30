package io.github.resilience4j.springboot3.retry.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link RetryMetricsAutoConfiguration}.
 */
@Generated
public class RetryMetricsAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'retryMetricsAutoConfiguration'.
   */
  public static BeanDefinition getRetryMetricsAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RetryMetricsAutoConfiguration.class);
    beanDefinition.setTargetType(RetryMetricsAutoConfiguration.class);
    ConfigurationClassUtils.initializeConfigurationClass(RetryMetricsAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(RetryMetricsAutoConfiguration$$SpringCGLIB$$0::new);
    return beanDefinition;
  }
}
