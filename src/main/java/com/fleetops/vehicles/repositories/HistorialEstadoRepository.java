package com.fleetops.vehicles.repositories;

import com.fleetops.vehicles.models.entities.HistorialEstadoVehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstadoVehiculo, UUID> {

    @EntityGraph(attributePaths = {"vehiculo", "vehiculo.tipoVehiculo"})
    Page<HistorialEstadoVehiculo> findAllByOrderByRegistradoEnDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"vehiculo", "vehiculo.tipoVehiculo"})
    Page<HistorialEstadoVehiculo> findByVehiculo_IdVehiculoOrderByRegistradoEnDesc(UUID idVehiculo, Pageable pageable);

    @EntityGraph(attributePaths = {"vehiculo", "vehiculo.tipoVehiculo"})
    Page<HistorialEstadoVehiculo> findByVehiculo_NumeroPlacaIgnoreCaseOrderByRegistradoEnDesc(
            String numeroPlaca, Pageable pageable);

    boolean existsByIdCorrelacion(String idCorrelacion);
}
