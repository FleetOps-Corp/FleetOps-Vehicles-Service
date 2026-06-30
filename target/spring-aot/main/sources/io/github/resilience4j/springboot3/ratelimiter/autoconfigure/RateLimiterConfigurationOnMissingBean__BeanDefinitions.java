package io.github.resilience4j.springboot3.ratelimiter.autoconfigure;

import io.github.resilience4j.common.CompositeCustomizer;
import io.github.resilience4j.common.ratelimiter.configuration.RateLimiterConfigCustomizer;
import io.github.resilience4j.consumer.EventConsumerRegistry;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.event.RateLimiterEvent;
import io.github.resilience4j.spring6.fallback.FallbackExecutor;
import io.github.resilience4j.spring6.ratelimiter.configure.RateLimiterAspect;
import io.github.resilience4j.spring6.ratelimiter.configure.RateLimiterConfigurationProperties;
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
 * Bean definitions for {@link RateLimiterConfigurationOnMissingBean}.
 */
@Generated
public class RateLimiterConfigurationOnMissingBean__BeanDefinitions {
  /**
   * Get the bean definition for 'rateLimiterConfigurationOnMissingBean'.
   */
  public static BeanDefinition getRateLimiterConfigurationOnMissingBeanBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RateLimiterConfigurationOnMissingBean.class);
    beanDefinition.setTargetType(RateLimiterConfigurationOnMissingBean.class);
    ConfigurationClassUtils.initializeConfigurationClass(RateLimiterConfigurationOnMissingBean.class);
    beanDefinition.setInstanceSupplier(RateLimiterConfigurationOnMissingBean$$SpringCGLIB$$0::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'rateLimiterEventsConsumerRegistry'.
   */
  private static BeanInstanceSupplier<EventConsumerRegistry> getRateLimiterEventsConsumerRegistryInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<EventConsumerRegistry>forFactoryMethod(RateLimiterConfigurationOnMissingBean$$SpringCGLIB$$0.class, "rateLimiterEventsConsumerRegistry")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterConfigurationOnMissingBean", RateLimiterConfigurationOnMissingBean.class).rateLimiterEventsConsumerRegistry());
  }

  /**
   * Get the bean definition for 'rateLimiterEventsConsumerRegistry'.
   */
  public static BeanDefinition getRateLimiterEventsConsumerRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(EventConsumerRegistry.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(EventConsumerRegistry.class, RateLimiterEvent.class));
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getRateLimiterEventsConsumerRegistryInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'compositeRateLimiterCustomizer'.
   */
  private static BeanInstanceSupplier<CompositeCustomizer> getCompositeRateLimiterCustomizerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<CompositeCustomizer>forFactoryMethod(RateLimiterConfigurationOnMissingBean$$SpringCGLIB$$0.class, "compositeRateLimiterCustomizer", List.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterConfigurationOnMissingBean", RateLimiterConfigurationOnMissingBean.class).compositeRateLimiterCustomizer(args.get(0)));
  }

  /**
   * Get the bean definition for 'compositeRateLimiterCustomizer'.
   */
  public static BeanDefinition getCompositeRateLimiterCustomizerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CompositeCustomizer.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(CompositeCustomizer.class, RateLimiterConfigCustomizer.class));
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getCompositeRateLimiterCustomizerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'rateLimiterRegistry'.
   */
  private static BeanInstanceSupplier<RateLimiterRegistry> getRateLimiterRegistryInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<RateLimiterRegistry>forFactoryMethod(RateLimiterConfigurationOnMissingBean$$SpringCGLIB$$0.class, "rateLimiterRegistry", RateLimiterConfigurationProperties.class, EventConsumerRegistry.class, RegistryEventConsumer.class, CompositeCustomizer.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterConfigurationOnMissingBean", RateLimiterConfigurationOnMissingBean.class).rateLimiterRegistry(args.get(0), args.get(1), args.get(2), args.get(3)));
  }

  /**
   * Get the bean definition for 'rateLimiterRegistry'.
   */
  public static BeanDefinition getRateLimiterRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RateLimiterRegistry.class);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getRateLimiterRegistryInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'rateLimiterRegistryEventConsumer'.
   */
  private static BeanInstanceSupplier<RegistryEventConsumer> getRateLimiterRegistryEventConsumerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<RegistryEventConsumer>forFactoryMethod(RateLimiterConfigurationOnMissingBean$$SpringCGLIB$$0.class, "rateLimiterRegistryEventConsumer", Optional.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterConfigurationOnMissingBean", RateLimiterConfigurationOnMissingBean.class).rateLimiterRegistryEventConsumer(args.get(0)));
  }

  /**
   * Get the bean definition for 'rateLimiterRegistryEventConsumer'.
   */
  public static BeanDefinition getRateLimiterRegistryEventConsumerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RegistryEventConsumer.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(RegistryEventConsumer.class, RateLimiter.class));
    beanDefinition.setPrimary(true);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getRateLimiterRegistryEventConsumerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'rateLimiterAspect'.
   */
  private static BeanInstanceSupplier<RateLimiterAspect> getRateLimiterAspectInstanceSupplier() {
    return BeanInstanceSupplier.<RateLimiterAspect>forFactoryMethod(RateLimiterConfigurationOnMissingBean$$SpringCGLIB$$0.class, "rateLimiterAspect", RateLimiterConfigurationProperties.class, RateLimiterRegistry.class, List.class, FallbackExecutor.class, SpelResolver.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterConfigurationOnMissingBean", RateLimiterConfigurationOnMissingBean.class).rateLimiterAspect(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4)));
  }

  /**
   * Get the bean definition for 'rateLimiterAspect'.
   */
  public static BeanDefinition getRateLimiterAspectBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RateLimiterAspect.class);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getRateLimiterAspectInstanceSupplier());
    return beanDefinition;
  }
}
