package io.github.resilience4j.springboot3.bulkhead.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link ThreadPoolBulkheadProperties}.
 */
@Generated
public class ThreadPoolBulkheadProperties__BeanDefinitions {
  /**
   * Get the bean definition for 'threadPoolBulkheadProperties'.
   */
  public static BeanDefinition getThreadPoolBulkheadPropertiesBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ThreadPoolBulkheadProperties.class);
    beanDefinition.setInstanceSupplier(ThreadPoolBulkheadProperties::new);
    return beanDefinition;
  }
}
