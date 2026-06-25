// Define la "carpeta" lógica del proyecto donde se almacenan las interfaces de acceso a datos.
package com.fleetops.vehicles.repositories;

// Importa la entidad HistorialEstadoVehiculo para indicarle a JPA con qué tabla vamos a trabajar.
import com.fleetops.vehicles.models.entities.HistorialEstadoVehiculo;
// Importa Page para paginar resultados (para no traer miles de registros de golpe).
import org.springframework.data.domain.Page;
// Importa Pageable para definir límites de página y ordenamiento en las consultas.
import org.springframework.data.domain.Pageable;
// Importa JpaRepository, la "varita mágica" que contiene todos los métodos de base de datos (CRUD).
import org.springframework.data.jpa.repository.JpaRepository;
// Importa la anotación para que Spring reconozca esta interfaz como un repositorio.
import org.springframework.stereotype.Repository;

// Importa UUID para identificar de forma única las llaves primarias.
import java.util.UUID;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: Repository Pattern.
// ¿Qué hace? Provee una capa de abstracción. El resto del sistema no necesita saber
// si la base de datos es PostgreSQL, MySQL o Mongo; solo interactúa con esta interfaz.
// =========================================================================================

// @Repository: Marca la interfaz para que Spring la detecte y cree una implementación automática al arrancar.
@Repository
// extends JpaRepository: Hereda métodos estándar como save(), findById(), delete().
// El primer parámetro es la entidad (HistorialEstadoVehiculo) y el segundo es el tipo del ID (UUID).
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstadoVehiculo, UUID> {

    // =====================================================================================
    // PATRÓN: Derived Query Methods (Consultas derivadas de nombres).
    // Spring Data lee el nombre del método y construye el SQL automáticamente.
    // findAll: Trae todos.
    // ByOrderByRegistradoEnDesc: Ordena por la fecha registrada, de la más nueva a la más vieja.
    // =====================================================================================
    Page<HistorialEstadoVehiculo> findAllByOrderByRegistradoEnDesc(Pageable pageable);

    // =====================================================================================
    // PATRÓN: Navigation across relationships.
    // El guion bajo (_) le indica a JPA que "Vehiculo" es una relación, 
    // y luego entra en "IdVehiculo" para filtrar por él.
    // =====================================================================================
    Page<HistorialEstadoVehiculo> findByVehiculo_IdVehiculoOrderByRegistradoEnDesc(UUID idVehiculo, Pageable pageable);

    // =====================================================================================
    // REGLA DE NEGOCIO (UX): Ignorar mayúsculas/minúsculas.
    // Al usar 'IgnoreCase', el sistema permite que el usuario busque "abc-123" o "ABC-123"
    // y obtenga el mismo resultado. Es más amigable para el usuario final.
    // =====================================================================================
    Page<HistorialEstadoVehiculo> findByVehiculo_NumeroPlacaIgnoreCaseOrderByRegistradoEnDesc(String numeroPlaca, Pageable pageable);
    
} 