package io.github.resilience4j.springboot3.timelimiter.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link TimeLimiterMetricsAutoConfiguration}.
 */
@Generated
public class TimeLimiterMetricsAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'timeLimiterMetricsAutoConfiguration'.
   */
  public static BeanDefinition getTimeLimiterMetricsAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TimeLimiterMetricsAutoConfiguration.class);
    beanDefinition.setTargetType(TimeLimiterMetricsAutoConfiguration.class);
    ConfigurationClassUtils.initializeConfigurationClass(TimeLimiterMetricsAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(TimeLimiterMetricsAutoConfiguration$$SpringCGLIB$$0::new);
    return beanDefinition;
  }
}
