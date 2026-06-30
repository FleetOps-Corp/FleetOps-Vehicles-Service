package io.github.resilience4j.springboot3.retry.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link RetryAutoConfiguration}.
 */
@Generated
public class RetryAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'retryAutoConfiguration'.
   */
  public static BeanDefinition getRetryAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RetryAutoConfiguration.class);
    beanDefinition.setTargetType(RetryAutoConfiguration.class);
    ConfigurationClassUtils.initializeConfigurationClass(RetryAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(RetryAutoConfiguration$$SpringCGLIB$$0::new);
    return beanDefinition;
  }

  /**
   * Bean definitions for {@link RetryAutoConfiguration.RetryAutoEndpointConfiguration}.
   */
  @Generated
  public static class RetryAutoEndpointConfiguration {
    /**
     * Get the bean definition for 'retryAutoEndpointConfiguration'.
     */
    public static BeanDefinition getRetryAutoEndpointConfigurationBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(RetryAutoConfiguration.RetryAutoEndpointConfiguration.class);
      beanDefinition.setTargetType(RetryAutoConfiguration.RetryAutoEndpointConfiguration.class);
      ConfigurationClassUtils.initializeConfigurationClass(RetryAutoConfiguration.RetryAutoEndpointConfiguration.class);
      beanDefinition.setInstanceSupplier(RetryAutoConfiguration$RetryAutoEndpointConfiguration$$SpringCGLIB$$0::new);
      return beanDefinition;
    }
  }
}
