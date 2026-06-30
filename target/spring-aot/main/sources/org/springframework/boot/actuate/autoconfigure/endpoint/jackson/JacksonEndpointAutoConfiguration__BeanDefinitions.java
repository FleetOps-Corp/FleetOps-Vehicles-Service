package org.springframework.boot.actuate.autoconfigure.endpoint.jackson;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.actuate.endpoint.jackson.EndpointJsonMapper;

/**
 * Bean definitions for {@link JacksonEndpointAutoConfiguration}.
 */
@Generated
public class JacksonEndpointAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'jacksonEndpointAutoConfiguration'.
   */
  public static BeanDefinition getJacksonEndpointAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(JacksonEndpointAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(JacksonEndpointAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'endpointJsonMapper'.
   */
  private static BeanInstanceSupplier<EndpointJsonMapper> getEndpointJsonMapperInstanceSupplier() {
    return BeanInstanceSupplier.<EndpointJsonMapper>forFactoryMethod(JacksonEndpointAutoConfiguration.class, "endpointJsonMapper")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.actuate.autoconfigure.endpoint.jackson.JacksonEndpointAutoConfiguration", JacksonEndpointAutoConfiguration.class).endpointJsonMapper());
  }

  /**
   * Get the bean definition for 'endpointJsonMapper'.
   */
  public static BeanDefinition getEndpointJsonMapperBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(EndpointJsonMapper.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.actuate.autoconfigure.endpoint.jackson.JacksonEndpointAutoConfiguration");
    beanDefinition.setInstanceSupplier(getEndpointJsonMapperInstanceSupplier());
    return beanDefinition;
  }
}
