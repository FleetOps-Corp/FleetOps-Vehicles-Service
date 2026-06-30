package io.github.resilience4j.springboot3.circuitbreaker.autoconfigure;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import io.github.resilience4j.common.CompositeCustomizer;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.github.resilience4j.consumer.EventConsumerRegistry;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.spring6.circuitbreaker.configure.CircuitBreakerAspect;
import io.github.resilience4j.spring6.circuitbreaker.configure.CircuitBreakerConfigurationProperties;
import io.github.resilience4j.spring6.fallback.FallbackExecutor;
import io.github.resilience4j.spring6.spelresolver.SpelResolver;
import java.util.List;
import java.util.Optional;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;
import org.springframework.core.ResolvableType;

/**
 * Bean definitions for {@link CircuitBreakerConfigurationOnMissingBean}.
 */
@Generated
public class CircuitBreakerConfigurationOnMissingBean__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerConfigurationOnMissingBean'.
   */
  private static BeanInstanceSupplier<CircuitBreakerConfigurationOnMissingBean> getCircuitBreakerConfigurationOnMissingBeanInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<CircuitBreakerConfigurationOnMissingBean>forConstructor(CircuitBreakerConfigurationProperties.class)
            .withGenerator((registeredBean, args) -> new CircuitBreakerConfigurationOnMissingBean$$SpringCGLIB$$0(args.get(0)));
  }

  /**
   * Get the bean definition for 'circuitBreakerConfigurationOnMissingBean'.
   */
  public static BeanDefinition getCircuitBreakerConfigurationOnMissingBeanBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CircuitBreakerConfigurationOnMissingBean.class);
    beanDefinition.setTargetType(CircuitBreakerConfigurationOnMissingBean.class);
    ConfigurationClassUtils.initializeConfigurationClass(CircuitBreakerConfigurationOnMissingBean.class);
    beanDefinition.setInstanceSupplier(getCircuitBreakerConfigurationOnMissingBeanInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'eventConsumerRegistry'.
   */
  private static BeanInstanceSupplier<EventConsumerRegistry> getEventConsumerRegistryInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<EventConsumerRegistry>forFactoryMethod(CircuitBreakerConfigurationOnMissingBean$$SpringCGLIB$$0.class, "eventConsumerRegistry")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerConfigurationOnMissingBean", CircuitBreakerConfigurationOnMissingBean.class).eventConsumerRegistry());
  }

  /**
   * Get the bean definition for 'eventConsumerRegistry'.
   */
  public static BeanDefinition getEventConsumerRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(EventConsumerRegistry.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(EventConsumerRegistry.class, CircuitBreakerEvent.class));
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getEventConsumerRegistryInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'compositeCircuitBreakerCustomizer'.
   */
  private static BeanInstanceSupplier<CompositeCustomizer> getCompositeCircuitBreakerCustomizerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<CompositeCustomizer>forFactoryMethod(CircuitBreakerConfigurationOnMissingBean$$SpringCGLIB$$0.class, "compositeCircuitBreakerCustomizer", List.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerConfigurationOnMissingBean", CircuitBreakerConfigurationOnMissingBean.class).compositeCircuitBreakerCustomizer(args.get(0)));
  }

  /**
   * Get the bean definition for 'compositeCircuitBreakerCustomizer'.
   */
  public static BeanDefinition getCompositeCircuitBreakerCustomizerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CompositeCustomizer.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(CompositeCustomizer.class, CircuitBreakerConfigCustomizer.class));
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getCompositeCircuitBreakerCustomizerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'circuitBreakerRegistryEventConsumer'.
   */
  private static BeanInstanceSupplier<RegistryEventConsumer> getCircuitBreakerRegistryEventConsumerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<RegistryEventConsumer>forFactoryMethod(CircuitBreakerConfigurationOnMissingBean$$SpringCGLIB$$0.class, "circuitBreakerRegistryEventConsumer", Optional.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerConfigurationOnMissingBean", CircuitBreakerConfigurationOnMissingBean.class).circuitBreakerRegistryEventConsumer(args.get(0)));
  }

  /**
   * Get the bean definition for 'circuitBreakerRegistryEventConsumer'.
   */
  public static BeanDefinition getCircuitBreakerRegistryEventConsumerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RegistryEventConsumer.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(RegistryEventConsumer.class, CircuitBreaker.class));
    beanDefinition.setPrimary(true);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getCircuitBreakerRegistryEventConsumerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'circuitBreakerAspect'.
   */
  private static BeanInstanceSupplier<CircuitBreakerAspect> getCircuitBreakerAspectInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<CircuitBreakerAspect>forFactoryMethod(CircuitBreakerConfigurationOnMissingBean$$SpringCGLIB$$0.class, "circuitBreakerAspect", CircuitBreakerRegistry.class, List.class, FallbackExecutor.class, SpelResolver.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerConfigurationOnMissingBean", CircuitBreakerConfigurationOnMissingBean.class).circuitBreakerAspect(args.get(0), args.get(1), args.get(2), args.get(3)));
  }

  /**
   * Get the bean definition for 'circuitBreakerAspect'.
   */
  public static BeanDefinition getCircuitBreakerAspectBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CircuitBreakerAspect.class);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getCircuitBreakerAspectInstanceSupplier());
    return beanDefinition;
  }
}
