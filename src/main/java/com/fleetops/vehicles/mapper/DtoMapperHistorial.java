package com.fleetops.vehicles.mapper;

import com.fleetops.vehicles.models.entities.HistorialEstadoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.dto.response.HistorialEstadoResponse;
import org.springframework.stereotype.Component;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: Data Mapper / Layer Decoupling
// ¿Qué hace? Este componente actúa como un intermediario o "traductor". Toma un objeto interno 
// del sistema (HistorialEstadoVehiculo) y lo transforma en un objeto de salida (HistorialEstadoResponse).
// 
// REGLA DE NEGOCIO: Desacoplamiento.
// El Frontend nunca conoce cómo están guardadas las tablas en la Base de Datos; 
// solo conoce el DTO. Esto nos permite cambiar la Base de Datos mañana sin romper la App.
// =========================================================================================

// @Component: Le dice a Spring que esta clase es un "Bean" (un objeto de servicio) que puede 
// ser inyectado en cualquier otro lugar del sistema cuando se necesite mapear datos.
@Component
public class DtoMapperHistorial {

    // Método que realiza la transformación "Entidad -> DTO".
    public HistorialEstadoResponse toDto(HistorialEstadoVehiculo historial) {
        
        // REGLA DE SEGURIDAD: Defensiva.
        // Si el objeto origen es nulo, devolvemos null para evitar que el sistema reviente 
        // con un "NullPointerException".
        if (historial == null) {
            return null;
        }

        // Extraemos la entidad relacionada para trabajar con ella de forma limpia.
        // Ejemplo: Es como sacar el expediente del vehículo dentro del expediente del historial.
        Vehiculo v = historial.getVehiculo();

        // =========================================================================================
        // PATRÓN DE DISEÑO DETECTADO: DTO Flattening (Aplanamiento de Datos).
        // El Frontend necesita mostrar datos de 'Vehiculo' y 'TipoVehiculo' junto con el 'Historial'.
        // En lugar de enviar un JSON con objetos dentro de objetos, "aplanamos" los datos aquí.
        // =========================================================================================
        
        return new HistorialEstadoResponse(
                historial.getIdHistorial(),
                v != null ? v.getIdVehiculo() : null, // ID del vehículo (si existe).
                historial.getEstadoAnterior(),
                historial.getEstadoNuevo(),
                historial.getMotivoCambio(),
                historial.getServicioOrigen(),
                historial.getIdCorrelacion(),
                historial.getRegistradoEn(),

                // ESTRATEGIA DE FLATTENING:
                // 1. Placa: Extraemos solo el campo 'numeroPlaca' para no enviar todo el objeto Vehículo.
                v != null ? v.getNumeroPlaca() : null,
                
                // 2. Nombre del Tipo: Navegación segura.
                // REGLA DE NEGOCIO: Datos Aplanados.
                // Verificamos si v y el tipo existen antes de intentar sacar el nombre, 
                // evitando errores de "dato faltante".
                v != null && v.getTipoVehiculo() != null ? v.getTipoVehiculo().getNombreTipo() : null,
                
                // 3. Kilometraje: dato directo.
                v != null ? v.getKilometraje() : null,
                
                // 4. Capacidad de Carga: Navegación profunda segura.
                v != null && v.getTipoVehiculo() != null ? v.getTipoVehiculo().getCapacidadCarga() : null,

                // 5. Descripción del tipo: dato extraído para contexto administrativo.
                v != null && v.getTipoVehiculo() != null ? v.getTipoVehiculo().getDescripcion() : null
        );
    }
}