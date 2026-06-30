package com.fleetops.vehicles.repositories;

import com.fleetops.vehicles.models.entities.SagaVehiculo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.lang.String;
import java.util.Optional;
import org.springframework.aot.generate.Generated;
import org.springframework.data.jpa.repository.aot.AotRepositoryFragmentSupport;
import org.springframework.data.jpa.repository.query.QueryEnhancerSelector;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;

/**
 * AOT generated JPA repository implementation for {@link SagaRepository}.
 */
@Generated
public class SagaRepositoryImpl__AotRepository extends AotRepositoryFragmentSupport {
  private final RepositoryFactoryBeanSupport.FragmentCreationContext context;

  private final EntityManager entityManager;

  public SagaRepositoryImpl__AotRepository(EntityManager entityManager,
      RepositoryFactoryBeanSupport.FragmentCreationContext context) {
    super(QueryEnhancerSelector.DEFAULT_SELECTOR, context);
    this.entityManager = entityManager;
    this.context = context;
  }

  /**
   * AOT generated implementation of {@link SagaRepository#existsByClaveIdempotencia(java.lang.String)}.
   */
  public boolean existsByClaveIdempotencia(String claveIdempotencia) {
    String queryString = "SELECT s.idSaga FROM SagaVehiculo s WHERE s.claveIdempotencia = :claveIdempotencia";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("claveIdempotencia", claveIdempotencia);
    query.setMaxResults(1);

    return !query.getResultList().isEmpty();
  }

  /**
   * AOT generated implementation of {@link SagaRepository#findByClaveIdempotencia(java.lang.String)}.
   */
  public Optional<SagaVehiculo> findByClaveIdempotencia(String claveIdempotencia) {
    String queryString = "SELECT s FROM SagaVehiculo s WHERE s.claveIdempotencia = :claveIdempotencia";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("claveIdempotencia", claveIdempotencia);

    return Optional.ofNullable((SagaVehiculo) convertOne(query.getSingleResultOrNull(), false, SagaVehiculo.class));
  }
}
