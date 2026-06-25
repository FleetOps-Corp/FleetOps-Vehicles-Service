package com.fleetops.vehicles.repositories;

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.TipoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

// @Repository: Registra esta clase como un Bean de Spring para acceso a datos.
// Permite que Spring traduzca errores de base de datos a excepciones de Spring.
@Repository
public interface VehicleRepository extends JpaRepository<Vehiculo, UUID> {

  // =========================================================================================
  // PATRÓN: Aggregate Root Repository (Repositorio de la Raíz del Agregado).
  // Es el jefe del dominio. Todo el sistema de FleetOps (reservas, historiales) depende
  // de que el Vehículo exista y sea válido.
  // =========================================================================================

  // =========================================================================================
  // REGLA DE NEGOCIO: Borrado Lógico (Soft Delete).
  // Estos métodos garantizan que la lógica de la aplicación nunca "vea" registros desactivados 
  // (vendidos o fuera de flota), aunque sigan existiendo en la base de datos por auditoría.
  // =========================================================================================

  // Trae solo los vehículos activos. Se usa para el tablero principal de operaciones.
  Page<Vehiculo> findAllByActivoTrue(Pageable pageable);

  // Trae los vehículos inactivos. Se usa para el administrador si necesita recuperar o auditar un vehículo.
  Page<Vehiculo> findAllByActivoFalse(Pageable pageable);

  // Buscadores seguros: Garantizan que no devolvemos vehículos "ocultos" aunque el ID sea conocido.
  Optional<Vehiculo> findByIdVehiculoAndActivoTrue(UUID idVehiculo);

  // Búsqueda por placa ignorando mayúsculas/minúsculas: Facilita la vida al operador al buscar.
  Optional<Vehiculo> findByNumeroPlacaIgnoreCaseAndActivoTrue(String numeroPlaca);

  // Búsqueda para recuperar vehículos eliminados lógicamente.
  Optional<Vehiculo> findByNumeroPlacaIgnoreCaseAndActivoFalse(String numeroPlaca);

  // =========================================================================================
  // ESTADO Y CATEGORIZACIÓN
  // =========================================================================================

  // Trae vehículos por estado. Ejemplo: Mostrar solo los "DISPONIBLES" en el mapa de flota.
  Page<Vehiculo> findAllByEstadoVehiculoAndActivoTrue(EstadoVehiculo estadoVehiculo, Pageable pageable);

  // Filtrado avanzado: Buscar por estado y por nombre de categoría (ej. "Furgón") ignorando mayúsculas.
  Page<Vehiculo> findByEstadoVehiculoAndActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase(
      EstadoVehiculo estadoVehiculo,
      String nombreTipo,
      Pageable pageable);

  // Métricas: Este método alimenta los sensores de Micrometer para Grafana/Prometheus.
  long countByEstadoVehiculoAndActivoTrue(EstadoVehiculo estado);

  // Integridad Referencial: Si esto devuelve > 0, la interfaz debe bloquear el borrado de la categoría.
  long countByTipoVehiculoAndActivoTrue(TipoVehiculo tipoVehiculo);

  // =========================================================================================
  // REGLA DE NEGOCIO: Unicidad Física (Prevención de Duplicados)
  // Primera línea de defensa contra corrupción de datos.
  // =========================================================================================

  // Verifica que no haya placas duplicadas.
  boolean existsByNumeroPlacaIgnoreCase(String numeroPlaca);

  // Verifica unicidad del Chasis (VIN).
  boolean existsByNumeroChasisIgnoreCase(String numeroChasis);

  // Verifica unicidad del motor.
  boolean existsByNumeroMotorIgnoreCase(String numeroMotor);

  // =========================================================================================
  // REGLA DE NEGOCIO: Patrón de Auto-Exclusión (Actualización segura)
  // =========================================================================================
  
  // "IdVehiculoNot" asegura que el sistema no se auto-bloquee al editar el mismo registro.
  
  // Permite editar otros datos del vehículo sin que la placa propia dispare error de duplicado.
  boolean existsByNumeroPlacaIgnoreCaseAndIdVehiculoNot(String numeroPlaca, UUID idVehiculo);

  // Misma lógica para chasis.
  boolean existsByNumeroChasisIgnoreCaseAndIdVehiculoNot(String numeroChasis, UUID idVehiculo);

  // Misma lógica para motor.
  boolean existsByNumeroMotorIgnoreCaseAndIdVehiculoNot(String numeroMotor, UUID idVehiculo);
}