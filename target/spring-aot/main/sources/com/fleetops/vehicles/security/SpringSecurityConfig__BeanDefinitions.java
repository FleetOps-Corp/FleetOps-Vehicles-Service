package com.fleetops.vehicles.security;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Bean definitions for {@link SpringSecurityConfig}.
 */
@Generated
public class SpringSecurityConfig__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'springSecurityConfig'.
   */
  private static BeanInstanceSupplier<SpringSecurityConfig> getSpringSecurityConfigInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<SpringSecurityConfig>forConstructor(JwtValidationFilter.class, JwtAuthenticationEntryPoint.class)
            .withGenerator((registeredBean, args) -> new SpringSecurityConfig$$SpringCGLIB$$0(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'springSecurityConfig'.
   */
  public static BeanDefinition getSpringSecurityConfigBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SpringSecurityConfig.class);
    beanDefinition.setTargetType(SpringSecurityConfig.class);
    ConfigurationClassUtils.initializeConfigurationClass(SpringSecurityConfig.class);
    beanDefinition.setInstanceSupplier(getSpringSecurityConfigInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'securityFilterChain'.
   */
  private static BeanInstanceSupplier<SecurityFilterChain> getSecurityFilterChainInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<SecurityFilterChain>forFactoryMethod(SpringSecurityConfig$$SpringCGLIB$$0.class, "securityFilterChain", HttpSecurity.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("springSecurityConfig", SpringSecurityConfig.class).securityFilterChain(args.get(0)));
  }

  /**
   * Get the bean definition for 'securityFilterChain'.
   */
  public static BeanDefinition getSecurityFilterChainBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SecurityFilterChain.class);
    beanDefinition.setFactoryBeanName("springSecurityConfig");
    beanDefinition.setInstanceSupplier(getSecurityFilterChainInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'corsConfigurationSource'.
   */
  private static BeanInstanceSupplier<CorsConfigurationSource> getCorsConfigurationSourceInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<CorsConfigurationSource>forFactoryMethod(SpringSecurityConfig$$SpringCGLIB$$0.class, "corsConfigurationSource")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("springSecurityConfig", SpringSecurityConfig.class).corsConfigurationSource());
  }

  /**
   * Get the bean definition for 'corsConfigurationSource'.
   */
  public static BeanDefinition getCorsConfigurationSourceBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CorsConfigurationSource.class);
    beanDefinition.setFactoryBeanName("springSecurityConfig");
    beanDefinition.setInstanceSupplier(getCorsConfigurationSourceInstanceSupplier());
    return beanDefinition;
  }
}
