package io.github.resilience4j.springboot3.bulkhead.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link BulkheadProperties}.
 */
@Generated
public class BulkheadProperties__BeanDefinitions {
  /**
   * Get the bean definition for 'bulkheadProperties'.
   */
  public static BeanDefinition getBulkheadPropertiesBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(BulkheadProperties.class);
    beanDefinition.setInstanceSupplier(BulkheadProperties::new);
    return beanDefinition;
  }
}
