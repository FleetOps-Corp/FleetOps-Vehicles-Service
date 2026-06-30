package io.github.resilience4j.springboot3.bulkhead.autoconfigure;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.bulkhead.event.BulkheadEvent;
import io.github.resilience4j.common.CompositeCustomizer;
import io.github.resilience4j.common.bulkhead.configuration.BulkheadConfigCustomizer;
import io.github.resilience4j.common.bulkhead.configuration.CommonThreadPoolBulkheadConfigurationProperties;
import io.github.resilience4j.common.bulkhead.configuration.ThreadPoolBulkheadConfigCustomizer;
import io.github.resilience4j.consumer.EventConsumerRegistry;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.spring6.bulkhead.configure.BulkheadAspect;
import io.github.resilience4j.spring6.bulkhead.configure.BulkheadConfigurationProperties;
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
 * Bean definitions for {@link BulkheadConfigurationOnMissingBean}.
 */
@Generated
public class BulkheadConfigurationOnMissingBean__BeanDefinitions {
  /**
   * Get the bean definition for 'bulkheadConfigurationOnMissingBean'.
   */
  public static BeanDefinition getBulkheadConfigurationOnMissingBeanBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(BulkheadConfigurationOnMissingBean.class);
    beanDefinition.setTargetType(BulkheadConfigurationOnMissingBean.class);
    ConfigurationClassUtils.initializeConfigurationClass(BulkheadConfigurationOnMissingBean.class);
    beanDefinition.setInstanceSupplier(BulkheadConfigurationOnMissingBean$$SpringCGLIB$$0::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'bulkheadEventConsumerRegistry'.
   */
  private static BeanInstanceSupplier<EventConsumerRegistry> getBulkheadEventConsumerRegistryInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<EventConsumerRegistry>forFactoryMethod(BulkheadConfigurationOnMissingBean$$SpringCGLIB$$0.class, "bulkheadEventConsumerRegistry")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean", BulkheadConfigurationOnMissingBean.class).bulkheadEventConsumerRegistry());
  }

  /**
   * Get the bean definition for 'bulkheadEventConsumerRegistry'.
   */
  public static BeanDefinition getBulkheadEventConsumerRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(EventConsumerRegistry.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(EventConsumerRegistry.class, BulkheadEvent.class));
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getBulkheadEventConsumerRegistryInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'compositeBulkheadCustomizer'.
   */
  private static BeanInstanceSupplier<CompositeCustomizer> getCompositeBulkheadCustomizerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<CompositeCustomizer>forFactoryMethod(BulkheadConfigurationOnMissingBean$$SpringCGLIB$$0.class, "compositeBulkheadCustomizer", List.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean", BulkheadConfigurationOnMissingBean.class).compositeBulkheadCustomizer(args.get(0)));
  }

