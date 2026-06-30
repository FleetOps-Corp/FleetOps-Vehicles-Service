package org.springframework.boot.micrometer.metrics.autoconfigure.task;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredMethodArgumentsResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link TaskExecutorMetricsAutoConfiguration}.
 */
@Generated
public class TaskExecutorMetricsAutoConfiguration__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static TaskExecutorMetricsAutoConfiguration apply(RegisteredBean registeredBean,
      TaskExecutorMetricsAutoConfiguration instance) {
    AutowiredMethodArgumentsResolver.forRequiredMethod("bindTaskExecutorsToRegistry", ConfigurableListableBeanFactory.class, MeterRegistry.class).resolve(registeredBean, args -> instance.bindTaskExecutorsToRegistry(args.get(0), args.get(1)));
    return instance;
  }
}
