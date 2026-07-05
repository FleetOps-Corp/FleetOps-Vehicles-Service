package com.fleetops.vehicles.repositories;

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.TipoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

@Repository
public interface VehicleRepository extends JpaRepository<Vehiculo, UUID> {

  @EntityGraph(attributePaths = "tipoVehiculo")
  Page<Vehiculo> findAllByActivoTrue(Pageable pageable);

  @EntityGraph(attributePaths = "tipoVehiculo")
  Page<Vehiculo> findAllByActivoFalse(Pageable pageable);

  @EntityGraph(attributePaths = "tipoVehiculo")
  Optional<Vehiculo> findByIdVehiculoAndActivoTrue(UUID idVehiculo);

  @EntityGraph(attributePaths = "tipoVehiculo")
  @Query("SELECT v FROM Vehiculo v WHERE v.idVehiculo = :id")
  Optional<Vehiculo> findDetailedById(@Param("id") UUID id);

  @EntityGraph(attributePaths = "tipoVehiculo")
  Optional<Vehiculo> findByNumeroPlacaIgnoreCaseAndActivoTrue(String numeroPlaca);

  @EntityGraph(attributePaths = "tipoVehiculo")
  Optional<Vehiculo> findByNumeroPlacaIgnoreCaseAndActivoFalse(String numeroPlaca);

  Optional<Vehiculo> findByNumeroPlacaIgnoreCase(String numeroPlaca);

  @EntityGraph(attributePaths = "tipoVehiculo")
  Page<Vehiculo> findAllByEstadoVehiculoAndActivoTrue(EstadoVehiculo estadoVehiculo, Pageable pageable);

  List<Vehiculo> findAllByEstadoVehiculoAndActivoTrue(EstadoVehiculo estadoVehiculo);

  List<Vehiculo> findAllByActivoTrueAndEstadoVehiculoNot(EstadoVehiculo estadoVehiculo);

  List<Vehiculo> findByActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase(
        String nombreTipo);

  @Query("SELECT v.estadoVehiculo, COUNT(v) FROM Vehiculo v WHERE v.activo = true GROUP BY v.estadoVehiculo")
  List<Object[]> countActiveGroupByEstado();

  @EntityGraph(attributePaths = "tipoVehiculo")
  Page<Vehiculo> findByEstadoVehiculoAndActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase(
      EstadoVehiculo estadoVehiculo,
      String nombreTipo,
      Pageable pageable);

  long countByEstadoVehiculoAndActivoTrue(EstadoVehiculo estado);

  long countByTipoVehiculoAndActivoTrue(TipoVehiculo tipoVehiculo);

  @EntityGraph(attributePaths = "tipoVehiculo")
  @Query("""
      SELECT DISTINCT v FROM Vehiculo v
      JOIN ReservaVehiculo r ON r.vehiculo = v
      WHERE v.activo = true
        AND r.estadoReserva = com.fleetops.vehicles.models.entities.EstadoReserva.CONFIRMADA
        AND :now >= r.fechaInicio AND :now <= r.fechaFin
      """)
  Page<Vehiculo> findAllWithActiveReservation(@Param("now") LocalDateTime now, Pageable pageable);

  boolean existsByNumeroPlacaIgnoreCase(String numeroPlaca);

  boolean existsByNumeroChasisIgnoreCase(String numeroChasis);

  boolean existsByNumeroMotorIgnoreCase(String numeroMotor);

  boolean existsByNumeroPlacaIgnoreCaseAndIdVehiculoNot(String numeroPlaca, UUID idVehiculo);

  boolean existsByNumeroChasisIgnoreCaseAndIdVehiculoNot(String numeroChasis, UUID idVehiculo);

  boolean existsByNumeroMotorIgnoreCaseAndIdVehiculoNot(String numeroMotor, UUID idVehiculo);
}
