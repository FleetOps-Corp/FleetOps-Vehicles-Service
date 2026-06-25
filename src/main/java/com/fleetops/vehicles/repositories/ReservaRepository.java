// Define el paquete exclusivo para la capa de acceso a datos (Repositorios).
package com.fleetops.vehicles.repositories;

// Importa el enumerado de estados para filtrar búsquedas por tipo de reserva.
import com.fleetops.vehicles.models.entities.EstadoReserva;
// Importa la entidad de la Reserva para trabajar con su tabla.
import com.fleetops.vehicles.models.entities.ReservaVehiculo;

// Importa la interfaz Page para manejar resultados paginados (evita saturar la memoria).
import org.springframework.data.domain.Page;
// Importa Pageable para definir límites de búsqueda (ej: "trae los primeros 10").
import org.springframework.data.domain.Pageable;
// Importa JpaRepository, la interfaz maestra que contiene todas las operaciones CRUD.
import org.springframework.data.jpa.repository.JpaRepository;
// Importa la anotación @Query para escribir consultas personalizadas (JPQL).
import org.springframework.data.jpa.repository.Query;
// Importa la anotación para vincular parámetros de Java a la consulta SQL.
import org.springframework.data.repository.query.Param;
// Importa @Repository para que Spring gestione esta interfaz como un componente de base de datos.
import org.springframework.stereotype.Repository;

// Importa clases auxiliares de Java para manejo de fechas, listas y tipos.
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// @Repository: Le dice a Spring que esta interfaz debe ser instanciada como un Bean que interactúa con la BD.
@Repository
public interface ReservaRepository extends JpaRepository<ReservaVehiculo, UUID> {
  // Heredar de JpaRepository nos regala métodos como save(), findById(), delete()
  // automáticamente.

  // @Query: Define una consulta personalizada en lenguaje JPQL.
  // Buscamos una reserva específica para un vehículo que aún no ha sido aprobada.
  @Query("SELECT r FROM ReservaVehiculo r WHERE r.vehiculo.idVehiculo = :idVehiculo AND r.estadoReserva = 'PENDIENTE'")
  // @Param: Mapea la variable Java 'idVehiculo' al marcador de posición
  // ':idVehiculo' en la consulta.
  // Optional: Devuelve un objeto que puede estar vacío (si no hay reservas
  // pendientes), evitando NullPointerExceptions.
  Optional<ReservaVehiculo> findReservaPendienteByVehiculoId(@Param("idVehiculo") UUID idVehiculo);

  // Consulta compleja usando Text Blocks ("""...""") de Java para mayor
  // legibilidad.
  @Query("""
      SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
      FROM ReservaVehiculo r
      WHERE r.vehiculo.idVehiculo = :idVehiculo
        AND r.estadoReserva IN (:estados)
        AND r.fechaInicio <= :fechaFin
        AND r.fechaFin >= :fechaInicio
      """)
  // REGLA DE NEGOCIO: Detección de colisiones (Solapamiento de fechas).
  // Esta consulta es la "columna vertebral" de las reservas. Determina si el
  // intervalo
  // choca con cualquier reserva existente para el mismo vehículo.
  boolean existeReservaEnRango(
      @Param("idVehiculo") UUID idVehiculo,
      @Param("estados") List<EstadoReserva> estados,
      @Param("fechaFin") LocalDateTime fechaFin,
      @Param("fechaInicio") LocalDateTime fechaInicio);

  // @Query: Anotación de Spring Data JPA que permite definir una consulta
  // personalizada utilizando JPQL (Java Persistence Query Language).
  // SELECT r: Selecciona la entidad completa de la reserva.
  // FROM ReservaVehiculo r: Indica que la consulta se realiza sobre la tabla de
  // reservas.
  // WHERE r.vehiculo.idVehiculo = :idVehiculo: Filtra por el vehículo en
  // cuestión.
  // AND r.estadoReserva IN (:estados): Excluye los estados no restrictivos.
  // AND r.fechaInicio <= :fechaFin: Condición lógica de solapamiento.
  // AND r.fechaFin >= :fechaInicio: Condición lógica de solapamiento.
  @Query("""
      SELECT r
      FROM ReservaVehiculo r
      WHERE r.vehiculo.idVehiculo = :idVehiculo
        AND r.estadoReserva IN (:estados)
        AND r.fechaInicio <= :fechaFin
        AND r.fechaFin >= :fechaInicio
      """)
  List<ReservaVehiculo> obtenerReservasConflictivas(
      @Param("idVehiculo") UUID idVehiculo,
      @Param("estados") List<EstadoReserva> estados,
      @Param("fechaFin") LocalDateTime fechaFin,
      @Param("fechaInicio") LocalDateTime fechaInicio);

