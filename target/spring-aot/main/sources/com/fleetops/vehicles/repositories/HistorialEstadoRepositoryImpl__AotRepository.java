package com.fleetops.vehicles.repositories;

import com.fleetops.vehicles.models.entities.HistorialEstadoVehiculo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.lang.String;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.aot.generate.Generated;
import org.springframework.data.jpa.repository.aot.AotRepositoryFragmentSupport;
import org.springframework.data.jpa.repository.query.QueryEnhancerSelector;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;

/**
 * AOT generated JPA repository implementation for {@link HistorialEstadoRepository}.
 */
@Generated
public class HistorialEstadoRepositoryImpl__AotRepository extends AotRepositoryFragmentSupport {
  private final RepositoryFactoryBeanSupport.FragmentCreationContext context;

  private final EntityManager entityManager;

  public HistorialEstadoRepositoryImpl__AotRepository(EntityManager entityManager,
      RepositoryFactoryBeanSupport.FragmentCreationContext context) {
    super(QueryEnhancerSelector.DEFAULT_SELECTOR, context);
    this.entityManager = entityManager;
    this.context = context;
  }

  /**
   * AOT generated implementation of {@link HistorialEstadoRepository#findByVehiculo_IdVehiculoAndRegistradoEnBetween(java.util.UUID,java.time.LocalDateTime,java.time.LocalDateTime)}.
   */
  public List<HistorialEstadoVehiculo> findByVehiculo_IdVehiculoAndRegistradoEnBetween(
      UUID idVehiculo, LocalDateTime desde, LocalDateTime hasta) {
    String queryString = "SELECT h FROM HistorialEstadoVehiculo h WHERE h.vehiculo.idVehiculo = :idVehiculo AND h.registradoEn BETWEEN :desde AND :hasta";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("idVehiculo", idVehiculo);
    query.setParameter("desde", desde);
    query.setParameter("hasta", hasta);

    return (List<HistorialEstadoVehiculo>) query.getResultList();
  }

  /**
   * AOT generated implementation of {@link HistorialEstadoRepository#findByVehiculo_IdVehiculoOrderByRegistradoEnDesc(java.util.UUID)}.
   */
  public List<HistorialEstadoVehiculo> findByVehiculo_IdVehiculoOrderByRegistradoEnDesc(
      UUID idVehiculo) {
    String queryString = "SELECT h FROM HistorialEstadoVehiculo h WHERE h.vehiculo.idVehiculo = :idVehiculo ORDER BY h.registradoEn desc";
    Query query = this.entityManager.createQuery(queryString);
    query.setParameter("idVehiculo", idVehiculo);

    return (List<HistorialEstadoVehiculo>) query.getResultList();
  }
}
