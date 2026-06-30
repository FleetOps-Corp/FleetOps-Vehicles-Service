package io.github.resilience4j.springboot3.retry.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link RetryProperties}.
 */
@Generated
public class RetryProperties__BeanDefinitions {
  /**
   * Get the bean definition for 'retryProperties'.
   */
  public static BeanDefinition getRetryPropertiesBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RetryProperties.class);
    beanDefinition.setInstanceSupplier(RetryProperties::new);
    return beanDefinition;
  }
}