  // Busca todas las reservas futuras o vigentes de un vehículo para construir la
  // agenda
  @Query("""
      SELECT r FROM ReservaVehiculo r
      WHERE r.vehiculo.idVehiculo = :idVehiculo
        AND r.estadoReserva IN (:estados)
        AND r.fechaFin >= CURRENT_TIMESTAMP
      ORDER BY r.fechaInicio ASC
      """)
  List<ReservaVehiculo> findReservasActivas(@Param("idVehiculo") UUID idVehiculo,
      @Param("estados") List<EstadoReserva> estados);

  // =====================================================================================
  // PATRÓN: Control de Solapamiento Temporal Avanzado
  // Comprueba matemáticamente la colisión de fechas excluyendo la reserva bajo
  // edición.
  // Fórmula: (InicioNueva < FinExistente) AND (FinNueva > InicioExistente)
  // =====================================================================================
  @Query("SELECT r FROM ReservaVehiculo r WHERE r.vehiculo.idVehiculo = :idVehiculo " +
      "AND r.idReserva <> :idReservaActual " +
      "AND r.estadoReserva IN :estados " +
      "AND (:fechaInicio < r.fechaFin AND :fechaFin > r.fechaInicio)")
  List<ReservaVehiculo> findOverlappingReservations(
      @Param("idVehiculo") UUID idVehiculo,
      @Param("idReservaActual") UUID idReservaActual,
      @Param("fechaInicio") LocalDateTime fechaInicio,
      @Param("fechaFin") LocalDateTime fechaFin,
      @Param("estados") List<EstadoReserva> estados);

  // =====================================================================================
  // PATRÓN: Evaluación de Vigencia de Agendas
  // Busca todas las reservas PENDIENTES o CONFIRMADAS cuyo rango de tiempo
  // abarque la hora actual.
  // =====================================================================================
  @Query("SELECT r FROM ReservaVehiculo r WHERE r.estadoReserva IN :estados " +
      "AND :now >= r.fechaInicio AND :now <= r.fechaFin")
  List<ReservaVehiculo> findCurrentlyActiveReservations(
      @Param("now") LocalDateTime now,
      @Param("estados") List<EstadoReserva> estados);

  // REGLA DE NEGOCIO: Idempotencia.
  // Spring Data JPA lee el nombre del método y genera automáticamente la
  // consulta:
  // "SELECT exists(...) FROM reservas_vehiculo WHERE clave_idempotencia = ?"
  // Protege al sistema contra reintentos accidentales (clics dobles, errores de
  // red).
  boolean existsByClaveIdempotencia(String claveIdempotencia);

  // Busca TODAS las reservas asociadas a una placa y a un estado específico.
  // Devuelve una lista, evitando el error de NonUniqueResultException.
  List<ReservaVehiculo> findAllByVehiculoNumeroPlacaIgnoreCaseAndEstadoReserva(
      String numeroPlaca,
      EstadoReserva estadoReserva);

  // Page<ReservaVehiculo>: Trae todas las reservas paginadas, ordenadas por fecha
  // de creación descendente (la más nueva primero).
  // El uso de 'Page' es vital para no traer miles de registros y colapsar el
  // sistema.
  Page<ReservaVehiculo> findAllByOrderByCreadoEnDesc(Pageable pageable);

  // Busca reservas por estado (ej: solo PENDIENTES) y las ordena por fecha.
  Page<ReservaVehiculo> findAllByEstadoReservaOrderByCreadoEnDesc(EstadoReserva estadoReserva, Pageable pageable);

  // Busca reservas asociadas a un vehículo por su placa, ignorando si el usuario
  // escribió en mayúsculas o minúsculas.
  Page<ReservaVehiculo> findByVehiculo_NumeroPlacaIgnoreCaseOrderByCreadoEnDesc(String numeroPlaca,
      Pageable pageable);

  // Busca reservas por placa de vehículo Y estado específico, ordenando por fecha
  // de creación.
  // Es el método principal usado por los administradores en el dashboard de
  // gestión.
  Page<ReservaVehiculo> findByVehiculo_NumeroPlacaIgnoreCaseAndEstadoReservaOrderByCreadoEnDesc(
      String numeroPlaca,
      EstadoReserva estadoReserva,
      Pageable pageable);


      // =====================================================================================
    // Busca todas las reservas de un vehículo específico filtrando por varios estados.
    // =====================================================================================
    List<ReservaVehiculo> findByVehiculo_IdVehiculoAndEstadoReservaIn(UUID idVehiculo, List<EstadoReserva> estados);

    
}