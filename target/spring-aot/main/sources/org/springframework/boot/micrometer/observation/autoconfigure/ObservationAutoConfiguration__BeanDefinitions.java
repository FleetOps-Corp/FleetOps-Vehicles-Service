package org.springframework.boot.micrometer.observation.autoconfigure;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link ObservationAutoConfiguration}.
 */
@Generated
public class ObservationAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'observationAutoConfiguration'.
   */
  public static BeanDefinition getObservationAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ObservationAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(ObservationAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'observationRegistryPostProcessor'.
   */
  private static BeanInstanceSupplier<ObservationRegistryPostProcessor> getObservationRegistryPostProcessorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ObservationRegistryPostProcessor>forFactoryMethod(ObservationAutoConfiguration.class, "observationRegistryPostProcessor", ObjectProvider.class, ObjectProvider.class, ObjectProvider.class, ObjectProvider.class, ObjectProvider.class, ObjectProvider.class)
            .withGenerator((registeredBean, args) -> ObservationAutoConfiguration.observationRegistryPostProcessor(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5)));
  }

  /**
   * Get the bean definition for 'observationRegistryPostProcessor'.
   */
  public static BeanDefinition getObservationRegistryPostProcessorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ObservationAutoConfiguration.class);
    beanDefinition.setTargetType(ObservationRegistryPostProcessor.class);
    beanDefinition.setInstanceSupplier(getObservationRegistryPostProcessorInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'observationRegistry'.
   */
  private static BeanInstanceSupplier<ObservationRegistry> getObservationRegistryInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ObservationRegistry>forFactoryMethod(ObservationAutoConfiguration.class, "observationRegistry")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration", ObservationAutoConfiguration.class).observationRegistry());
  }

  /**
   * Get the bean definition for 'observationRegistry'.
   */
  public static BeanDefinition getObservationRegistryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ObservationRegistry.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration");
    beanDefinition.setInstanceSupplier(getObservationRegistryInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'propertiesObservationFilter'.
   */
  private static BeanInstanceSupplier<PropertiesObservationFilterPredicate> getPropertiesObservationFilterInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<PropertiesObservationFilterPredicate>forFactoryMethod(ObservationAutoConfiguration.class, "propertiesObservationFilter", ObservationProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration", ObservationAutoConfiguration.class).propertiesObservationFilter(args.get(0)));
  }

  /**
   * Get the bean definition for 'propertiesObservationFilter'.
   */
  public static BeanDefinition getPropertiesObservationFilterBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(PropertiesObservationFilterPredicate.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration");
    beanDefinition.setInstanceSupplier(getPropertiesObservationFilterInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'spelValueExpressionResolver'.
   */
  private static BeanInstanceSupplier<SpelValueExpressionResolver> getSpelValueExpressionResolverInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<SpelValueExpressionResolver>forFactoryMethod(ObservationAutoConfiguration.class, "spelValueExpressionResolver")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration", ObservationAutoConfiguration.class).spelValueExpressionResolver());
  }

  /**
   * Get the bean definition for 'spelValueExpressionResolver'.
   */
  public static BeanDefinition getSpelValueExpressionResolverBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SpelValueExpressionResolver.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration");
    beanDefinition.setInstanceSupplier(getSpelValueExpressionResolverInstanceSupplier());
    return beanDefinition;
  }
}
