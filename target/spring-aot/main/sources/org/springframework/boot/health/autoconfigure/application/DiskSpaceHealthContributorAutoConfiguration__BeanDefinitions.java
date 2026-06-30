package org.springframework.boot.health.autoconfigure.application;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.health.application.DiskSpaceHealthIndicator;

/**
 * Bean definitions for {@link DiskSpaceHealthContributorAutoConfiguration}.
 */
@Generated
public class DiskSpaceHealthContributorAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'diskSpaceHealthContributorAutoConfiguration'.
   */
  public static BeanDefinition getDiskSpaceHealthContributorAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DiskSpaceHealthContributorAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(DiskSpaceHealthContributorAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'diskSpaceHealthIndicator'.
   */
  private static BeanInstanceSupplier<DiskSpaceHealthIndicator> getDiskSpaceHealthIndicatorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<DiskSpaceHealthIndicator>forFactoryMethod(DiskSpaceHealthContributorAutoConfiguration.class, "diskSpaceHealthIndicator", DiskSpaceHealthIndicatorProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.health.autoconfigure.application.DiskSpaceHealthContributorAutoConfiguration", DiskSpaceHealthContributorAutoConfiguration.class).diskSpaceHealthIndicator(args.get(0)));
  }

  /**
   * Get the bean definition for 'diskSpaceHealthIndicator'.
   */
  public static BeanDefinition getDiskSpaceHealthIndicatorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DiskSpaceHealthIndicator.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.health.autoconfigure.application.DiskSpaceHealthContributorAutoConfiguration");
    beanDefinition.setInstanceSupplier(getDiskSpaceHealthIndicatorInstanceSupplier());
    return beanDefinition;
  }
}
