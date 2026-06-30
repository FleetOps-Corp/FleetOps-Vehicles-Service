package io.github.resilience4j.springboot3.micrometer.autoconfigure;

import io.github.resilience4j.common.CompositeCustomizer;
import io.github.resilience4j.common.micrometer.configuration.TimerConfigCustomizer;
import io.github.resilience4j.consumer.EventConsumerRegistry;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.micrometer.Timer;
import io.github.resilience4j.micrometer.TimerRegistry;
import io.github.resilience4j.micrometer.event.TimerEvent;
import io.github.resilience4j.spring6.fallback.FallbackExecutor;
import io.github.resilience4j.spring6.micrometer.configure.TimerAspect;
import io.github.resilience4j.spring6.micrometer.configure.TimerConfigurationProperties;
import io.github.resilience4j.spring6.spelresolver.SpelResolver;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Optional;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;
import org.springframework.core.ResolvableType;

/**
 * Bean definitions for {@link TimerConfigurationOnMissingBean}.
 */
@Generated
public class TimerConfigurationOnMissingBean__BeanDefinitions {
  /**
   * Get the bean definition for 'timerConfigurationOnMissingBean'.
   */
  public static BeanDefinition getTimerConfigurationOnMissingBeanBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TimerConfigurationOnMissingBean.class);
    beanDefinition.setTargetType(TimerConfigurationOnMissingBean.class);
    ConfigurationClassUtils.initializeConfigurationClass(TimerConfigurationOnMissingBean.class);
    beanDefinition.setInstanceSupplier(TimerConfigurationOnMissingBean$$SpringCGLIB$$0::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'timerEventsConsumerRegistry'.
   */
  private static BeanInstanceSupplier<EventConsumerRegistry> getTimerEventsConsumerRegistryInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<EventConsumerRegistry>forFactoryMethod(TimerConfigurationOnMissingBean$$SpringCGLIB$$0.class, "timerEventsConsumerRegistry")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.micrometer.autoconfigure.TimerConfigurationOnMissingBean", TimerConfigurationOnMissingBean.class).timerEventsConsumerRegistry());
  }

  /**
   * Get the bean definition for 'timerEventsConsumerRegistry'.
   */
  public static BeanDefinition getTimerEventsConsumerRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(EventConsumerRegistry.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(EventConsumerRegistry.class, TimerEvent.class));
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.micrometer.autoconfigure.TimerConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getTimerEventsConsumerRegistryInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'compositeTimerCustomizer'.
   */
  private static BeanInstanceSupplier<CompositeCustomizer> getCompositeTimerCustomizerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<CompositeCustomizer>forFactoryMethod(TimerConfigurationOnMissingBean$$SpringCGLIB$$0.class, "compositeTimerCustomizer", List.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.micrometer.autoconfigure.TimerConfigurationOnMissingBean", TimerConfigurationOnMissingBean.class).compositeTimerCustomizer(args.get(0)));
  }

  /**
   * Get the bean definition for 'compositeTimerCustomizer'.
   */
  public static BeanDefinition getCompositeTimerCustomizerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CompositeCustomizer.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(CompositeCustomizer.class, TimerConfigCustomizer.class));
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.micrometer.autoconfigure.TimerConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getCompositeTimerCustomizerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'timerRegistry'.
   */
  private static BeanInstanceSupplier<TimerRegistry> getTimerRegistryInstanceSupplier() {
    return BeanInstanceSupplier.<TimerRegistry>forFactoryMethod(TimerConfigurationOnMissingBean$$SpringCGLIB$$0.class, "timerRegistry", TimerConfigurationProperties.class, EventConsumerRegistry.class, RegistryEventConsumer.class, CompositeCustomizer.class, MeterRegistry.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.micrometer.autoconfigure.TimerConfigurationOnMissingBean", TimerConfigurationOnMissingBean.class).timerRegistry(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4)));
  }

  /**
   * Get the bean definition for 'timerRegistry'.
   */
  public static BeanDefinition getTimerRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TimerRegistry.class);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.micrometer.autoconfigure.TimerConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getTimerRegistryInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'timerRegistryEventConsumer'.
   */
  private static BeanInstanceSupplier<RegistryEventConsumer> getTimerRegistryEventConsumerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<RegistryEventConsumer>forFactoryMethod(TimerConfigurationOnMissingBean$$SpringCGLIB$$0.class, "timerRegistryEventConsumer", Optional.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.micrometer.autoconfigure.TimerConfigurationOnMissingBean", TimerConfigurationOnMissingBean.class).timerRegistryEventConsumer(args.get(0)));
  }

  /**
   * Get the bean definition for 'timerRegistryEventConsumer'.
   */
  public static BeanDefinition getTimerRegistryEventConsumerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RegistryEventConsumer.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(RegistryEventConsumer.class, Timer.class));
    beanDefinition.setPrimary(true);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.micrometer.autoconfigure.TimerConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getTimerRegistryEventConsumerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'timerAspect'.
   */
  private static BeanInstanceSupplier<TimerAspect> getTimerAspectInstanceSupplier() {
    return BeanInstanceSupplier.<TimerAspect>forFactoryMethod(TimerConfigurationOnMissingBean$$SpringCGLIB$$0.class, "timerAspect", TimerConfigurationProperties.class, TimerRegistry.class, List.class, FallbackExecutor.class, SpelResolver.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.micrometer.autoconfigure.TimerConfigurationOnMissingBean", TimerConfigurationOnMissingBean.class).timerAspect(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4)));
  }

  /**
   * Get the bean definition for 'timerAspect'.
   */
  public static BeanDefinition getTimerAspectBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TimerAspect.class);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.micrometer.autoconfigure.TimerConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getTimerAspectInstanceSupplier());
    return beanDefinition;
  }
}
