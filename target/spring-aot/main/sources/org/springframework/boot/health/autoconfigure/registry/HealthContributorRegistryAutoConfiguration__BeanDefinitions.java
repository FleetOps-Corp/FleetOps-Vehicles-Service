package org.springframework.boot.health.autoconfigure.registry;

import java.util.List;
import java.util.Map;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.health.registry.DefaultHealthContributorRegistry;

/**
 * Bean definitions for {@link HealthContributorRegistryAutoConfiguration}.
 */
@Generated
public class HealthContributorRegistryAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'healthContributorRegistryAutoConfiguration'.
   */
  public static BeanDefinition getHealthContributorRegistryAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(HealthContributorRegistryAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(HealthContributorRegistryAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'healthContributorRegistry'.
   */
  private static BeanInstanceSupplier<DefaultHealthContributorRegistry> getHealthContributorRegistryInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<DefaultHealthContributorRegistry>forFactoryMethod(HealthContributorRegistryAutoConfiguration.class, "healthContributorRegistry", Map.class, ObjectProvider.class, List.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.health.autoconfigure.registry.HealthContributorRegistryAutoConfiguration", HealthContributorRegistryAutoConfiguration.class).healthContributorRegistry(args.get(0), args.get(1), args.get(2)));
  }

  /**
   * Get the bean definition for 'healthContributorRegistry'.
   */
  public static BeanDefinition getHealthContributorRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DefaultHealthContributorRegistry.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.health.autoconfigure.registry.HealthContributorRegistryAutoConfiguration");
    beanDefinition.setInstanceSupplier(getHealthContributorRegistryInstanceSupplier());
    return beanDefinition;
  }
}
