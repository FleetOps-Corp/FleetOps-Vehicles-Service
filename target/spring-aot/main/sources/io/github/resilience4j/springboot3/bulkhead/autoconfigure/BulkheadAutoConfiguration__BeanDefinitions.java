package io.github.resilience4j.springboot3.bulkhead.autoconfigure;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link BulkheadAutoConfiguration}.
 */
@Generated
public class BulkheadAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'bulkheadAutoConfiguration'.
   */
  public static BeanDefinition getBulkheadAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(BulkheadAutoConfiguration.class);
    beanDefinition.setTargetType(BulkheadAutoConfiguration.class);
    ConfigurationClassUtils.initializeConfigurationClass(BulkheadAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(BulkheadAutoConfiguration$$SpringCGLIB$$0::new);
    return beanDefinition;
  }

  /**
   * Bean definitions for {@link BulkheadAutoConfiguration.BulkheadEndpointAutoConfiguration}.
   */
  @Generated
  public static class BulkheadEndpointAutoConfiguration {
    /**
     * Get the bean definition for 'bulkheadEndpointAutoConfiguration'.
     */
    public static BeanDefinition getBulkheadEndpointAutoConfigurationBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(BulkheadAutoConfiguration.BulkheadEndpointAutoConfiguration.class);
      beanDefinition.setTargetType(BulkheadAutoConfiguration.BulkheadEndpointAutoConfiguration.class);
      ConfigurationClassUtils.initializeConfigurationClass(BulkheadAutoConfiguration.BulkheadEndpointAutoConfiguration.class);
      beanDefinition.setInstanceSupplier(BulkheadAutoConfiguration$BulkheadEndpointAutoConfiguration$$SpringCGLIB$$0::new);
      return beanDefinition;
    }
  }
}
