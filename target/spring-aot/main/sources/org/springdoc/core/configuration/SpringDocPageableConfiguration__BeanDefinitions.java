package org.springdoc.core.configuration;

import java.util.Optional;
import org.springdoc.core.converters.PageOpenAPIConverter;
import org.springdoc.core.converters.PageableOpenAPIConverter;
import org.springdoc.core.customizers.DataRestDelegatingMethodParameterCustomizer;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link SpringDocPageableConfiguration}.
 */
@Generated
public class SpringDocPageableConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'springDocPageableConfiguration'.
   */
  public static BeanDefinition getSpringDocPageableConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SpringDocPageableConfiguration.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setInstanceSupplier(SpringDocPageableConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'pageableOpenAPIConverter'.
   */
  private static BeanInstanceSupplier<PageableOpenAPIConverter> getPageableOpenAPIConverterInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<PageableOpenAPIConverter>forFactoryMethod(SpringDocPageableConfiguration.class, "pageableOpenAPIConverter", ObjectMapperProvider.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.core.configuration.SpringDocPageableConfiguration", SpringDocPageableConfiguration.class).pageableOpenAPIConverter(args.get(0)));
  }

  /**
   * Get the bean definition for 'pageableOpenAPIConverter'.
   */
  public static BeanDefinition getPageableOpenAPIConverterBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(PageableOpenAPIConverter.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.core.configuration.SpringDocPageableConfiguration");
    beanDefinition.setInstanceSupplier(getPageableOpenAPIConverterInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'pageOpenAPIConverter'.
   */
  private static BeanInstanceSupplier<PageOpenAPIConverter> getPageOpenAPIConverterInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<PageOpenAPIConverter>forFactoryMethod(SpringDocPageableConfiguration.class, "pageOpenAPIConverter", Optional.class, ObjectMapperProvider.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.core.configuration.SpringDocPageableConfiguration", SpringDocPageableConfiguration.class).pageOpenAPIConverter(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'pageOpenAPIConverter'.
   */
  public static BeanDefinition getPageOpenAPIConverterBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(PageOpenAPIConverter.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.core.configuration.SpringDocPageableConfiguration");
    beanDefinition.setInstanceSupplier(getPageOpenAPIConverterInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'dataRestDelegatingMethodParameterCustomizer'.
   */
  private static BeanInstanceSupplier<DataRestDelegatingMethodParameterCustomizer> getDataRestDelegatingMethodParameterCustomizerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<DataRestDelegatingMethodParameterCustomizer>forFactoryMethod(SpringDocPageableConfiguration.class, "dataRestDelegatingMethodParameterCustomizer", Optional.class, Optional.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.core.configuration.SpringDocPageableConfiguration", SpringDocPageableConfiguration.class).dataRestDelegatingMethodParameterCustomizer(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'dataRestDelegatingMethodParameterCustomizer'.
   */
  public static BeanDefinition getDataRestDelegatingMethodParameterCustomizerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DataRestDelegatingMethodParameterCustomizer.class);
    beanDefinition.setLazyInit(false);
    beanDefinition.setFactoryBeanName("org.springdoc.core.configuration.SpringDocPageableConfiguration");
    beanDefinition.setInstanceSupplier(getDataRestDelegatingMethodParameterCustomizerInstanceSupplier());
    return beanDefinition;
  }
}
