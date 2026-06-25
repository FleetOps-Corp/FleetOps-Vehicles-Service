// Define la "carpeta lógica" donde agrupamos los sobres de respuesta de nuestra API.
package com.fleetops.vehicles.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: DTO (Data Transfer Object) de Respuesta
// ¿Qué hace? Es el "empaque" oficial del vehículo. Cuando el administrador abre la tabla 
// general en la web, el sistema llena este sobre con todos los detalles y lo envía.
// 
// PATRÓN DE DISEÑO DETECTADO: DTO Flattening (Aplanamiento de datos)
// ¿Qué hace? Evita enviar objetos anidados complejos. En lugar de enviar un objeto "TipoVehiculo"
// dentro del vehículo, extraemos los datos clave (nombre, capacidad) y los ponemos al mismo nivel.
// =========================================================================================
public record VehicleResponse(
        // Identificador único (ID) del vehículo en base de datos.
        UUID idVehiculo, 
        
        // Placa del vehículo. Ejemplo: "BOG123".
        String numeroPlaca, 
        
        // Marca del fabricante (Ej: "Chevrolet").
        String marca, 
        
        // Línea del vehículo (Ej: "NPR").
        String modelo, 
        
        // Año de fabricación.
        Integer anioFabricacion, 
        
        // Color físico del automotor.
        String color, 
        
        // Kilometraje actual (dato clave para mantenimiento).
        Integer kilometraje, 
        
        // Ciudad donde opera el vehículo.
        String ciudadOperacion, 
        
        // Sede (patio) donde se resguarda el vehículo.
        String sedeOperacion, 
        
        // REGLA DE NEGOCIO: Estado Operativo.
        // Indica la etapa del ciclo de vida (DISPONIBLE, EN_MANTENIMIENTO, etc.).
        String estadoVehiculo, 
        
        // =========================================================================================
        // REGLA DE NEGOCIO: Visibilidad de Activos (Soft Delete)
        // Indica si el vehículo está en la "papelera lógica". Si es 'false', el sistema
        // oculta el vehículo automáticamente aunque siga existiendo en la base de datos.
        // =========================================================================================
        Boolean activo, 
        
        // Fechas de vigencia legal (SOAT, RTM).
        LocalDate fechaSoat,
        LocalDate fechaRtm,
        LocalDate fechaUltimoMant,
        
        // Auditoría: Indica cuándo se registró y cuándo se tocó por última vez este registro.
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn,

        // =========================================================================================
        // DATOS APLANADOS (Flattening):
        // En lugar de enviar un objeto complejo de "TipoVehiculo", enviamos estos datos
        // crudos para que la tabla en el Frontend sea rápida, limpia y fácil de renderizar.
        // =========================================================================================
        
        // REGLA DE NEGOCIO: Optimización de consumo de red.
        // Enviamos el nombre del tipo (Ej: "Camioneta") directamente en la respuesta del
        // vehículo para evitar que el navegador tenga que hacer llamadas extra al servidor.
        String nombreTipoVehiculo,
        
        // Capacidad de carga extraída del catálogo.
        Double capacidadCarga, 
        
        // Descripción del tipo extraída del catálogo.
        String descripcionTipo

) {
}