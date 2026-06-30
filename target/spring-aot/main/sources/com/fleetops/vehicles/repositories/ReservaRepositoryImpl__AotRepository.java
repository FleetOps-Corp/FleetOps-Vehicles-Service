package com.fleetops.vehicles.repositories;

import com.fleetops.vehicles.models.entities.ReservaVehiculo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.lang.String;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.aot.generate.Generated;
import org.springframework.data.jpa.repository.aot.AotRepositoryFragmentSupport;
import org.springframework.data.jpa.repository.query.QueryEnhancerSelector;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;

/**
 * AOT generated JPA repository implementation for {@link ReservaRepository}.
 */
@Generated
public class ReservaRepositoryImpl__AotRepository extends AotRepositoryFragmentSupport {
  private final RepositoryFactoryBeanSupport.FragmentCreationContext context;

  private final EntityManager entityManager;

  public ReservaRepositoryImpl__AotRepository(EntityManager entityManager,
      RepositoryFactoryBeanSupport.FragmentCreationContext context) {
    super(QueryEnhancerSelector.DEFAULT_SELECTOR, context);
    this.entityManager = entityManager;
    this.context = context;
  }

  /**
   * AOT generated implementation of {@link ReservaRepository#existsByClaveIdempotencia(java.lang.String)}.
   */
  public boolean existsByClaveIdempotencia(String claveIdempotencia) {
    String queryString = "SELECT r.idReserva FROM ReservaVehiculo r WHERE r.claveIdempotencia = :claveIdempotencia";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("claveIdempotencia", claveIdempotencia);
    query.setMaxResults(1);

    return !query.getResultList().isEmpty();
  }

  /**
   * AOT generated implementation of {@link ReservaRepository#existsByVehiculo_IdVehiculoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(java.util.UUID,java.time.LocalDateTime,java.time.LocalDateTime)}.
   */
  public boolean existsByVehiculo_IdVehiculoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
      UUID idVehiculo, LocalDateTime fechaFin, LocalDateTime fechaInicio) {
    String queryString = "SELECT r.idReserva FROM ReservaVehiculo r WHERE r.vehiculo.idVehiculo = :idVehiculo AND r.fechaInicio <= :fechaFin AND r.fechaFin >= :fechaInicio";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("idVehiculo", idVehiculo);
    query.setParameter("fechaFin", fechaFin);
    query.setParameter("fechaInicio", fechaInicio);
    query.setMaxResults(1);

    return !query.getResultList().isEmpty();
  }

  /**
   * AOT generated implementation of {@link ReservaRepository#findByClaveIdempotencia(java.lang.String)}.
   */
  public Optional<ReservaVehiculo> findByClaveIdempotencia(String claveIdempotencia) {
    String queryString = "SELECT r FROM ReservaVehiculo r WHERE r.claveIdempotencia = :claveIdempotencia";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("claveIdempotencia", claveIdempotencia);

    return Optional.ofNullable((ReservaVehiculo) convertOne(query.getSingleResultOrNull(), false, ReservaVehiculo.class));
  }
}
