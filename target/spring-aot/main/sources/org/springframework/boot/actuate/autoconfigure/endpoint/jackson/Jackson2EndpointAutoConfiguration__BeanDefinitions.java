package org.springframework.boot.actuate.autoconfigure.endpoint.jackson;

import java.lang.SuppressWarnings;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.actuate.endpoint.jackson.EndpointJackson2ObjectMapper;

/**
 * Bean definitions for {@link Jackson2EndpointAutoConfiguration}.
 */
@Generated
public class Jackson2EndpointAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'jackson2EndpointAutoConfiguration'.
   */
  @SuppressWarnings("removal")
  public static BeanDefinition getJacksonEndpointAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(Jackson2EndpointAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(Jackson2EndpointAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'jackson2EndpointJsonMapper'.
   */
  @SuppressWarnings("removal")
  private static BeanInstanceSupplier<EndpointJackson2ObjectMapper> getJacksonEndpointJsonMapperInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<EndpointJackson2ObjectMapper>forFactoryMethod(Jackson2EndpointAutoConfiguration.class, "jackson2EndpointJsonMapper")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.actuate.autoconfigure.endpoint.jackson.Jackson2EndpointAutoConfiguration", Jackson2EndpointAutoConfiguration.class).jackson2EndpointJsonMapper());
  }

  /**
   * Get the bean definition for 'jackson2EndpointJsonMapper'.
   */
  @SuppressWarnings("removal")
  public static BeanDefinition getJacksonEndpointJsonMapperBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(EndpointJackson2ObjectMapper.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.actuate.autoconfigure.endpoint.jackson.Jackson2EndpointAutoConfiguration");
    beanDefinition.setInstanceSupplier(getJacksonEndpointJsonMapperInstanceSupplier());
    return beanDefinition;
  }
}
