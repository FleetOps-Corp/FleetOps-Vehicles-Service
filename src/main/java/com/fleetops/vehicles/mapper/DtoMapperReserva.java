// Define la "carpeta" lógica donde agrupamos los convertidores de datos de la API.
package com.fleetops.vehicles.mapper;

import com.fleetops.vehicles.models.entities.ReservaVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.dto.response.ReservaResponse;
import org.springframework.stereotype.Component;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: Data Mapper / Layer Decoupling
// ¿Qué hace? Este componente actúa como un traductor. Convierte la Entidad (formato de base de datos) 
// en un DTO (formato de red/JSON). 
//
// REGLA DE NEGOCIO: Desacoplamiento.
// El Frontend nunca conoce la estructura de la base de datos (tablas), solo recibe el objeto 
// que nosotros definimos como 'ReservaResponse'. Esto protege la seguridad de la información.
// =========================================================================================

// @Component: Spring gestionará esta clase como un servicio reutilizable.
@Component
public class DtoMapperReserva {

    // Método que transforma una entidad de reserva en un DTO de respuesta.
    public ReservaResponse toDto(ReservaVehiculo reserva) {
        
        // REGLA DE SEGURIDAD: Defensiva.
        // Si el objeto origen es nulo, devolvemos null para evitar que el sistema reviente 
        // con un "NullPointerException".
        if (reserva == null) {
            return null;
        }

        // Extraemos el vehículo a una variable local para hacer la navegación más limpia y segura.
        // Ejemplo: Es como sacar el expediente del vehículo dentro del expediente de la reserva.
        Vehiculo v = reserva.getVehiculo();

        // =========================================================================================
        // PATRÓN DE DISEÑO DETECTADO: DTO Flattening (Aplanamiento de datos).
        // Construimos un 'record' inmutable que agrupa datos de la Reserva y del Vehículo
        // en un solo nivel, ahorrando al cliente peticiones extra al servidor.
        // =========================================================================================
        return new ReservaResponse(
                reserva.getIdReserva(), // ID único de la reserva.
                
                // Navegación segura: Si el vehículo (v) existe, obtenemos su ID, si no, devolvemos null.
                v != null ? v.getIdVehiculo() : null,
                
                // REGLA DE NEGOCIO: Serialización de Enums.
                // Convertimos el Enum 'estadoReserva' a su nombre en texto (.name()) para el JSON.
                reserva.getEstadoReserva() != null ? reserva.getEstadoReserva().name() : null,
                
                // REGLA DE NEGOCIO: Trazabilidad.
                // Convertimos el ID de asignación a String para asegurar consistencia en la respuesta.
                reserva.getIdAsignacionExt() != null ? reserva.getIdAsignacionExt().toString() : null,
                
                reserva.getSolicitadoPor(), // Quién hizo la reserva.
                reserva.getFechaInicio(),   // Inicio del uso del vehículo.
                reserva.getFechaFin(),      // Fin del uso del vehículo.
                reserva.getClaveIdempotencia(), // Ticket contra clics dobles.
                
                // Navegación segura para obtener el ID de la Saga desde el objeto de la transacción.
                reserva.getSagaVehiculo() != null ? reserva.getSagaVehiculo().getIdSaga() : null,

                // =================================================================================
                // EXTRACCIÓN PLANA (FLATTENING) DE LOS DATOS DEL VEHÍCULO
                // =================================================================================
                
                // 1. Placa: Extraemos la placa directamente para el "Voucher Digital".
                v != null ? v.getNumeroPlaca() : null,
                
                // 2. Tipo de Vehículo: Navegación de 2 niveles (Vehiculo -> Tipo).
                v != null && v.getTipoVehiculo() != null ? v.getTipoVehiculo().getNombreTipo() : null,
                
                // 3. Descripción: Dato extraído para contexto del cliente.
                v != null && v.getTipoVehiculo() != null ? v.getTipoVehiculo().getDescripcion() : null,
                
                // 4. Kilometraje: Dato clave para el reporte de auditoría de la reserva.
                v != null ? v.getKilometraje() : null,
                
                // 5. Capacidad de Carga: Navegación de 2 niveles con chequeo de nulls.
                v != null && v.getTipoVehiculo() != null ? v.getTipoVehiculo().getCapacidadCarga() : null
        );
    }
}