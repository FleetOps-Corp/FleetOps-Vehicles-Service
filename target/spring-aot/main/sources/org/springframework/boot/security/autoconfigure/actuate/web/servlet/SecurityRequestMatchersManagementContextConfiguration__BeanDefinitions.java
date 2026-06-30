package org.springframework.boot.security.autoconfigure.actuate.web.servlet;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletPath;

/**
 * Bean definitions for {@link SecurityRequestMatchersManagementContextConfiguration}.
 */
@Generated
public class SecurityRequestMatchersManagementContextConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'securityRequestMatchersManagementContextConfiguration'.
   */
  public static BeanDefinition getSecurityRequestMatchersManagementContextConfigurationBeanDefinition(
      ) {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SecurityRequestMatchersManagementContextConfiguration.class);
    beanDefinition.setInstanceSupplier(SecurityRequestMatchersManagementContextConfiguration::new);
    return beanDefinition;
  }

  /**
   * Bean definitions for {@link SecurityRequestMatchersManagementContextConfiguration.MvcRequestMatcherConfiguration}.
   */
  @Generated
  public static class MvcRequestMatcherConfiguration {
    /**
     * Get the bean definition for 'mvcRequestMatcherConfiguration'.
     */
    public static BeanDefinition getMvcRequestMatcherConfigurationBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(SecurityRequestMatchersManagementContextConfiguration.MvcRequestMatcherConfiguration.class);
      beanDefinition.setInstanceSupplier(SecurityRequestMatchersManagementContextConfiguration.MvcRequestMatcherConfiguration::new);
      return beanDefinition;
    }

    /**
     * Get the bean instance supplier for 'requestMatcherProvider'.
     */
    private static BeanInstanceSupplier<RequestMatcherProvider> getRequestMatcherProviderInstanceSupplier(
        ) {
      return BeanInstanceSupplier.<RequestMatcherProvider>forFactoryMethod(SecurityRequestMatchersManagementContextConfiguration.MvcRequestMatcherConfiguration.class, "requestMatcherProvider", DispatcherServletPath.class)
              .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.security.autoconfigure.actuate.web.servlet.SecurityRequestMatchersManagementContextConfiguration$MvcRequestMatcherConfiguration", SecurityRequestMatchersManagementContextConfiguration.MvcRequestMatcherConfiguration.class).requestMatcherProvider(args.get(0)));
    }

    /**
     * Get the bean definition for 'requestMatcherProvider'.
     */
    public static BeanDefinition getRequestMatcherProviderBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(RequestMatcherProvider.class);
      beanDefinition.setFactoryBeanName("org.springframework.boot.security.autoconfigure.actuate.web.servlet.SecurityRequestMatchersManagementContextConfiguration$MvcRequestMatcherConfiguration");
      beanDefinition.setInstanceSupplier(getRequestMatcherProviderInstanceSupplier());
      return beanDefinition;
    }
  }
}
