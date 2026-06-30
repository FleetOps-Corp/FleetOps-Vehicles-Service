package org.springframework.boot.actuate.autoconfigure.endpoint.web;

import java.lang.SuppressWarnings;
import java.util.Collection;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.actuate.autoconfigure.endpoint.expose.IncludeExcludeEndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointAccessResolver;
import org.springframework.boot.actuate.endpoint.OperationFilter;
import org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.web.annotation.ExposableControllerEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointDiscoverer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

/**
 * Bean definitions for {@link WebEndpointAutoConfiguration}.
 */
@Generated
public class WebEndpointAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration'.
   */
  private static BeanInstanceSupplier<WebEndpointAutoConfiguration> getWebEndpointAutoConfigurationInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<WebEndpointAutoConfiguration>forConstructor(ApplicationContext.class, WebEndpointProperties.class)
            .withGenerator((registeredBean, args) -> new WebEndpointAutoConfiguration(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'webEndpointAutoConfiguration'.
   */
  public static BeanDefinition getWebEndpointAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(WebEndpointAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(getWebEndpointAutoConfigurationInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'webEndpointPathMapper'.
   */
  private static BeanInstanceSupplier<PathMapper> getWebEndpointPathMapperInstanceSupplier() {
    return BeanInstanceSupplier.<PathMapper>forFactoryMethod(WebEndpointAutoConfiguration.class, "webEndpointPathMapper")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration", WebEndpointAutoConfiguration.class).webEndpointPathMapper());
  }

  /**
   * Get the bean definition for 'webEndpointPathMapper'.
   */
  public static BeanDefinition getWebEndpointPathMapperBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(PathMapper.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration");
    beanDefinition.setInstanceSupplier(getWebEndpointPathMapperInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'endpointMediaTypes'.
   */
  private static BeanInstanceSupplier<EndpointMediaTypes> getEndpointMediaTypesInstanceSupplier() {
    return BeanInstanceSupplier.<EndpointMediaTypes>forFactoryMethod(WebEndpointAutoConfiguration.class, "endpointMediaTypes")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration", WebEndpointAutoConfiguration.class).endpointMediaTypes());
  }

  /**
   * Get the bean definition for 'endpointMediaTypes'.
   */
  public static BeanDefinition getEndpointMediaTypesBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(EndpointMediaTypes.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration");
    beanDefinition.setInstanceSupplier(getEndpointMediaTypesInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'webEndpointDiscoverer'.
   */
  private static BeanInstanceSupplier<WebEndpointDiscoverer> getWebEndpointDiscovererInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<WebEndpointDiscoverer>forFactoryMethod(WebEndpointAutoConfiguration.class, "webEndpointDiscoverer", ParameterValueMapper.class, EndpointMediaTypes.class, ObjectProvider.class, ObjectProvider.class, ObjectProvider.class, ObjectProvider.class, ObjectProvider.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration", WebEndpointAutoConfiguration.class).webEndpointDiscoverer(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5), args.get(6)));
  }

  /**
   * Get the bean definition for 'webEndpointDiscoverer'.
   */
  public static BeanDefinition getWebEndpointDiscovererBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(WebEndpointDiscoverer.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration");
    beanDefinition.setInstanceSupplier(getWebEndpointDiscovererInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'controllerEndpointDiscoverer'.
   */
  @SuppressWarnings("removal")
  private static BeanInstanceSupplier<ControllerEndpointDiscoverer> getControllerEndpointDiscovererInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ControllerEndpointDiscoverer>forFactoryMethod(WebEndpointAutoConfiguration.class, "controllerEndpointDiscoverer", ObjectProvider.class, ObjectProvider.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration", WebEndpointAutoConfiguration.class).controllerEndpointDiscoverer(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'controllerEndpointDiscoverer'.
   */
  @SuppressWarnings("removal")
  public static BeanDefinition getControllerEndpointDiscovererBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ControllerEndpointDiscoverer.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration");
    beanDefinition.setInstanceSupplier(getControllerEndpointDiscovererInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'pathMappedEndpoints'.
   */
  private static BeanInstanceSupplier<PathMappedEndpoints> getPathMappedEndpointsInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<PathMappedEndpoints>forFactoryMethod(WebEndpointAutoConfiguration.class, "pathMappedEndpoints", Collection.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration", WebEndpointAutoConfiguration.class).pathMappedEndpoints(args.get(0)));
  }

  /**
   * Get the bean definition for 'pathMappedEndpoints'.
   */
  public static BeanDefinition getPathMappedEndpointsBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(PathMappedEndpoints.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration");
    beanDefinition.setInstanceSupplier(getPathMappedEndpointsInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'webExposeExcludePropertyEndpointFilter'.
   */
  private static BeanInstanceSupplier<IncludeExcludeEndpointFilter> getWebExposeExcludePropertyEndpointFilterInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<IncludeExcludeEndpointFilter>forFactoryMethod(WebEndpointAutoConfiguration.class, "webExposeExcludePropertyEndpointFilter")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration", WebEndpointAutoConfiguration.class).webExposeExcludePropertyEndpointFilter());
  }

  /**
   * Get the bean definition for 'webExposeExcludePropertyEndpointFilter'.
   */
  public static BeanDefinition getWebExposeExcludePropertyEndpointFilterBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(IncludeExcludeEndpointFilter.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(IncludeExcludeEndpointFilter.class, ExposableWebEndpoint.class));
    beanDefinition.setFactoryBeanName("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration");
    beanDefinition.setInstanceSupplier(getWebExposeExcludePropertyEndpointFilterInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'controllerExposeExcludePropertyEndpointFilter'.
   */
  private static BeanInstanceSupplier<IncludeExcludeEndpointFilter> getControllerExposeExcludePropertyEndpointFilterInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<IncludeExcludeEndpointFilter>forFactoryMethod(WebEndpointAutoConfiguration.class, "controllerExposeExcludePropertyEndpointFilter")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration", WebEndpointAutoConfiguration.class).controllerExposeExcludePropertyEndpointFilter());
  }

  /**
   * Get the bean definition for 'controllerExposeExcludePropertyEndpointFilter'.
   */
  @SuppressWarnings("removal")
  public static BeanDefinition getControllerExposeExcludePropertyEndpointFilterBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(IncludeExcludeEndpointFilter.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(IncludeExcludeEndpointFilter.class, ExposableControllerEndpoint.class));
    beanDefinition.setFactoryBeanName("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration");
    beanDefinition.setInstanceSupplier(getControllerExposeExcludePropertyEndpointFilterInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'webAccessPropertiesOperationFilter'.
   */
  private static BeanInstanceSupplier<OperationFilter> getWebAccessPropertiesOperationFilterInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<OperationFilter>forFactoryMethod(WebEndpointAutoConfiguration.class, "webAccessPropertiesOperationFilter", EndpointAccessResolver.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration", WebEndpointAutoConfiguration.class).webAccessPropertiesOperationFilter(args.get(0)));
  }

  /**
   * Get the bean definition for 'webAccessPropertiesOperationFilter'.
   */
  public static BeanDefinition getWebAccessPropertiesOperationFilterBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(OperationFilter.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(OperationFilter.class, WebOperation.class));
    beanDefinition.setFactoryBeanName("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration");
    beanDefinition.setInstanceSupplier(getWebAccessPropertiesOperationFilterInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Bean definitions for {@link WebEndpointAutoConfiguration.WebEndpointServletConfiguration}.
   */
  @Generated
  public static class WebEndpointServletConfiguration {
    /**
     * Get the bean definition for 'webEndpointServletConfiguration'.
     */
    public static BeanDefinition getWebEndpointServletConfigurationBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(WebEndpointAutoConfiguration.WebEndpointServletConfiguration.class);
      beanDefinition.setInstanceSupplier(WebEndpointAutoConfiguration.WebEndpointServletConfiguration::new);
      return beanDefinition;
    }

    /**
     * Get the bean instance supplier for 'servletEndpointDiscoverer'.
     */
    @SuppressWarnings("removal")
    private static BeanInstanceSupplier<ServletEndpointDiscoverer> getServletEndpointDiscovererInstanceSupplier(
        ) {
      return BeanInstanceSupplier.<ServletEndpointDiscoverer>forFactoryMethod(WebEndpointAutoConfiguration.WebEndpointServletConfiguration.class, "servletEndpointDiscoverer", ApplicationContext.class, ObjectProvider.class, ObjectProvider.class)
              .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration$WebEndpointServletConfiguration", WebEndpointAutoConfiguration.WebEndpointServletConfiguration.class).servletEndpointDiscoverer(args.get(0), args.get(1), args.get(2)));
    }

    /**
     * Get the bean definition for 'servletEndpointDiscoverer'.
     */
    @SuppressWarnings("removal")
    public static BeanDefinition getServletEndpointDiscovererBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(ServletEndpointDiscoverer.class);
      beanDefinition.setFactoryBeanName("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration$WebEndpointServletConfiguration");
      beanDefinition.setInstanceSupplier(getServletEndpointDiscovererInstanceSupplier());
      return beanDefinition;
    }
  }
}
