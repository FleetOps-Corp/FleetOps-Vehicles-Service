package io.github.resilience4j.springboot3.ratelimiter.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link RateLimiterProperties}.
 */
@Generated
public class RateLimiterProperties__BeanDefinitions {
  /**
   * Get the bean definition for 'rateLimiterProperties'.
   */
  public static BeanDefinition getRateLimiterPropertiesBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RateLimiterProperties.class);
    beanDefinition.setInstanceSupplier(RateLimiterProperties::new);
    return beanDefinition;
  }
}
