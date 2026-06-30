package org.springframework.boot.servlet.autoconfigure.actuate.web;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;

/**
 * Bean definitions for {@link ServletManagementContextAutoConfiguration}.
 */
@Generated
public class ServletManagementContextAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'servletManagementContextAutoConfiguration'.
   */
  public static BeanDefinition getServletManagementContextAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ServletManagementContextAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(ServletManagementContextAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'managementServletContext'.
   */
  private static BeanInstanceSupplier<ManagementServletContext> getManagementServletContextInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ManagementServletContext>forFactoryMethod(ServletManagementContextAutoConfiguration.class, "managementServletContext", WebEndpointProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.servlet.autoconfigure.actuate.web.ServletManagementContextAutoConfiguration", ServletManagementContextAutoConfiguration.class).managementServletContext(args.get(0)));
  }

  /**
   * Get the bean definition for 'managementServletContext'.
   */
  public static BeanDefinition getManagementServletContextBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ManagementServletContext.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.servlet.autoconfigure.actuate.web.ServletManagementContextAutoConfiguration");
    beanDefinition.setInstanceSupplier(getManagementServletContextInstanceSupplier());
    return beanDefinition;
  }
}
