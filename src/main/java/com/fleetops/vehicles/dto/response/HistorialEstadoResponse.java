// Define la "carpeta" donde agrupamos los sobres de respuesta de nuestra API.
package com.fleetops.vehicles.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: DTO (Data Transfer Object) implementado como 'record'
// ¿Qué hace? Empaqueta los datos en un objeto inmutable para enviarlos al cliente.
// Ejemplo: Es como una ficha técnica que se entrega al administrador; una vez impresa,
// nadie debe modificarla.
//
// PATRÓN DE DISEÑO DETECTADO: DTO Flattening (Aplanamiento de datos)
// ¿Qué hace? En lugar de enviar un objeto "Vehículo" anidado y complejo, extraemos los
// datos clave (como la placa y el nombre del tipo) y los ponemos en el mismo nivel.
// Ejemplo: Es como recibir un sobre que ya trae el nombre de la calle, sin tener que abrir
// otro sobre pequeño adentro que diga "ubicación".
// =========================================================================================
public record HistorialEstadoResponse(
    
    // ID único del registro de auditoría en la tabla Historial.
    UUID idHistorial,
    
    // ID del vehículo involucrado en el cambio.
    UUID idVehiculo,
    
    // Estado en el que estaba antes del cambio (Ej: "DISPONIBLE").
    String estadoAnterior,
    
    // Estado al que se movió (Ej: "EN_MANTENIMIENTO").
    String estadoNuevo,
    
    // =========================================================================================
    // REGLA DE NEGOCIO: Transparencia Operativa
    // Es obligatorio explicar por qué se cambió el estado para que los auditores sepan
    // si fue por un choque, un mantenimiento preventivo o una venta.
    // =========================================================================================
    String motivoCambio,
    
    // Identificador del sistema que originó el cambio (Ej: "App-Mecánicos").
    String servicioOrigen,
    
    // Código para rastrear la petición a través de varios microservicios.
    String idCorrelacion,
    
    // Fecha y hora exacta del evento de cambio.
    LocalDateTime registradoEn,

    // =========================================================================================
    // DATOS APLANADOS (Flattening):
    // Estos campos no pertenecen a la tabla Historial, pero los incluimos aquí para
    // que el Frontend pueda mostrar "Camión CP-001" sin tener que hacer otra consulta extra.
    // =========================================================================================
    
    String numeroPlaca,      // Ej: "CP-001"
    String nombreTipo,       // Ej: "Camión Carga Pesada"
    Integer kilometraje,     // Kilometraje en el momento del cambio.
    Double capacidadCarga,   // Capacidad en el momento del cambio.

    // Nota: Corregí el nombre del campo a 'descripcionTipo' para mayor claridad.
    String descriptionTipo   // Ej: "Transporte de carga pesada nacional"

) {}