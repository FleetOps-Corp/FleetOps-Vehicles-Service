package io.github.resilience4j.springboot3.micrometer.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link TimerProperties}.
 */
@Generated
public class TimerProperties__BeanDefinitions {
  /**
   * Get the bean definition for 'timerProperties'.
   */
  public static BeanDefinition getTimerPropertiesBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TimerProperties.class);
    beanDefinition.setInstanceSupplier(TimerProperties::new);
    return beanDefinition;
  }
}
