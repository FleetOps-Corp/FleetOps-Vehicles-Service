package org.springdoc.webmvc.ui;

import org.springdoc.core.events.SpringDocAppInitializer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;

/**
 * Bean definitions for {@link SwaggerConfig}.
 */
@Generated
public class SwaggerConfig__BeanDefinitions {
  /**
   * Get the bean definition for 'swaggerConfig'.
   */
  public static BeanDefinition getSwaggerConfigBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SwaggerConfig.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setInstanceSupplier(SwaggerConfig::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'swaggerWelcome'.
   */
  private static BeanInstanceSupplier<SwaggerWelcomeWebMvc> getSwaggerWelcomeInstanceSupplier() {
    return BeanInstanceSupplier.<SwaggerWelcomeWebMvc>forFactoryMethod(SwaggerConfig.class, "swaggerWelcome", SwaggerUiConfigProperties.class, SpringDocConfigProperties.class, ObjectProvider.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.webmvc.ui.SwaggerConfig", SwaggerConfig.class).swaggerWelcome(args.get(0), args.get(1), args.get(2)));
  }

  /**
   * Get the bean definition for 'swaggerWelcome'.
   */
  public static BeanDefinition getSwaggerWelcomeBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SwaggerWelcomeWebMvc.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.webmvc.ui.SwaggerConfig");
    InstanceSupplier<SwaggerWelcomeWebMvc> instanceSupplier = getSwaggerWelcomeInstanceSupplier();
    instanceSupplier = instanceSupplier.andThen(SwaggerWelcomeWebMvc__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'swaggerConfigResource'.
   */
  private static BeanInstanceSupplier<SwaggerConfigResource> getSwaggerConfigResourceInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<SwaggerConfigResource>forFactoryMethod(SwaggerConfig.class, "swaggerConfigResource", SwaggerWelcomeCommon.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.webmvc.ui.SwaggerConfig", SwaggerConfig.class).swaggerConfigResource(args.get(0)));
  }

  /**
   * Get the bean definition for 'swaggerConfigResource'.
   */
  public static BeanDefinition getSwaggerConfigResourceBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SwaggerConfigResource.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.webmvc.ui.SwaggerConfig");
    beanDefinition.setInstanceSupplier(getSwaggerConfigResourceInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'indexPageTransformer'.
   */
  private static BeanInstanceSupplier<SwaggerIndexTransformer> getIndexPageTransformerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<SwaggerIndexTransformer>forFactoryMethod(SwaggerConfig.class, "indexPageTransformer", SwaggerUiConfigProperties.class, SwaggerUiOAuthProperties.class, SwaggerWelcomeCommon.class, ObjectMapperProvider.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.webmvc.ui.SwaggerConfig", SwaggerConfig.class).indexPageTransformer(args.get(0), args.get(1), args.get(2), args.get(3)));
  }

  /**
   * Get the bean definition for 'indexPageTransformer'.
   */
  public static BeanDefinition getIndexPageTransformerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SwaggerIndexTransformer.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.webmvc.ui.SwaggerConfig");
    beanDefinition.setInstanceSupplier(getIndexPageTransformerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'swaggerWebMvcConfigurer'.
   */
  private static BeanInstanceSupplier<SwaggerWebMvcConfigurer> getSwaggerWebMvcConfigurerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<SwaggerWebMvcConfigurer>forFactoryMethod(SwaggerConfig.class, "swaggerWebMvcConfigurer", SwaggerUiConfigProperties.class, WebProperties.class, WebMvcProperties.class, SwaggerIndexTransformer.class, SwaggerResourceResolver.class, SwaggerWelcomeCommon.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.webmvc.ui.SwaggerConfig", SwaggerConfig.class).swaggerWebMvcConfigurer(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5)));
  }

  /**
   * Get the bean definition for 'swaggerWebMvcConfigurer'.
   */
  public static BeanDefinition getSwaggerWebMvcConfigurerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SwaggerWebMvcConfigurer.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.webmvc.ui.SwaggerConfig");
    beanDefinition.setInstanceSupplier(getSwaggerWebMvcConfigurerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'swaggerResourceResolver'.
   */
  private static BeanInstanceSupplier<SwaggerResourceResolver> getSwaggerResourceResolverInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<SwaggerResourceResolver>forFactoryMethod(SwaggerConfig.class, "swaggerResourceResolver", SwaggerUiConfigProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.webmvc.ui.SwaggerConfig", SwaggerConfig.class).swaggerResourceResolver(args.get(0)));
  }

  /**
   * Get the bean definition for 'swaggerResourceResolver'.
   */
  public static BeanDefinition getSwaggerResourceResolverBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SwaggerResourceResolver.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.webmvc.ui.SwaggerConfig");
    beanDefinition.setInstanceSupplier(getSwaggerResourceResolverInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'springDocSwaggerInitializer'.
   */
  private static BeanInstanceSupplier<SpringDocAppInitializer> getSpringDocSwaggerInitializerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<SpringDocAppInitializer>forFactoryMethod(SwaggerConfig.class, "springDocSwaggerInitializer", SwaggerUiConfigProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.webmvc.ui.SwaggerConfig", SwaggerConfig.class).springDocSwaggerInitializer(args.get(0)));
  }

  /**
   * Get the bean definition for 'springDocSwaggerInitializer'.
   */
  public static BeanDefinition getSpringDocSwaggerInitializerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SpringDocAppInitializer.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.webmvc.ui.SwaggerConfig");
    beanDefinition.setInstanceSupplier(getSpringDocSwaggerInitializerInstanceSupplier());
    return beanDefinition;
  }
}
