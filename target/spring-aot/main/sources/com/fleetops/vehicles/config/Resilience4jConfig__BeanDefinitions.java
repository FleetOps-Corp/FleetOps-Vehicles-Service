package com.fleetops.vehicles.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link Resilience4jConfig}.
 */
@Generated
public class Resilience4jConfig__BeanDefinitions {
  /**
   * Get the bean definition for 'resilience4jConfig'.
   */
  public static BeanDefinition getResiliencejConfigBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(Resilience4jConfig.class);
    beanDefinition.setTargetType(Resilience4jConfig.class);
    ConfigurationClassUtils.initializeConfigurationClass(Resilience4jConfig.class);
    beanDefinition.setInstanceSupplier(Resilience4jConfig$$SpringCGLIB$$0::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'circuitBreakerRegistry'.
   */
  private static BeanInstanceSupplier<CircuitBreakerRegistry> getCircuitBreakerRegistryInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<CircuitBreakerRegistry>forFactoryMethod(Resilience4jConfig$$SpringCGLIB$$0.class, "circuitBreakerRegistry")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("resilience4jConfig", Resilience4jConfig.class).circuitBreakerRegistry());
  }

  /**
   * Get the bean definition for 'circuitBreakerRegistry'.
   */
  public static BeanDefinition getCircuitBreakerRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CircuitBreakerRegistry.class);
    beanDefinition.setFactoryBeanName("resilience4jConfig");
    beanDefinition.setInstanceSupplier(getCircuitBreakerRegistryInstanceSupplier());
    return beanDefinition;
  }
}
