package io.github.resilience4j.springboot3.timelimiter.autoconfigure;

import io.github.resilience4j.common.CompositeCustomizer;
import io.github.resilience4j.common.timelimiter.configuration.TimeLimiterConfigCustomizer;
import io.github.resilience4j.consumer.EventConsumerRegistry;
import io.github.resilience4j.core.ContextAwareScheduledThreadPoolExecutor;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.spring6.fallback.FallbackExecutor;
import io.github.resilience4j.spring6.spelresolver.SpelResolver;
import io.github.resilience4j.spring6.timelimiter.configure.TimeLimiterAspect;
import io.github.resilience4j.spring6.timelimiter.configure.TimeLimiterConfigurationProperties;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.event.TimeLimiterEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;
import org.springframework.core.ResolvableType;

/**
 * Bean definitions for {@link TimeLimiterConfigurationOnMissingBean}.
 */
@Generated
public class TimeLimiterConfigurationOnMissingBean__BeanDefinitions {
  /**
   * Get the bean definition for 'timeLimiterConfigurationOnMissingBean'.
   */
  public static BeanDefinition getTimeLimiterConfigurationOnMissingBeanBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TimeLimiterConfigurationOnMissingBean.class);
    beanDefinition.setTargetType(TimeLimiterConfigurationOnMissingBean.class);
    ConfigurationClassUtils.initializeConfigurationClass(TimeLimiterConfigurationOnMissingBean.class);
    beanDefinition.setInstanceSupplier(TimeLimiterConfigurationOnMissingBean$$SpringCGLIB$$0::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'timeLimiterEventsConsumerRegistry'.
   */
  private static BeanInstanceSupplier<EventConsumerRegistry> getTimeLimiterEventsConsumerRegistryInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<EventConsumerRegistry>forFactoryMethod(TimeLimiterConfigurationOnMissingBean$$SpringCGLIB$$0.class, "timeLimiterEventsConsumerRegistry")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterConfigurationOnMissingBean", TimeLimiterConfigurationOnMissingBean.class).timeLimiterEventsConsumerRegistry());
  }

  /**
   * Get the bean definition for 'timeLimiterEventsConsumerRegistry'.
   */
  public static BeanDefinition getTimeLimiterEventsConsumerRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(EventConsumerRegistry.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(EventConsumerRegistry.class, TimeLimiterEvent.class));
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getTimeLimiterEventsConsumerRegistryInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'compositeTimeLimiterCustomizer'.
   */
  private static BeanInstanceSupplier<CompositeCustomizer> getCompositeTimeLimiterCustomizerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<CompositeCustomizer>forFactoryMethod(TimeLimiterConfigurationOnMissingBean$$SpringCGLIB$$0.class, "compositeTimeLimiterCustomizer", List.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterConfigurationOnMissingBean", TimeLimiterConfigurationOnMissingBean.class).compositeTimeLimiterCustomizer(args.get(0)));
  }

  /**
   * Get the bean definition for 'compositeTimeLimiterCustomizer'.
   */
  public static BeanDefinition getCompositeTimeLimiterCustomizerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CompositeCustomizer.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(CompositeCustomizer.class, TimeLimiterConfigCustomizer.class));
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getCompositeTimeLimiterCustomizerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'timeLimiterRegistry'.
   */
  private static BeanInstanceSupplier<TimeLimiterRegistry> getTimeLimiterRegistryInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<TimeLimiterRegistry>forFactoryMethod(TimeLimiterConfigurationOnMissingBean$$SpringCGLIB$$0.class, "timeLimiterRegistry", TimeLimiterConfigurationProperties.class, EventConsumerRegistry.class, RegistryEventConsumer.class, CompositeCustomizer.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterConfigurationOnMissingBean", TimeLimiterConfigurationOnMissingBean.class).timeLimiterRegistry(args.get(0), args.get(1), args.get(2), args.get(3)));
  }

  /**
   * Get the bean definition for 'timeLimiterRegistry'.
   */
  public static BeanDefinition getTimeLimiterRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TimeLimiterRegistry.class);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getTimeLimiterRegistryInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'timeLimiterRegistryEventConsumer'.
   */
  private static BeanInstanceSupplier<RegistryEventConsumer> getTimeLimiterRegistryEventConsumerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<RegistryEventConsumer>forFactoryMethod(TimeLimiterConfigurationOnMissingBean$$SpringCGLIB$$0.class, "timeLimiterRegistryEventConsumer", Optional.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterConfigurationOnMissingBean", TimeLimiterConfigurationOnMissingBean.class).timeLimiterRegistryEventConsumer(args.get(0)));
  }

  /**
   * Get the bean definition for 'timeLimiterRegistryEventConsumer'.
   */
  public static BeanDefinition getTimeLimiterRegistryEventConsumerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RegistryEventConsumer.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(RegistryEventConsumer.class, TimeLimiter.class));
    beanDefinition.setPrimary(true);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getTimeLimiterRegistryEventConsumerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'timeLimiterAspect'.
   */
  private static BeanInstanceSupplier<TimeLimiterAspect> getTimeLimiterAspectInstanceSupplier() {
    return BeanInstanceSupplier.<TimeLimiterAspect>forFactoryMethod(TimeLimiterConfigurationOnMissingBean$$SpringCGLIB$$0.class, "timeLimiterAspect", TimeLimiterConfigurationProperties.class, TimeLimiterRegistry.class, List.class, FallbackExecutor.class, SpelResolver.class, ContextAwareScheduledThreadPoolExecutor.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterConfigurationOnMissingBean", TimeLimiterConfigurationOnMissingBean.class).timeLimiterAspect(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5)));
  }

  /**
   * Get the bean definition for 'timeLimiterAspect'.
   */
  public static BeanDefinition getTimeLimiterAspectBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TimeLimiterAspect.class);
    beanDefinition.setDestroyMethodNames("close");
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getTimeLimiterAspectInstanceSupplier());
    return beanDefinition;
  }
}
