// Define la "carpeta" lógica del proyecto donde se agrupan los convertidores (mappers) de datos.
package com.fleetops.vehicles.mapper;

// Importa la entidad Vehiculo (lo que vive en la base de datos).
import com.fleetops.vehicles.models.entities.Vehiculo;
// Importa el DTO de respuesta (la estructura limpia que enviamos al cliente).
import com.fleetops.vehicles.dto.response.VehicleResponse;
// Importa la anotación para que Spring gestione esta clase como un componente.
import org.springframework.stereotype.Component;

// =========================================================================================
// PATRÓN DE DISEÑO: Data Mapper / Flattening
// =========================================================================================

// @Component: Registra esta clase en Spring para que pueda ser usada en cualquier servicio.
@Component
public class DtoMapperVehicle {

    // Método que transforma una entidad Vehiculo en un DTO VehicleResponse.
    public VehicleResponse toDto(Vehiculo vehiculo) {
        // REGLA DE SEGURIDAD: Si el objeto origen es nulo, retornamos null inmediatamente para evitar errores.
        if (vehiculo == null) {
            // Salida temprana en caso de objeto nulo.
            return null;
        }

        // =========================================================================================
        // PATRÓN DE DISEÑO: Flattening (Aplanamiento de Estructuras)
        // Construimos el DTO inmutable 'VehicleResponse'.
        // Aquí tomamos los datos de 'vehiculo' y los datos anidados de su 'tipoVehiculo'
        // para ponerlos todos al mismo nivel jerárquico.
        // =========================================================================================
        return new VehicleResponse(
                // 1. Extrae el ID único del vehículo.
                vehiculo.getIdVehiculo(),
                // 2. Extrae la placa del vehículo.
                vehiculo.getNumeroPlaca(),
                // 3. Extrae la marca.
                vehiculo.getMarca(),
                // 4. Extrae el modelo.
                vehiculo.getModelo(),
                // 5. Extrae el año de fabricación.
                vehiculo.getAnioFabricacion(),
                // 6. Extrae el color.
                vehiculo.getColor(),
                // 7. Extrae el kilometraje.
                vehiculo.getKilometraje(),
                // 8. Extrae la ciudad de operación.
                vehiculo.getCiudadOperacion(),
                // 9. Extrae la sede de operación.
                vehiculo.getSedeOperacion(),
                // 10. REGLA DE NEGOCIO: Convierte el Enum 'estadoVehiculo' a texto plano (.name()) para JSON.
                vehiculo.getEstadoVehiculo() != null ? vehiculo.getEstadoVehiculo().name() : null,
                // 11. Extrae el flag de activo (para control de borrado lógico).
                vehiculo.getActivo(),
                // 12. Extrae fecha de SOAT.
                vehiculo.getFechaSoat(),
                // 13. Extrae fecha de RTM.
                vehiculo.getFechaRtm(),
                // 14. Extrae fecha de último mantenimiento.
                vehiculo.getFechaUltimoMant(),
                // 15. Extrae fecha de creación del registro.
                vehiculo.getCreadoEn(),
                // 16. Extrae fecha de última actualización.
                vehiculo.getActualizadoEn(),
                
                // =================================================================================
                // APLANAMIENTO DE DATOS (Flattening)
                // =================================================================================
                
                // 17. Extrae el nombre del tipo (navegación segura: si tipoVehiculo es null, devuelve null).
                vehiculo.getTipoVehiculo() != null ? vehiculo.getTipoVehiculo().getNombreTipo() : null,
                // 18. Extrae la capacidad de carga (navegación segura).
                vehiculo.getTipoVehiculo() != null ? vehiculo.getTipoVehiculo().getCapacidadCarga() : null,
                // 19. Extrae la descripción del tipo (navegación segura).
                vehiculo.getTipoVehiculo() != null ? vehiculo.getTipoVehiculo().getDescripcion() : null
        ); // Fin de la construcción del VehicleResponse.
    } // Fin del método toDto.
} // Fin de la clase DtoMapperVehicle.