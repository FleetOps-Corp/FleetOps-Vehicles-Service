// Define el paquete exclusivo para la capa de acceso a datos.
package com.fleetops.vehicles.repositories;

// Importa la entidad TipoVehiculo (la estructura del catálogo).
import com.fleetops.vehicles.models.entities.TipoVehiculo;
// Importa JpaRepository, la interfaz maestra que nos da los métodos CRUD.
import org.springframework.data.jpa.repository.JpaRepository;
// Importa la anotación para que Spring reconozca este componente.
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

// Importa Optional, una herramienta de seguridad que evita NullPointerExceptions.
import java.util.Optional;

// =========================================================================================
// PATRÓN DE DISEÑO: Repository Pattern (Catálogo Maestro)
// ¿Qué hace? Gestiona las categorías de vehículos. A diferencia de las otras tablas, 
// aquí usamos Long como ID porque los catálogos son pequeños, estáticos y muy consultados.
// =========================================================================================

// @Repository: Marca esta interfaz como un Bean de Spring para acceso a la base de datos.
@Repository
public interface TipoVehiculoRepository extends JpaRepository<TipoVehiculo, Long> {
    // Heredar de JpaRepository nos regala métodos como save(), findById(), delete() automáticamente.

    // =====================================================================================
    // findByNombreTipoIgnoreCase:
    // REGLA DE NEGOCIO (UX): Facilidad de búsqueda.
    // Permite buscar por nombre sin importar si el usuario escribió "Furgón", "furgón" o "FURGÓN".
    // Esto es vital para que las interfaces de administración sean resistentes a errores de escritura.
    // =====================================================================================
    Optional<TipoVehiculo> findByNombreTipoIgnoreCase(String nombreTipo);

    // =====================================================================================
    // existsByNombreTipoIgnoreCase:
    // REGLA DE NEGOCIO: Integridad del Catálogo.
    // Antes de guardar un nuevo tipo de vehículo, el servicio usa este método para verificar
    // que el nombre no esté duplicado, protegiendo la base de datos contra registros redundantes.
    // =====================================================================================
    boolean existsByNombreTipoIgnoreCase(String nombreTipo);

} 