package com.fleetops.vehicles.repositories;

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.lang.Long;
import java.lang.String;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.LongSupplier;
import org.springframework.aot.generate.Generated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.aot.AotRepositoryFragmentSupport;
import org.springframework.data.jpa.repository.query.DeclaredQuery;
import org.springframework.data.jpa.repository.query.QueryEnhancerSelector;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.support.PageableExecutionUtils;

/**
 * AOT generated JPA repository implementation for {@link VehicleRepository}.
 */
@Generated
public class VehicleRepositoryImpl__AotRepository extends AotRepositoryFragmentSupport {
  private final RepositoryFactoryBeanSupport.FragmentCreationContext context;

  private final EntityManager entityManager;

  public VehicleRepositoryImpl__AotRepository(EntityManager entityManager,
      RepositoryFactoryBeanSupport.FragmentCreationContext context) {
    super(QueryEnhancerSelector.DEFAULT_SELECTOR, context);
    this.entityManager = entityManager;
    this.context = context;
  }

  /**
   * AOT generated implementation of {@link VehicleRepository#countByEstadoVehiculoAndActivoTrue(com.fleetops.vehicles.models.entities.EstadoVehiculo)}.
   */
  public long countByEstadoVehiculoAndActivoTrue(EstadoVehiculo estado) {
    String queryString = "SELECT COUNT(v) FROM Vehiculo v WHERE v.estadoVehiculo = :estado AND v.activo = TRUE";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("estado", estado);

    return (Long) convertOne(query.getSingleResultOrNull(), false, Long.class);
  }

  /**
   * AOT generated implementation of {@link VehicleRepository#existsByNumeroPlacaIgnoreCase(java.lang.String)}.
   */
  public boolean existsByNumeroPlacaIgnoreCase(String numeroPlaca) {
    String queryString = "SELECT v.idVehiculo FROM Vehiculo v WHERE UPPER(v.numeroPlaca) = UPPER(:numeroPlaca)";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("numeroPlaca", numeroPlaca != null ? numeroPlaca.toUpperCase() : numeroPlaca);
    query.setMaxResults(1);

    return !query.getResultList().isEmpty();
  }

  /**
   * AOT generated implementation of {@link VehicleRepository#findAllByActivoTrue(org.springframework.data.domain.Pageable)}.
   */
  public Page<Vehiculo> findAllByActivoTrue(Pageable pageable) {
    String queryString = "SELECT v FROM Vehiculo v WHERE v.activo = TRUE";
    String countQueryString = "SELECT COUNT(v) FROM Vehiculo v WHERE v.activo = TRUE";
    Pageable pageable_1 = pageable != null ? pageable : Pageable.unpaged();
    if (pageable_1.getSort().isSorted()) {
      DeclaredQuery declaredQuery = DeclaredQuery.jpqlQuery(queryString);
      queryString = rewriteQuery(declaredQuery, pageable_1.getSort(), Vehiculo.class);
    }
    Query query = this.entityManager.createQuery(queryString);
    if (pageable_1.isPaged()) {
      query.setFirstResult(Long.valueOf(pageable_1.getOffset()).intValue());
      query.setMaxResults(pageable_1.getPageSize());
    }
    LongSupplier countAll = () -> {
      Query countQuery = this.entityManager.createQuery(countQueryString);
      return getCount(countQuery);
    };

    return PageableExecutionUtils.getPage((List<Vehiculo>) query.getResultList(), pageable_1, countAll);
  }

  /**
   * AOT generated implementation of {@link VehicleRepository#findAllByEstadoVehiculo(com.fleetops.vehicles.models.entities.EstadoVehiculo)}.
   */
  public List<Vehiculo> findAllByEstadoVehiculo(EstadoVehiculo estadoVehiculo) {
    String queryString = "SELECT v FROM Vehiculo v WHERE v.estadoVehiculo = :estadoVehiculo";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("estadoVehiculo", estadoVehiculo);

    return (List<Vehiculo>) query.getResultList();
  }

  /**
   * AOT generated implementation of {@link VehicleRepository#findAllByEstadoVehiculoAndActivoTrue(com.fleetops.vehicles.models.entities.EstadoVehiculo)}.
   */
  public List<Vehiculo> findAllByEstadoVehiculoAndActivoTrue(EstadoVehiculo estadoVehiculo) {
    String queryString = "SELECT v FROM Vehiculo v WHERE v.estadoVehiculo = :estadoVehiculo AND v.activo = TRUE";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("estadoVehiculo", estadoVehiculo);

    return (List<Vehiculo>) query.getResultList();
  }

  /**
   * AOT generated implementation of {@link VehicleRepository#findByIdVehiculoAndActivoTrue(java.util.UUID)}.
   */
  public Optional<Vehiculo> findByIdVehiculoAndActivoTrue(UUID id) {
    String queryString = "SELECT v FROM Vehiculo v WHERE v.idVehiculo = :id AND v.activo = TRUE";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("id", id);

    return Optional.ofNullable((Vehiculo) convertOne(query.getSingleResultOrNull(), false, Vehiculo.class));
  }

  /**
   * AOT generated implementation of {@link VehicleRepository#findByNumeroPlacaIgnoreCase(java.lang.String)}.
   */
  public Optional<Vehiculo> findByNumeroPlacaIgnoreCase(String numeroPlaca) {
    String queryString = "SELECT v FROM Vehiculo v WHERE UPPER(v.numeroPlaca) = UPPER(:numeroPlaca)";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("numeroPlaca", numeroPlaca != null ? numeroPlaca.toUpperCase() : numeroPlaca);

    return Optional.ofNullable((Vehiculo) convertOne(query.getSingleResultOrNull(), false, Vehiculo.class));
  }
}
