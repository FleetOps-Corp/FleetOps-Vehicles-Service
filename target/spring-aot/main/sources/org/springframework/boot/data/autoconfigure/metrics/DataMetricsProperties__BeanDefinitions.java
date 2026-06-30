package org.springframework.boot.data.autoconfigure.metrics;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link DataMetricsProperties}.
 */
@Generated
public class DataMetricsProperties__BeanDefinitions {
  /**
   * Get the bean definition for 'dataMetricsProperties'.
   */
  public static BeanDefinition getDataMetricsPropertiesBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DataMetricsProperties.class);
    beanDefinition.setInstanceSupplier(DataMetricsProperties::new);
    return beanDefinition;
  }
}
