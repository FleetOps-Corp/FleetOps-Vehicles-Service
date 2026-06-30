package org.springframework.boot.health.autoconfigure.actuate.endpoint;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointGroups;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointWebExtension;
import org.springframework.boot.health.registry.HealthContributorRegistry;

/**
 * Bean definitions for {@link HealthEndpointWebExtensionConfiguration}.
 */
@Generated
public class HealthEndpointWebExtensionConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'healthEndpointWebExtensionConfiguration'.
   */
  public static BeanDefinition getHealthEndpointWebExtensionConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(HealthEndpointWebExtensionConfiguration.class);
    beanDefinition.setInstanceSupplier(HealthEndpointWebExtensionConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'healthEndpointWebExtension'.
   */
  private static BeanInstanceSupplier<HealthEndpointWebExtension> getHealthEndpointWebExtensionInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<HealthEndpointWebExtension>forFactoryMethod(HealthEndpointWebExtensionConfiguration.class, "healthEndpointWebExtension", HealthContributorRegistry.class, ObjectProvider.class, HealthEndpointGroups.class, HealthEndpointProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointWebExtensionConfiguration", HealthEndpointWebExtensionConfiguration.class).healthEndpointWebExtension(args.get(0), args.get(1), args.get(2), args.get(3)));
  }

  /**
   * Get the bean definition for 'healthEndpointWebExtension'.
   */
  public static BeanDefinition getHealthEndpointWebExtensionBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(HealthEndpointWebExtension.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointWebExtensionConfiguration");
    beanDefinition.setInstanceSupplier(getHealthEndpointWebExtensionInstanceSupplier());
    return beanDefinition;
  }
}
