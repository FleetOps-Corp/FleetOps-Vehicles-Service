package org.springdoc.webmvc.core.configuration;

import java.util.List;
import java.util.Optional;
import org.springdoc.core.customizers.SpringDocCustomizers;
import org.springdoc.core.discoverer.SpringDocParameterNameDiscoverer;
import org.springdoc.core.extractor.MethodParameterPojoExtractor;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.SpringDocProviders;
import org.springdoc.core.providers.SpringWebProvider;
import org.springdoc.core.service.AbstractRequestService;
import org.springdoc.core.service.GenericParameterService;
import org.springdoc.core.service.GenericResponseService;
import org.springdoc.core.service.OperationService;
import org.springdoc.core.service.RequestBodyService;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springdoc.webmvc.core.providers.RouterFunctionWebMvcProvider;
import org.springdoc.webmvc.core.service.RequestService;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link SpringDocWebMvcConfiguration}.
 */
@Generated
public class SpringDocWebMvcConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'springDocWebMvcConfiguration'.
   */
  public static BeanDefinition getSpringDocWebMvcConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SpringDocWebMvcConfiguration.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setInstanceSupplier(SpringDocWebMvcConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'openApiResource'.
   */
  private static BeanInstanceSupplier<OpenApiWebMvcResource> getOpenApiResourceInstanceSupplier() {
    return BeanInstanceSupplier.<OpenApiWebMvcResource>forFactoryMethod(SpringDocWebMvcConfiguration.class, "openApiResource", ObjectFactory.class, AbstractRequestService.class, GenericResponseService.class, OperationService.class, SpringDocConfigProperties.class, SpringDocProviders.class, SpringDocCustomizers.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration", SpringDocWebMvcConfiguration.class).openApiResource(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5), args.get(6)));
  }

  /**
   * Get the bean definition for 'openApiResource'.
   */
  public static BeanDefinition getOpenApiResourceBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(OpenApiWebMvcResource.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration");
    beanDefinition.setInstanceSupplier(getOpenApiResourceInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'requestBuilder'.
   */
  private static BeanInstanceSupplier<RequestService> getRequestBuilderInstanceSupplier() {
    return BeanInstanceSupplier.<RequestService>forFactoryMethod(SpringDocWebMvcConfiguration.class, "requestBuilder", GenericParameterService.class, RequestBodyService.class, SpringDocCustomizers.class, SpringDocParameterNameDiscoverer.class, MethodParameterPojoExtractor.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration", SpringDocWebMvcConfiguration.class).requestBuilder(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4)));
  }

  /**
   * Get the bean definition for 'requestBuilder'.
   */
  public static BeanDefinition getRequestBuilderBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RequestService.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration");
    beanDefinition.setInstanceSupplier(getRequestBuilderInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'springWebProvider'.
   */
  private static BeanInstanceSupplier<SpringWebProvider> getSpringWebProviderInstanceSupplier() {
    return BeanInstanceSupplier.<SpringWebProvider>forFactoryMethod(SpringDocWebMvcConfiguration.class, "springWebProvider", Optional.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration", SpringDocWebMvcConfiguration.class).springWebProvider(args.get(0)));
  }

  /**
   * Get the bean definition for 'springWebProvider'.
   */
  public static BeanDefinition getSpringWebProviderBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SpringWebProvider.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration");
    beanDefinition.setInstanceSupplier(getSpringWebProviderInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'springDocApiVersionCustomizer'.
   */
  private static BeanInstanceSupplier<SmartInitializingSingleton> getSpringDocApiVersionCustomizerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<SmartInitializingSingleton>forFactoryMethod(SpringDocWebMvcConfiguration.class, "springDocApiVersionCustomizer", Optional.class, SpringDocConfigProperties.class, Optional.class, List.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration", SpringDocWebMvcConfiguration.class).springDocApiVersionCustomizer(args.get(0), args.get(1), args.get(2), args.get(3)));
  }

  /**
   * Get the bean definition for 'springDocApiVersionCustomizer'.
   */
  public static BeanDefinition getSpringDocApiVersionCustomizerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SmartInitializingSingleton.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration");
    beanDefinition.setInstanceSupplier(getSpringDocApiVersionCustomizerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'responseBuilder'.
   */
  private static BeanInstanceSupplier<GenericResponseService> getResponseBuilderInstanceSupplier() {
    return BeanInstanceSupplier.<GenericResponseService>forFactoryMethod(SpringDocWebMvcConfiguration.class, "responseBuilder", OperationService.class, SpringDocConfigProperties.class, PropertyResolverUtils.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration", SpringDocWebMvcConfiguration.class).responseBuilder(args.get(0), args.get(1), args.get(2)));
  }

  /**
   * Get the bean definition for 'responseBuilder'.
   */
  public static BeanDefinition getResponseBuilderBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(GenericResponseService.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration");
    beanDefinition.setInstanceSupplier(getResponseBuilderInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Bean definitions for {@link SpringDocWebMvcConfiguration.SpringDocWebMvcActuatorConfiguration}.
   */
  @Generated
  public static class SpringDocWebMvcActuatorConfiguration {
    /**
     * Get the bean definition for 'springDocWebMvcActuatorConfiguration'.
     */
    public static BeanDefinition getSpringDocWebMvcActuatorConfigurationBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(SpringDocWebMvcConfiguration.SpringDocWebMvcActuatorConfiguration.class);
      beanDefinition.setInstanceSupplier(SpringDocWebMvcConfiguration.SpringDocWebMvcActuatorConfiguration::new);
      return beanDefinition;
    }
  }

  /**
   * Bean definitions for {@link SpringDocWebMvcConfiguration.SpringDocWebMvcRouterConfiguration}.
   */
  @Generated
  public static class SpringDocWebMvcRouterConfiguration {
    /**
     * Get the bean definition for 'springDocWebMvcRouterConfiguration'.
     */
    public static BeanDefinition getSpringDocWebMvcRouterConfigurationBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(SpringDocWebMvcConfiguration.SpringDocWebMvcRouterConfiguration.class);
      beanDefinition.setInstanceSupplier(SpringDocWebMvcConfiguration.SpringDocWebMvcRouterConfiguration::new);
      return beanDefinition;
    }

    /**
     * Get the bean instance supplier for 'routerFunctionProvider'.
     */
    private static BeanInstanceSupplier<RouterFunctionWebMvcProvider> getRouterFunctionProviderInstanceSupplier(
        ) {
      return BeanInstanceSupplier.<RouterFunctionWebMvcProvider>forFactoryMethod(SpringDocWebMvcConfiguration.SpringDocWebMvcRouterConfiguration.class, "routerFunctionProvider")
              .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration$SpringDocWebMvcRouterConfiguration", SpringDocWebMvcConfiguration.SpringDocWebMvcRouterConfiguration.class).routerFunctionProvider());
    }

    /**
     * Get the bean definition for 'routerFunctionProvider'.
     */
    public static BeanDefinition getRouterFunctionProviderBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(RouterFunctionWebMvcProvider.class);
      beanDefinition.setLazyInit(false);
      beanDefinition.setFactoryBeanName("org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration$SpringDocWebMvcRouterConfiguration");
      beanDefinition.setInstanceSupplier(getRouterFunctionProviderInstanceSupplier());
      return beanDefinition;
    }
  }
}
