// Define la "carpeta lógica" donde agrupamos los sobres de respuesta de nuestra API.
package com.fleetops.vehicles.dto.response;

// Importa herramientas del sistema para manejar fechas y tiempos.
import java.time.LocalDateTime;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: DTO (Data Transfer Object) de Respuesta
// ¿Qué hace? Es el "empaque" final. Cuando la web pregunta "¿Qué tipos de vehículos existen?",
// el sistema llena este sobre con la información y se la entrega al navegador.
//
// PATRÓN DE DISEÑO: Inmutabilidad
// Al usar 'record', garantizamos que los datos que salen del servidor no pueden ser 
// alterados accidentalmente mientras viajan por la red hacia el cliente.
// =========================================================================================
public record TipoVehiculoResponse(
    
    // Identificador único de la categoría. 
    // Ejemplo: 1 (para Furgón), 2 (para Camioneta).
    Long idTipoVehiculo, 

    // Nombre legible de la categoría.
    // Ejemplo: "Furgón Refrigerado".
    String nombreTipo, 

    // REGLA DE NEGOCIO: Descripción Operativa.
    // Este campo es obligatorio para que el usuario sepa exactamente qué tipo de carga puede 
    // meter en este vehículo.
    // Ejemplo: "Ideal para alimentos" (ayuda a tomar decisiones de logística).
    String descripcion, 

    // REGLA DE NEGOCIO: Capacidad Física.
    // Este campo expone el dato validado de cuánto peso puede llevar esta categoría.
    // Ejemplo: 2500.5 (esto asegura que el sistema de asignación de carga no sobrecargue el vehículo).
    Double capacidadCarga, 

    // Auditoría: Indica cuándo se dio de alta esta categoría en el catálogo.
    LocalDateTime creadoEn, 

    // Auditoría: Indica la última vez que alguien modificó los datos de este tipo de vehículo.
    LocalDateTime actualizadoEn 
) {}