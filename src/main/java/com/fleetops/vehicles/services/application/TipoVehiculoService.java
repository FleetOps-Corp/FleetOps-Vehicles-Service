// Define la ubicación lógica del archivo dentro del proyecto; pertenece a la capa de servicios de aplicación.
package com.fleetops.vehicles.services.application;

// Importa los DTOs (Data Transfer Objects) que actúan como contrato de datos con el cliente.
import com.fleetops.vehicles.dto.request.TipoVehiculoRequest;
import com.fleetops.vehicles.dto.response.TipoVehiculoResponse;
// Importa las herramientas de Spring para manejar paginación de datos.
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// =========================================================================================
// PATRÓN DE DISEÑO: Application Facade (Fachada de Aplicación).
// ¿Por qué existe? Centraliza la lógica de negocio. Los controladores (la API) solo 
// interactúan con esta interfaz, ignorando si el dato viene de una base de datos, 
// un servicio externo o un caché. Esto permite cambiar la implementación interna sin 
// afectar al cliente (Frontend/App Móvil).
// =========================================================================================
public interface TipoVehiculoService {

    // findAll: Método para listar categorías. 
    // Usa 'Pageable' para evitar sobrecargar la memoria del servidor si el catálogo crece.
    Page<TipoVehiculoResponse> findAll(Pageable pageable);
   
    // findById: Recupera los detalles técnicos de una categoría (como su nombre y capacidad).
    // Es la operación de lectura puntual por excelencia.
    TipoVehiculoResponse findById(Long id);
   
    // create: Método para registrar una nueva categoría en el catálogo.
    // REGLA DE NEGOCIO: Unicidad estricta. El servicio validará que no exista un 
    // nombre duplicado antes de persistir.
    TipoVehiculoResponse create(TipoVehiculoRequest request);
   
    // update: Método para modificar una categoría existente.
    // REGLA DE NEGOCIO: Protección contra colisiones. Asegura que el cambio de nombre 
    // no choque con otra categoría existente.
    TipoVehiculoResponse update(Long id, TipoVehiculoRequest request);
   
    // delete: Método para eliminar una categoría.
    // REGLA DE NEGOCIO: Integridad Referencial. El servicio debe verificar si existen 
    // vehículos asociados a esta categoría; si hay, debe lanzar un error para evitar 
    // dejar registros huérfanos en el sistema.
    void delete(Long id);
   
}