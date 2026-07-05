package com.fleetops.vehicles.repositories;

import com.fleetops.vehicles.models.entities.EstadoSaga;
import com.fleetops.vehicles.models.entities.SagaVehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SagaRepository extends JpaRepository<SagaVehiculo, UUID> {

    Optional<SagaVehiculo> findByClaveIdempotencia(String claveIdempotencia);

    boolean existsByClaveIdempotencia(String claveIdempotencia);

    @EntityGraph(attributePaths = "vehiculo")
    @Override
    Optional<SagaVehiculo> findById(UUID id);

    @EntityGraph(attributePaths = "vehiculo")
    Page<SagaVehiculo> findAllByOrderByCreadoEnDesc(Pageable pageable);

    @EntityGraph(attributePaths = "vehiculo")
    Page<SagaVehiculo> findAllByEstadoSagaOrderByCreadoEnDesc(EstadoSaga estadoSaga, Pageable pageable);

    @EntityGraph(attributePaths = "vehiculo")
    Page<SagaVehiculo> findByVehiculo_NumeroPlacaIgnoreCaseOrderByCreadoEnDesc(String numeroPlaca, Pageable pageable);

    @EntityGraph(attributePaths = "vehiculo")
    Page<SagaVehiculo> findByVehiculo_NumeroPlacaIgnoreCaseAndEstadoSagaOrderByCreadoEnDesc(
            String numeroPlaca,
            EstadoSaga estadoSaga,
            Pageable pageable);
}
