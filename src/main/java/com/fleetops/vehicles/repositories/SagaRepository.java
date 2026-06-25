// Define el paquete exclusivo para la capa de acceso a datos de las Sagas.
package com.fleetops.vehicles.repositories;

// Importa el DTO de respuesta (aunque no se usa en la interfaz, es una buena práctica dejarlo disponible).
import com.fleetops.vehicles.dto.response.ReservaResponse;
// Importa la entidad SagaVehiculo, que es el objeto que estamos persistiendo.
import com.fleetops.vehicles.models.entities.SagaVehiculo;

// Importa la interfaz Page para manejar resultados paginados y optimizar el rendimiento.
import org.springframework.data.domain.Page;
// Importa Pageable para definir límites de consulta (ej: "trae de 20 en 20").
import org.springframework.data.domain.Pageable;
// Importa JpaRepository, la interfaz maestra que nos da los métodos CRUD (save, find, delete, etc.).
import org.springframework.data.jpa.repository.JpaRepository;
// Importa la anotación para que Spring reconozca este componente.
import org.springframework.stereotype.Repository;

// Importa Optional para manejar búsquedas que podrían no devolver nada sin causar errores.
import java.util.Optional;
// Importa UUID para identificar de forma única los registros en la base de datos.
import java.util.UUID;

// @Repository: Marca esta interfaz como un componente de Spring para acceso a datos.
// Spring la detecta y genera su implementación automáticamente al arrancar.
@Repository
public interface SagaRepository extends JpaRepository<SagaVehiculo, UUID> {
    // Heredamos de JpaRepository, por lo que ya tenemos métodos como findById(), save(), count(), etc.

    // =========================================================================================
    // BUSQUEDA POR IDEMPOTENCIA
    // =========================================================================================
    
    // findByClaveIdempotencia: Busca el expediente de una saga usando su ticket único.
    // REGLA DE NEGOCIO: Si el sistema falla y reintenta, busca si ya existe el ticket para recuperar el estado original.
    Optional<SagaVehiculo> findByClaveIdempotencia(String claveIdempotencia);

    // existsByClaveIdempotencia: Método de alto rendimiento.
    // REGLA DE NEGOCIO: Idempotencia perimetral. Retorna true/false muy rápido.
    // Sirve para bloquear "dobles clics" de usuarios o reintentos automáticos de la red antes de procesar nada.
    boolean existsByClaveIdempotencia(String claveIdempotencia);

    // =========================================================================================
    // MÉTODOS DE AUDITORÍA Y LISTADO (PAGINADOS)
    // =========================================================================================

    // 1. findAllByOrderByCreadoEnDesc:
    // Trae todo el historial de sagas. El ordenamiento Descendente es crítico aquí:
    // los administradores siempre quieren ver el problema más reciente primero.
    Page<SagaVehiculo> findAllByOrderByCreadoEnDesc(Pageable pageable);

    // 2. findAllByEstadoSagaOrderByCreadoEnDesc:
    // REGLA DE NEGOCIO: Dashboard Operativo. Permite ver cuántas sagas están "FALLIDAS" o "EN_PROGRESO".
    // El Orquestador de Sagas usa esto para filtrar y decidir qué procesos necesita reintentar o compensar.
    Page<SagaVehiculo> findAllByEstadoSagaOrderByCreadoEnDesc(com.fleetops.vehicles.models.entities.EstadoSaga estadoSaga, Pageable pageable);

    // 3. findByVehiculo_NumeroPlacaIgnoreCaseOrderByCreadoEnDesc:
    // PATRÓN DE NAVEGACIÓN: Entra en la entidad 'Vehiculo' (a través de la relación) y busca por la placa.
    // IgnoreCase: Hace que la búsqueda no diferencie entre "BOG123" y "bog123" (amigable para el usuario).
    Page<SagaVehiculo> findByVehiculo_NumeroPlacaIgnoreCaseOrderByCreadoEnDesc(String numeroPlaca, Pageable pageable);

    // 4. findByVehiculo_NumeroPlacaIgnoreCaseAndEstadoSagaOrderByCreadoEnDesc:
    // REGLA DE NEGOCIO: Búsqueda avanzada de auditoría.
    // Permite al soporte técnico filtrar: "¿Cuántas sagas fallidas tiene el vehículo con placa ABC-123?".
    Page<SagaVehiculo> findByVehiculo_NumeroPlacaIgnoreCaseAndEstadoSagaOrderByCreadoEnDesc(
            String numeroPlaca, 
            com.fleetops.vehicles.models.entities.EstadoSaga estadoSaga, 
            Pageable pageable);
    
} 