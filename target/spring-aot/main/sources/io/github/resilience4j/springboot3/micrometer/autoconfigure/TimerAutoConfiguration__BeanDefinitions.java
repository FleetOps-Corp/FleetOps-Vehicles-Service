package io.github.resilience4j.springboot3.micrometer.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link TimerAutoConfiguration}.
 */
@Generated
public class TimerAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'timerAutoConfiguration'.
   */
  public static BeanDefinition getTimerAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TimerAutoConfiguration.class);
    beanDefinition.setTargetType(TimerAutoConfiguration.class);
    ConfigurationClassUtils.initializeConfigurationClass(TimerAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(TimerAutoConfiguration$$SpringCGLIB$$0::new);
    return beanDefinition;
  }

  /**
   * Bean definitions for {@link TimerAutoConfiguration.TimerAutoEndpointConfiguration}.
   */
  @Generated
  public static class TimerAutoEndpointConfiguration {
    /**
     * Get the bean definition for 'timerAutoEndpointConfiguration'.
     */
    public static BeanDefinition getTimerAutoEndpointConfigurationBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(TimerAutoConfiguration.TimerAutoEndpointConfiguration.class);
      beanDefinition.setTargetType(TimerAutoConfiguration.TimerAutoEndpointConfiguration.class);
      ConfigurationClassUtils.initializeConfigurationClass(TimerAutoConfiguration.TimerAutoEndpointConfiguration.class);
      beanDefinition.setInstanceSupplier(TimerAutoConfiguration$TimerAutoEndpointConfiguration$$SpringCGLIB$$0::new);
      return beanDefinition;
    }
  }
}