  /**
   * Get the bean definition for 'compositeBulkheadCustomizer'.
   */
  public static BeanDefinition getCompositeBulkheadCustomizerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CompositeCustomizer.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(CompositeCustomizer.class, BulkheadConfigCustomizer.class));
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getCompositeBulkheadCustomizerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'bulkheadRegistry'.
   */
  private static BeanInstanceSupplier<BulkheadRegistry> getBulkheadRegistryInstanceSupplier() {
    return BeanInstanceSupplier.<BulkheadRegistry>forFactoryMethod(BulkheadConfigurationOnMissingBean$$SpringCGLIB$$0.class, "bulkheadRegistry", BulkheadConfigurationProperties.class, EventConsumerRegistry.class, RegistryEventConsumer.class, CompositeCustomizer.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean", BulkheadConfigurationOnMissingBean.class).bulkheadRegistry(args.get(0), args.get(1), args.get(2), args.get(3)));
  }

  /**
   * Get the bean definition for 'bulkheadRegistry'.
   */
  public static BeanDefinition getBulkheadRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(BulkheadRegistry.class);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getBulkheadRegistryInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'bulkheadRegistryEventConsumer'.
   */
  private static BeanInstanceSupplier<RegistryEventConsumer> getBulkheadRegistryEventConsumerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<RegistryEventConsumer>forFactoryMethod(BulkheadConfigurationOnMissingBean$$SpringCGLIB$$0.class, "bulkheadRegistryEventConsumer", Optional.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean", BulkheadConfigurationOnMissingBean.class).bulkheadRegistryEventConsumer(args.get(0)));
  }

  /**
   * Get the bean definition for 'bulkheadRegistryEventConsumer'.
   */
  public static BeanDefinition getBulkheadRegistryEventConsumerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RegistryEventConsumer.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(RegistryEventConsumer.class, Bulkhead.class));
    beanDefinition.setPrimary(true);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getBulkheadRegistryEventConsumerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'bulkheadAspect'.
   */
  private static BeanInstanceSupplier<BulkheadAspect> getBulkheadAspectInstanceSupplier() {
    return BeanInstanceSupplier.<BulkheadAspect>forFactoryMethod(BulkheadConfigurationOnMissingBean$$SpringCGLIB$$0.class, "bulkheadAspect", BulkheadConfigurationProperties.class, ThreadPoolBulkheadRegistry.class, BulkheadRegistry.class, List.class, FallbackExecutor.class, SpelResolver.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean", BulkheadConfigurationOnMissingBean.class).bulkheadAspect(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5)));
  }

  /**
   * Get the bean definition for 'bulkheadAspect'.
   */
  public static BeanDefinition getBulkheadAspectBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(BulkheadAspect.class);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getBulkheadAspectInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'compositeThreadPoolBulkheadCustomizer'.
   */
  private static BeanInstanceSupplier<CompositeCustomizer> getCompositeThreadPoolBulkheadCustomizerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<CompositeCustomizer>forFactoryMethod(BulkheadConfigurationOnMissingBean$$SpringCGLIB$$0.class, "compositeThreadPoolBulkheadCustomizer", List.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean", BulkheadConfigurationOnMissingBean.class).compositeThreadPoolBulkheadCustomizer(args.get(0)));
  }

  /**
   * Get the bean definition for 'compositeThreadPoolBulkheadCustomizer'.
   */
  public static BeanDefinition getCompositeThreadPoolBulkheadCustomizerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CompositeCustomizer.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(CompositeCustomizer.class, ThreadPoolBulkheadConfigCustomizer.class));
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getCompositeThreadPoolBulkheadCustomizerInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'threadPoolBulkheadRegistry'.
   */
  private static BeanInstanceSupplier<ThreadPoolBulkheadRegistry> getThreadPoolBulkheadRegistryInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ThreadPoolBulkheadRegistry>forFactoryMethod(BulkheadConfigurationOnMissingBean$$SpringCGLIB$$0.class, "threadPoolBulkheadRegistry", CommonThreadPoolBulkheadConfigurationProperties.class, EventConsumerRegistry.class, RegistryEventConsumer.class, CompositeCustomizer.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean", BulkheadConfigurationOnMissingBean.class).threadPoolBulkheadRegistry(args.get(0), args.get(1), args.get(2), args.get(3)));
  }

  /**
   * Get the bean definition for 'threadPoolBulkheadRegistry'.
   */
  public static BeanDefinition getThreadPoolBulkheadRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ThreadPoolBulkheadRegistry.class);
    beanDefinition.setDestroyMethodNames("close");
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getThreadPoolBulkheadRegistryInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'threadPoolBulkheadRegistryEventConsumer'.
   */
  private static BeanInstanceSupplier<RegistryEventConsumer> getThreadPoolBulkheadRegistryEventConsumerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<RegistryEventConsumer>forFactoryMethod(BulkheadConfigurationOnMissingBean$$SpringCGLIB$$0.class, "threadPoolBulkheadRegistryEventConsumer", Optional.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean", BulkheadConfigurationOnMissingBean.class).threadPoolBulkheadRegistryEventConsumer(args.get(0)));
  }

  /**
   * Get the bean definition for 'threadPoolBulkheadRegistryEventConsumer'.
   */
  public static BeanDefinition getThreadPoolBulkheadRegistryEventConsumerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RegistryEventConsumer.class);
    beanDefinition.setTargetType(ResolvableType.forClassWithGenerics(RegistryEventConsumer.class, ThreadPoolBulkhead.class));
    beanDefinition.setPrimary(true);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getThreadPoolBulkheadRegistryEventConsumerInstanceSupplier());
    return beanDefinition;
  }
}
