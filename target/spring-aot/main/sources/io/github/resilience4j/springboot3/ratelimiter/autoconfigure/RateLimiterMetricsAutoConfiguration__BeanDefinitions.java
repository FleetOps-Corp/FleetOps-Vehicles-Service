package io.github.resilience4j.springboot3.ratelimiter.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link RateLimiterMetricsAutoConfiguration}.
 */
@Generated
public class RateLimiterMetricsAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'rateLimiterMetricsAutoConfiguration'.
   */
  public static BeanDefinition getRateLimiterMetricsAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RateLimiterMetricsAutoConfiguration.class);
    beanDefinition.setTargetType(RateLimiterMetricsAutoConfiguration.class);
    ConfigurationClassUtils.initializeConfigurationClass(RateLimiterMetricsAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(RateLimiterMetricsAutoConfiguration$$SpringCGLIB$$0::new);
    return beanDefinition;
  }
}
