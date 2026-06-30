package org.springframework.boot.webmvc.autoconfigure.actuate.endpoint.web;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointGroups;
import org.springframework.boot.webmvc.actuate.endpoint.web.AdditionalHealthEndpointPathsWebMvcHandlerMapping;

/**
 * Bean definitions for {@link WebMvcHealthEndpointExtensionAutoConfiguration}.
 */
@Generated
public class WebMvcHealthEndpointExtensionAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'webMvcHealthEndpointExtensionAutoConfiguration'.
   */
  public static BeanDefinition getWebMvcHealthEndpointExtensionAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(WebMvcHealthEndpointExtensionAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(WebMvcHealthEndpointExtensionAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'healthEndpointWebMvcHandlerMapping'.
   */
  private static BeanInstanceSupplier<AdditionalHealthEndpointPathsWebMvcHandlerMapping> getHealthEndpointWebMvcHandlerMappingInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<AdditionalHealthEndpointPathsWebMvcHandlerMapping>forFactoryMethod(WebMvcHealthEndpointExtensionAutoConfiguration.class, "healthEndpointWebMvcHandlerMapping", WebEndpointsSupplier.class, HealthEndpointGroups.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.webmvc.autoconfigure.actuate.endpoint.web.WebMvcHealthEndpointExtensionAutoConfiguration", WebMvcHealthEndpointExtensionAutoConfiguration.class).healthEndpointWebMvcHandlerMapping(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'healthEndpointWebMvcHandlerMapping'.
   */
  public static BeanDefinition getHealthEndpointWebMvcHandlerMappingBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(AdditionalHealthEndpointPathsWebMvcHandlerMapping.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.webmvc.autoconfigure.actuate.endpoint.web.WebMvcHealthEndpointExtensionAutoConfiguration");
    beanDefinition.setInstanceSupplier(getHealthEndpointWebMvcHandlerMappingInstanceSupplier());
    return beanDefinition;
  }
}
