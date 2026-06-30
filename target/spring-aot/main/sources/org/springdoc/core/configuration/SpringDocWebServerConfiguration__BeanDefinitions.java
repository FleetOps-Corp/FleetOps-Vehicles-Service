package org.springdoc.core.configuration;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link SpringDocWebServerConfiguration}.
 */
@Generated
public class SpringDocWebServerConfiguration__BeanDefinitions {
  /**
   * Bean definitions for {@link SpringDocWebServerConfiguration.EmbeddedWebServerConfiguration}.
   */
  @Generated
  public static class EmbeddedWebServerConfiguration {
    /**
     * Get the bean definition for 'embeddedWebServerConfiguration'.
     */
    public static BeanDefinition getEmbeddedWebServerConfigurationBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(SpringDocWebServerConfiguration.EmbeddedWebServerConfiguration.class);
      beanDefinition.setInstanceSupplier(SpringDocWebServerConfiguration.EmbeddedWebServerConfiguration::new);
      return beanDefinition;
    }

    /**
     * Get the bean instance supplier for 'webServerPortProvider'.
     */
    private static BeanInstanceSupplier<SpringDocWebServerConfiguration.SpringDocWebServerContext> getWebServerPortProviderInstanceSupplier(
        ) {
      return BeanInstanceSupplier.<SpringDocWebServerConfiguration.SpringDocWebServerContext>forFactoryMethod(SpringDocWebServerConfiguration.EmbeddedWebServerConfiguration.class, "webServerPortProvider", ObjectProvider.class)
              .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springdoc.core.configuration.SpringDocWebServerConfiguration$EmbeddedWebServerConfiguration", SpringDocWebServerConfiguration.EmbeddedWebServerConfiguration.class).webServerPortProvider(args.get(0)));
    }

    /**
     * Get the bean definition for 'webServerPortProvider'.
     */
    public static BeanDefinition getWebServerPortProviderBeanDefinition() {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(SpringDocWebServerConfiguration.SpringDocWebServerContext.class);
      beanDefinition.setFactoryBeanName("org.springdoc.core.configuration.SpringDocWebServerConfiguration$EmbeddedWebServerConfiguration");
      beanDefinition.setInstanceSupplier(getWebServerPortProviderInstanceSupplier());
      return beanDefinition;
    }
  }
}
