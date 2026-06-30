package io.github.resilience4j.springboot3.timelimiter.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link TimeLimiterAutoConfiguration}.
 */
@Generated
public class TimeLimiterAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'timeLimiterAutoConfiguration'.
   */
  public static BeanDefinition getTimeLimiterAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TimeLimiterAutoConfiguration.class);
    beanDefinition.setTargetType(TimeLimiterAutoConfiguration.class);
    ConfigurationClassUtils.initializeConfigurationClass(TimeLimiterAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(TimeLimiterAutoConfiguration$$SpringCGLIB$$0::new);
    return beanDefinition;
  }

  /**
   * Bean definitions for {@link TimeLimiterAutoConfiguration.TimeLimiterAutoEndpointConfiguration}.
   */
  @Generated
  public static class TimeLimiterAutoEndpointConfiguration {
    /**
     * Get the bean definition for 'timeLimiterAutoEndpointConfiguration'.
     */
    public static BeanDefinition getTimeLimiterAutoEndpointConfigurationBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(TimeLimiterAutoConfiguration.TimeLimiterAutoEndpointConfiguration.class);
      beanDefinition.setTargetType(TimeLimiterAutoConfiguration.TimeLimiterAutoEndpointConfiguration.class);
      ConfigurationClassUtils.initializeConfigurationClass(TimeLimiterAutoConfiguration.TimeLimiterAutoEndpointConfiguration.class);
      beanDefinition.setInstanceSupplier(TimeLimiterAutoConfiguration$TimeLimiterAutoEndpointConfiguration$$SpringCGLIB$$0::new);
      return beanDefinition;
    }
  }
}
