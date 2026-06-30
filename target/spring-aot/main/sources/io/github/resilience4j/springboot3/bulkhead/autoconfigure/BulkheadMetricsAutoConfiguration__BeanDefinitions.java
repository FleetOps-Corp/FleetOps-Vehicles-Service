package io.github.resilience4j.springboot3.bulkhead.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link BulkheadMetricsAutoConfiguration}.
 */
@Generated
public class BulkheadMetricsAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'bulkheadMetricsAutoConfiguration'.
   */
  public static BeanDefinition getBulkheadMetricsAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(BulkheadMetricsAutoConfiguration.class);
    beanDefinition.setTargetType(BulkheadMetricsAutoConfiguration.class);
    ConfigurationClassUtils.initializeConfigurationClass(BulkheadMetricsAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(BulkheadMetricsAutoConfiguration$$SpringCGLIB$$0::new);
    return beanDefinition;
  }
}
