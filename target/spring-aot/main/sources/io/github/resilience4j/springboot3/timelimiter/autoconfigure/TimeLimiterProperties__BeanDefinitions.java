package io.github.resilience4j.springboot3.timelimiter.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link TimeLimiterProperties}.
 */
@Generated
public class TimeLimiterProperties__BeanDefinitions {
  /**
   * Get the bean definition for 'timeLimiterProperties'.
   */
  public static BeanDefinition getTimeLimiterPropertiesBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TimeLimiterProperties.class);
    beanDefinition.setInstanceSupplier(TimeLimiterProperties::new);
    return beanDefinition;
  }
}
