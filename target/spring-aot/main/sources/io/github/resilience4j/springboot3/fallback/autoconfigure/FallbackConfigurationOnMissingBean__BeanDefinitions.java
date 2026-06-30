package io.github.resilience4j.springboot3.fallback.autoconfigure;

import io.github.resilience4j.spring6.fallback.CompletionStageFallbackDecorator;
import io.github.resilience4j.spring6.fallback.FallbackDecorators;
import io.github.resilience4j.spring6.fallback.FallbackExecutor;
import io.github.resilience4j.spring6.spelresolver.SpelResolver;
import java.util.List;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassUtils;

/**
 * Bean definitions for {@link FallbackConfigurationOnMissingBean}.
 */
@Generated
public class FallbackConfigurationOnMissingBean__BeanDefinitions {
  /**
   * Get the bean definition for 'fallbackConfigurationOnMissingBean'.
   */
  public static BeanDefinition getFallbackConfigurationOnMissingBeanBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(FallbackConfigurationOnMissingBean.class);
    beanDefinition.setTargetType(FallbackConfigurationOnMissingBean.class);
    ConfigurationClassUtils.initializeConfigurationClass(FallbackConfigurationOnMissingBean.class);
    beanDefinition.setInstanceSupplier(FallbackConfigurationOnMissingBean$$SpringCGLIB$$0::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'fallbackDecorators'.
   */
  private static BeanInstanceSupplier<FallbackDecorators> getFallbackDecoratorsInstanceSupplier() {
    return BeanInstanceSupplier.<FallbackDecorators>forFactoryMethod(FallbackConfigurationOnMissingBean$$SpringCGLIB$$0.class, "fallbackDecorators", List.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.fallback.autoconfigure.FallbackConfigurationOnMissingBean", FallbackConfigurationOnMissingBean.class).fallbackDecorators(args.get(0)));
  }

  /**
   * Get the bean definition for 'fallbackDecorators'.
   */
  public static BeanDefinition getFallbackDecoratorsBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(FallbackDecorators.class);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.fallback.autoconfigure.FallbackConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getFallbackDecoratorsInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'fallbackExecutor'.
   */
  private static BeanInstanceSupplier<FallbackExecutor> getFallbackExecutorInstanceSupplier() {
    return BeanInstanceSupplier.<FallbackExecutor>forFactoryMethod(FallbackConfigurationOnMissingBean$$SpringCGLIB$$0.class, "fallbackExecutor", SpelResolver.class, FallbackDecorators.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.fallback.autoconfigure.FallbackConfigurationOnMissingBean", FallbackConfigurationOnMissingBean.class).fallbackExecutor(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'fallbackExecutor'.
   */
  public static BeanDefinition getFallbackExecutorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(FallbackExecutor.class);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.fallback.autoconfigure.FallbackConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getFallbackExecutorInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'completionStageFallbackDecorator'.
   */
  private static BeanInstanceSupplier<CompletionStageFallbackDecorator> getCompletionStageFallbackDecoratorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<CompletionStageFallbackDecorator>forFactoryMethod(FallbackConfigurationOnMissingBean$$SpringCGLIB$$0.class, "completionStageFallbackDecorator")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("io.github.resilience4j.springboot3.fallback.autoconfigure.FallbackConfigurationOnMissingBean", FallbackConfigurationOnMissingBean.class).completionStageFallbackDecorator());
  }

  /**
   * Get the bean definition for 'completionStageFallbackDecorator'.
   */
  public static BeanDefinition getCompletionStageFallbackDecoratorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CompletionStageFallbackDecorator.class);
    beanDefinition.setFactoryBeanName("io.github.resilience4j.springboot3.fallback.autoconfigure.FallbackConfigurationOnMissingBean");
    beanDefinition.setInstanceSupplier(getCompletionStageFallbackDecoratorInstanceSupplier());
    return beanDefinition;
  }
}
