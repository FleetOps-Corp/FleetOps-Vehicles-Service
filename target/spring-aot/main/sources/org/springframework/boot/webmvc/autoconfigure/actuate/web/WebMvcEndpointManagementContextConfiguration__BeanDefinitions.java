package org.springframework.boot.webmvc.autoconfigure.actuate.web;

import java.lang.SuppressWarnings;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.EndpointAccessResolver;
import org.springframework.boot.actuate.endpoint.jackson.EndpointJackson2ObjectMapper;
import org.springframework.boot.actuate.endpoint.jackson.EndpointJsonMapper;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ServletEndpointRegistrar;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.webmvc.actuate.endpoint.web.ControllerEndpointHandlerMapping;
import org.springframework.boot.webmvc.actuate.endpoint.web.WebMvcEndpointHandlerMapping;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletPath;
import org.springframework.core.env.Environment;

/**
 * Bean definitions for {@link WebMvcEndpointManagementContextConfiguration}.
 */
@Generated
public class WebMvcEndpointManagementContextConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'webMvcEndpointManagementContextConfiguration'.
   */
  public static BeanDefinition getWebMvcEndpointManagementContextConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(WebMvcEndpointManagementContextConfiguration.class);
    beanDefinition.setInstanceSupplier(WebMvcEndpointManagementContextConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'webEndpointServletHandlerMapping'.
   */
  @SuppressWarnings("removal")
  private static BeanInstanceSupplier<WebMvcEndpointHandlerMapping> getWebEndpointServletHandlerMappingInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<WebMvcEndpointHandlerMapping>forFactoryMethod(WebMvcEndpointManagementContextConfiguration.class, "webEndpointServletHandlerMapping", WebEndpointsSupplier.class, ServletEndpointsSupplier.class, ControllerEndpointsSupplier.class, EndpointMediaTypes.class, CorsEndpointProperties.class, WebEndpointProperties.class, Environment.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.webmvc.autoconfigure.actuate.web.WebMvcEndpointManagementContextConfiguration", WebMvcEndpointManagementContextConfiguration.class).webEndpointServletHandlerMapping(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5), args.get(6)));
  }

  /**
   * Get the bean definition for 'webEndpointServletHandlerMapping'.
   */
  public static BeanDefinition getWebEndpointServletHandlerMappingBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(WebMvcEndpointHandlerMapping.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.webmvc.autoconfigure.actuate.web.WebMvcEndpointManagementContextConfiguration");
    beanDefinition.setInstanceSupplier(getWebEndpointServletHandlerMappingInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'controllerEndpointHandlerMapping'.
   */
  @SuppressWarnings("removal")
  private static BeanInstanceSupplier<ControllerEndpointHandlerMapping> getControllerEndpointHandlerMappingInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ControllerEndpointHandlerMapping>forFactoryMethod(WebMvcEndpointManagementContextConfiguration.class, "controllerEndpointHandlerMapping", ControllerEndpointsSupplier.class, CorsEndpointProperties.class, WebEndpointProperties.class, EndpointAccessResolver.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.webmvc.autoconfigure.actuate.web.WebMvcEndpointManagementContextConfiguration", WebMvcEndpointManagementContextConfiguration.class).controllerEndpointHandlerMapping(args.get(0), args.get(1), args.get(2), args.get(3)));
  }

  /**
   * Get the bean definition for 'controllerEndpointHandlerMapping'.
   */
  @SuppressWarnings("removal")
  public static BeanDefinition getControllerEndpointHandlerMappingBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ControllerEndpointHandlerMapping.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.webmvc.autoconfigure.actuate.web.WebMvcEndpointManagementContextConfiguration");
    beanDefinition.setInstanceSupplier(getControllerEndpointHandlerMappingInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'servletEndpointRegistrar'.
   */
  @SuppressWarnings("removal")
  private static BeanInstanceSupplier<ServletEndpointRegistrar> getServletEndpointRegistrarInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ServletEndpointRegistrar>forFactoryMethod(WebMvcEndpointManagementContextConfiguration.class, "servletEndpointRegistrar", WebEndpointProperties.class, ServletEndpointsSupplier.class, DispatcherServletPath.class, EndpointAccessResolver.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.webmvc.autoconfigure.actuate.web.WebMvcEndpointManagementContextConfiguration", WebMvcEndpointManagementContextConfiguration.class).servletEndpointRegistrar(args.get(0), args.get(1), args.get(2), args.get(3)));
  }

  /**
   * Get the bean definition for 'servletEndpointRegistrar'.
   */
  @SuppressWarnings("removal")
  public static BeanDefinition getServletEndpointRegistrarBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ServletEndpointRegistrar.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.webmvc.autoconfigure.actuate.web.WebMvcEndpointManagementContextConfiguration");
    beanDefinition.setInstanceSupplier(getServletEndpointRegistrarInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'endpointJsonMapperWebMvcConfigurer'.
   */
  private static BeanInstanceSupplier<WebMvcEndpointManagementContextConfiguration.EndpointJsonMapperWebMvcConfigurer> getEndpointJsonMapperWebMvcConfigurerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<WebMvcEndpointManagementContextConfiguration.EndpointJsonMapperWebMvcConfigurer>forFactoryMethod(WebMvcEndpointManagementContextConfiguration.class, "endpointJsonMapperWebMvcConfigurer", EndpointJsonMapper.class)
            .withGenerator((registeredBean, args) -> WebMvcEndpointManagementContextConfiguration.endpointJsonMapperWebMvcConfigurer(args.get(0)));
  }

  /**
   * Get the bean definition for 'endpointJsonMapperWebMvcConfigurer'.
   */
  public static BeanDefinition getEndpointJsonMapperWebMvcConfigurerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(WebMvcEndpointManagementContextConfiguration.class);
    beanDefinition.setTargetType(WebMvcEndpointManagementContextConfiguration.EndpointJsonMapperWebMvcConfigurer.class);
    beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    beanDefinition.setInstanceSupplier(getEndpointJsonMapperWebMvcConfigurerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'endpointJackson2ObjectMapperWebMvcConfigurer'.
   */
  @SuppressWarnings("removal")
  private static BeanInstanceSupplier<WebMvcEndpointManagementContextConfiguration.EndpointJackson2ObjectMapperWebMvcConfigurer> getEndpointJacksonObjectMapperWebMvcConfigurerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<WebMvcEndpointManagementContextConfiguration.EndpointJackson2ObjectMapperWebMvcConfigurer>forFactoryMethod(WebMvcEndpointManagementContextConfiguration.class, "endpointJackson2ObjectMapperWebMvcConfigurer", EndpointJackson2ObjectMapper.class)
            .withGenerator((registeredBean, args) -> WebMvcEndpointManagementContextConfiguration.endpointJackson2ObjectMapperWebMvcConfigurer(args.get(0)));
  }

  /**
   * Get the bean definition for 'endpointJackson2ObjectMapperWebMvcConfigurer'.
   */
  @SuppressWarnings("removal")
  public static BeanDefinition getEndpointJacksonObjectMapperWebMvcConfigurerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(WebMvcEndpointManagementContextConfiguration.class);
    beanDefinition.setTargetType(WebMvcEndpointManagementContextConfiguration.EndpointJackson2ObjectMapperWebMvcConfigurer.class);
    beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    beanDefinition.setInstanceSupplier(getEndpointJacksonObjectMapperWebMvcConfigurerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Bean definitions for {@link WebMvcEndpointManagementContextConfiguration.HealthConfiguration}.
   */
  @Generated
  public static class HealthConfiguration {
    /**
     * Get the bean definition for 'healthConfiguration'.
     */
    public static BeanDefinition getHealthConfigurationBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(WebMvcEndpointManagementContextConfiguration.HealthConfiguration.class);
      beanDefinition.setInstanceSupplier(WebMvcEndpointManagementContextConfiguration.HealthConfiguration::new);
      return beanDefinition;
    }
  }
}
