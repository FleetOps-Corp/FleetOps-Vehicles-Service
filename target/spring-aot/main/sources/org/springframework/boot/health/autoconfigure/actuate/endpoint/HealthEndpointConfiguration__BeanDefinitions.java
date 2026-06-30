package org.springframework.boot.health.autoconfigure.actuate.endpoint;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointGroups;
import org.springframework.boot.health.actuate.endpoint.HttpCodeStatusMapper;
import org.springframework.boot.health.actuate.endpoint.StatusAggregator;
import org.springframework.boot.health.registry.HealthContributorRegistry;
import org.springframework.context.ApplicationContext;

/**
 * Bean definitions for {@link HealthEndpointConfiguration}.
 */
@Generated
public class HealthEndpointConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'healthEndpointConfiguration'.
   */
  public static BeanDefinition getHealthEndpointConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(HealthEndpointConfiguration.class);
    beanDefinition.setInstanceSupplier(HealthEndpointConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'healthStatusAggregator'.
   */
  private static BeanInstanceSupplier<StatusAggregator> getHealthStatusAggregatorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<StatusAggregator>forFactoryMethod(HealthEndpointConfiguration.class, "healthStatusAggregator", HealthEndpointProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointConfiguration", HealthEndpointConfiguration.class).healthStatusAggregator(args.get(0)));
  }

  /**
   * Get the bean definition for 'healthStatusAggregator'.
   */
  public static BeanDefinition getHealthStatusAggregatorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(StatusAggregator.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointConfiguration");
    beanDefinition.setInstanceSupplier(getHealthStatusAggregatorInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'healthHttpCodeStatusMapper'.
   */
  private static BeanInstanceSupplier<HttpCodeStatusMapper> getHealthHttpCodeStatusMapperInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<HttpCodeStatusMapper>forFactoryMethod(HealthEndpointConfiguration.class, "healthHttpCodeStatusMapper", HealthEndpointProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointConfiguration", HealthEndpointConfiguration.class).healthHttpCodeStatusMapper(args.get(0)));
  }

  /**
   * Get the bean definition for 'healthHttpCodeStatusMapper'.
   */
  public static BeanDefinition getHealthHttpCodeStatusMapperBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(HttpCodeStatusMapper.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointConfiguration");
    beanDefinition.setInstanceSupplier(getHealthHttpCodeStatusMapperInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'healthEndpointGroups'.
   */
  private static BeanInstanceSupplier<AutoConfiguredHealthEndpointGroups> getHealthEndpointGroupsInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<AutoConfiguredHealthEndpointGroups>forFactoryMethod(HealthEndpointConfiguration.class, "healthEndpointGroups", ApplicationContext.class, HealthEndpointProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointConfiguration", HealthEndpointConfiguration.class).healthEndpointGroups(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'healthEndpointGroups'.
   */
  public static BeanDefinition getHealthEndpointGroupsBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(AutoConfiguredHealthEndpointGroups.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointConfiguration");
    beanDefinition.setInstanceSupplier(getHealthEndpointGroupsInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'groupsHealthContributorNameValidator'.
   */
  private static BeanInstanceSupplier<GroupsHealthContributorNameValidator> getGroupsHealthContributorNameValidatorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<GroupsHealthContributorNameValidator>forFactoryMethod(HealthEndpointConfiguration.class, "groupsHealthContributorNameValidator", ObjectProvider.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointConfiguration", HealthEndpointConfiguration.class).groupsHealthContributorNameValidator(args.get(0)));
  }

  /**
   * Get the bean definition for 'groupsHealthContributorNameValidator'.
   */
  public static BeanDefinition getGroupsHealthContributorNameValidatorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(GroupsHealthContributorNameValidator.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointConfiguration");
    beanDefinition.setInstanceSupplier(getGroupsHealthContributorNameValidatorInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'healthEndpointGroupMembershipValidator'.
   */
  private static BeanInstanceSupplier<HealthEndpointConfiguration.HealthEndpointGroupMembershipValidator> getHealthEndpointGroupMembershipValidatorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<HealthEndpointConfiguration.HealthEndpointGroupMembershipValidator>forFactoryMethod(HealthEndpointConfiguration.class, "healthEndpointGroupMembershipValidator", HealthEndpointProperties.class, HealthContributorRegistry.class, ObjectProvider.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointConfiguration", HealthEndpointConfiguration.class).healthEndpointGroupMembershipValidator(args.get(0), args.get(1), args.get(2)));
  }

  /**
   * Get the bean definition for 'healthEndpointGroupMembershipValidator'.
   */
  public static BeanDefinition getHealthEndpointGroupMembershipValidatorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(HealthEndpointConfiguration.HealthEndpointGroupMembershipValidator.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointConfiguration");
    beanDefinition.setInstanceSupplier(getHealthEndpointGroupMembershipValidatorInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'healthEndpoint'.
   */
  private static BeanInstanceSupplier<HealthEndpoint> getHealthEndpointInstanceSupplier() {
    return BeanInstanceSupplier.<HealthEndpoint>forFactoryMethod(HealthEndpointConfiguration.class, "healthEndpoint", HealthContributorRegistry.class, ObjectProvider.class, HealthEndpointGroups.class, HealthEndpointProperties.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointConfiguration", HealthEndpointConfiguration.class).healthEndpoint(args.get(0), args.get(1), args.get(2), args.get(3)));
  }

  /**
   * Get the bean definition for 'healthEndpoint'.
   */
  public static BeanDefinition getHealthEndpointBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(HealthEndpoint.class);
    beanDefinition.setFactoryBeanName("org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointConfiguration");
    beanDefinition.setInstanceSupplier(getHealthEndpointInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'healthEndpointGroupsBeanPostProcessor'.
   */
  private static BeanInstanceSupplier<HealthEndpointConfiguration.HealthEndpointGroupsBeanPostProcessor> getHealthEndpointGroupsBeanPostProcessorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<HealthEndpointConfiguration.HealthEndpointGroupsBeanPostProcessor>forFactoryMethod(HealthEndpointConfiguration.class, "healthEndpointGroupsBeanPostProcessor", ObjectProvider.class)
            .withGenerator((registeredBean, args) -> HealthEndpointConfiguration.healthEndpointGroupsBeanPostProcessor(args.get(0)));
  }

  /**
   * Get the bean definition for 'healthEndpointGroupsBeanPostProcessor'.
   */
  public static BeanDefinition getHealthEndpointGroupsBeanPostProcessorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(HealthEndpointConfiguration.class);
    beanDefinition.setTargetType(HealthEndpointConfiguration.HealthEndpointGroupsBeanPostProcessor.class);
    beanDefinition.setInstanceSupplier(getHealthEndpointGroupsBeanPostProcessorInstanceSupplier());
    return beanDefinition;
  }
}
