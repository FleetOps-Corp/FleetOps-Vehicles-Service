package com.fleetops.vehicles.repositories;

import com.fleetops.vehicles.models.entities.TipoVehiculo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.lang.String;
import java.util.Optional;
import org.springframework.aot.generate.Generated;
import org.springframework.data.jpa.repository.aot.AotRepositoryFragmentSupport;
import org.springframework.data.jpa.repository.query.QueryEnhancerSelector;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;

/**
 * AOT generated JPA repository implementation for {@link TipoVehiculoRepository}.
 */
@Generated
public class TipoVehiculoRepositoryImpl__AotRepository extends AotRepositoryFragmentSupport {
  private final RepositoryFactoryBeanSupport.FragmentCreationContext context;

  private final EntityManager entityManager;

  public TipoVehiculoRepositoryImpl__AotRepository(EntityManager entityManager,
      RepositoryFactoryBeanSupport.FragmentCreationContext context) {
    super(QueryEnhancerSelector.DEFAULT_SELECTOR, context);
    this.entityManager = entityManager;
    this.context = context;
  }

  /**
   * AOT generated implementation of {@link TipoVehiculoRepository#existsByNombreTipoIgnoreCase(java.lang.String)}.
   */
  public boolean existsByNombreTipoIgnoreCase(String nombreTipo) {
    String queryString = "SELECT t.idTipoVehiculo FROM TipoVehiculo t WHERE UPPER(t.nombreTipo) = UPPER(:nombreTipo)";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("nombreTipo", nombreTipo != null ? nombreTipo.toUpperCase() : nombreTipo);
    query.setMaxResults(1);

    return !query.getResultList().isEmpty();
  }

  /**
   * AOT generated implementation of {@link TipoVehiculoRepository#findByNombreTipoIgnoreCase(java.lang.String)}.
   */
  public Optional<TipoVehiculo> findByNombreTipoIgnoreCase(String nombreTipo) {
    String queryString = "SELECT t FROM TipoVehiculo t WHERE UPPER(t.nombreTipo) = UPPER(:nombreTipo)";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("nombreTipo", nombreTipo != null ? nombreTipo.toUpperCase() : nombreTipo);

    return Optional.ofNullable((TipoVehiculo) convertOne(query.getSingleResultOrNull(), false, TipoVehiculo.class));
  }
}
