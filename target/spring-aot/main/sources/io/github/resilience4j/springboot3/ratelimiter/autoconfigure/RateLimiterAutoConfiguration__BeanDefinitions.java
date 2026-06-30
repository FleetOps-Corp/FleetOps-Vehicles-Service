package io.github.resilience4j.springboot3.ratelimiter.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link RateLimiterAutoConfiguration}.
 */
@Generated
public class RateLimiterAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'rateLimiterAutoConfiguration'.
   */
  public static BeanDefinition getRateLimiterAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RateLimiterAutoConfiguration.class);
    beanDefinition.setTargetType(RateLimiterAutoConfiguration.class);
    ConfigurationClassUtils.initializeConfigurationClass(RateLimiterAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(RateLimiterAutoConfiguration$$SpringCGLIB$$0::new);
    return beanDefinition;
  }

  /**
   * Bean definitions for {@link RateLimiterAutoConfiguration.RateLimiterEndpointAutoConfiguration}.
   */
  @Generated
  public static class RateLimiterEndpointAutoConfiguration {
    /**
     * Get the bean definition for 'rateLimiterEndpointAutoConfiguration'.
     */
    public static BeanDefinition getRateLimiterEndpointAutoConfigurationBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(RateLimiterAutoConfiguration.RateLimiterEndpointAutoConfiguration.class);
      beanDefinition.setTargetType(RateLimiterAutoConfiguration.RateLimiterEndpointAutoConfiguration.class);
      ConfigurationClassUtils.initializeConfigurationClass(RateLimiterAutoConfiguration.RateLimiterEndpointAutoConfiguration.class);
      beanDefinition.setInstanceSupplier(RateLimiterAutoConfiguration$RateLimiterEndpointAutoConfiguration$$SpringCGLIB$$0::new);
      return beanDefinition;
    }
  }
}
