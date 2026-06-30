package com.fleetops.vehicles.validators;

import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.repositories.SagaRepository;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link IdempotencyValidator}.
 */
@Generated
public class IdempotencyValidator__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'idempotencyValidator'.
   */
  private static BeanInstanceSupplier<IdempotencyValidator> getIdempotencyValidatorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<IdempotencyValidator>forConstructor(ReservaRepository.class, SagaRepository.class)
            .withGenerator((registeredBean, args) -> new IdempotencyValidator(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'idempotencyValidator'.
   */
  public static BeanDefinition getIdempotencyValidatorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(IdempotencyValidator.class);
    beanDefinition.setInstanceSupplier(getIdempotencyValidatorInstanceSupplier());
    return beanDefinition;
  }
}
