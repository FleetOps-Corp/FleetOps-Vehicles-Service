// Define la "carpeta" lógica del proyecto donde agrupamos los convertidores de datos de la API.
package com.fleetops.vehicles.mapper;

// Importa la entidad de base de datos que define qué es un tipo de vehículo.
import com.fleetops.vehicles.models.entities.TipoVehiculo;
// Importa el DTO de respuesta que verá el usuario en la web.
import com.fleetops.vehicles.dto.response.TipoVehiculoResponse;
// Importa la anotación para marcar esta clase como un componente gestionado por Spring.
import org.springframework.stereotype.Component;

// =========================================================================================
// PATRÓN DE DISEÑO: Data Mapper / Isolation Layer
// ¿Qué hace? Actúa como un muro de contención. La entidad 'TipoVehiculo' puede tener 
// campos técnicos, contraseñas, o relaciones internas complejas que no queremos exponer.
// Este mapper se asegura de que solo los datos permitidos salgan hacia el Frontend.
// =========================================================================================

// @Component: Le indica a Spring que cree una instancia de esta clase para ser usada en otros servicios.
@Component
public class DtoMapperTipoVehiculo {

    // Método público que transforma la entidad interna en un DTO de respuesta.
    public TipoVehiculoResponse toDto(TipoVehiculo tipo) {
        
        // REGLA DE SEGURIDAD (Programación Defensiva): Verifica si el objeto origen es nulo.
        if (tipo == null) {
            // Si el objeto está vacío, retornamos nulo para evitar un error grave (NullPointerException) en la app.
            return null;
        }

        // =====================================================================================
        // Construcción inmutable del record.
        // Creamos el objeto de respuesta enviando todos los datos necesarios para la vista.
        // =====================================================================================
        return new TipoVehiculoResponse(
                // 1. ID único de la categoría.
                tipo.getIdTipoVehiculo(),
                
                // 2. Nombre del tipo (Ej: "Furgón Refrigerado").
                tipo.getNombreTipo(),
                
                // 3. Descripción detallada (para que el usuario sepa la utilidad).
                tipo.getDescripcion(),
                
                // 4. Capacidad de carga (dato numérico fundamental para la operación).
                tipo.getCapacidadCarga(),
                
                // 5. Fecha de creación (auditoría).
                tipo.getCreadoEn(),
                
                // 6. Fecha de última actualización (auditoría).
                tipo.getActualizadoEn()
        );
        // Cierre de la construcción del objeto.
    }
    // Cierre del método toDto.
}
// Cierre de la clase DtoMapperTipoVehiculo.