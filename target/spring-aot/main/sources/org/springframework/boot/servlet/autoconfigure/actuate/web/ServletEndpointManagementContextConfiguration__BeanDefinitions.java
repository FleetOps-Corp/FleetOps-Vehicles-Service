package org.springframework.boot.servlet.autoconfigure.actuate.web;

import java.lang.SuppressWarnings;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.actuate.autoconfigure.endpoint.expose.IncludeExcludeEndpointFilter;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.ExposableServletEndpoint;
import org.springframework.core.ResolvableType;

/**
 * Bean definitions for {@link ServletEndpointManagementContextConfiguration}.
 */
@Generated
public class ServletEndpointManagementContextConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'servletEndpointManagementContextConfiguration'.
   */
  public static BeanDefinition getServletEndpointManagementContextConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ServletEndpointManagementContextConfiguration.class);
    beanDefinition.setInstanceSupplier(ServletEndpointManagementContextConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'servletExposeExcludePropertyEndpointFilter'.
   */
  private static BeanInstanceSupplier<IncludeExcludeEndpointFilter> getServletExposeExcludePropertyEndpointFilterInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<IncludeExcludeEndpointFilter>forFactoryMethod(ServletEndpointManagementContextConfiguration.class, "servletExposeExcludePropertyEndpointFilter", WebEndpointProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.servlet.autoconfigure.actuate.web.ServletEndpointManagementContextConfiguration", ServletEndpointManagementContextConfiguration.class).servletExposeExcludePropertyEndpointFilter(args.get(0)));
  }

  /**
   * Get the bean definition for 'servletExposeExcludePropertyEndpointFilter'.
   */
  @SuppressWarnings("removal")
  public static BeanDefinition getServletExposeExcludePropertyEndpointFilterBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(IncludeExcludeEndpointFilter.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(IncludeExcludeEndpointFilter.class, ExposableServletEndpoint.class));
    beanDefinition.setFactoryBeanName("org.springframework.boot.servlet.autoconfigure.actuate.web.ServletEndpointManagementContextConfiguration");
    beanDefinition.setInstanceSupplier(getServletExposeExcludePropertyEndpointFilterInstanceSupplier());
    return beanDefinition;
  }
}
