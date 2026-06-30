package io.github.resilience4j.springboot3.bulkhead.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link ThreadPoolBulkheadMetricsAutoConfiguration}.
 */
@Generated
public class ThreadPoolBulkheadMetricsAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'threadPoolBulkheadMetricsAutoConfiguration'.
   */
  public static BeanDefinition getThreadPoolBulkheadMetricsAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ThreadPoolBulkheadMetricsAutoConfiguration.class);
    beanDefinition.setTargetType(ThreadPoolBulkheadMetricsAutoConfiguration.class);
    ConfigurationClassUtils.initializeConfigurationClass(ThreadPoolBulkheadMetricsAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(ThreadPoolBulkheadMetricsAutoConfiguration$$SpringCGLIB$$0::new);
    return beanDefinition;
  }
}
