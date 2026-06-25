// Define la "carpeta lógica" donde agrupamos los sobres de respuesta de nuestra API.
package com.fleetops.vehicles.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: DTO (Data Transfer Object) implementado como 'record'
// ¿Qué hace? Funciona como un recibo o "voucher" digital. Una vez que el sistema confirma
// la reserva, este objeto viaja hacia el cliente con todos los detalles del trámite.
// Ejemplo: Es como el comprobante que recibes por correo después de comprar un tiquete de avión.
// 
// PATRÓN DE DISEÑO DETECTADO: DTO Flattening (Aplanamiento de datos)
// ¿Qué hace? Incluye datos del vehículo directamente aquí para que el cliente no tenga 
// que hacer otra consulta extra.
// =========================================================================================
public record ReservaResponse(
    
    // Identificador único de esta reserva en nuestra base de datos.
    UUID idReserva, 
    
    // ID del vehículo que se reservó.
    UUID idVehiculo, 
    
    // Estado actual del proceso (Ej: "CONFIRMADA", "PENDIENTE").
    String estadoReserva, 
    
    // ID externo asignado por el sistema de gestión de viajes (Asignaciones).
    // REGLA DE NEGOCIO: Trazabilidad Inter-sistemas.
    // Permite que el sistema de asignaciones sepa exactamente qué reserva le corresponde a este viaje.
    String idAsignacionExt, 
    
    // Quién es el responsable humano o sistema que solicitó el vehículo.
    String solicitadoPor, 
    
    // Fecha y hora exacta de inicio del uso del vehículo.
    LocalDateTime fechaInicio, 
    
    // Fecha y hora exacta en la que se debe devolver el vehículo.
    LocalDateTime fechaFin, 
    
    // El ticket que se usó para evitar duplicados en el proceso.
    // REGLA DE NEGOCIO: Auditoría de Idempotencia.
    // Si hay un conflicto, usamos esta clave para verificar qué solicitud fue la original.
    String claveIdempotencia, 
    
    // ID que agrupa todo el proceso de la Saga (trámite distribuido).
    // Ejemplo: Si el pago falla, buscamos este ID para revertir todos los pasos anteriores.
    UUID idSaga, 

    // =========================================================================================
    // DATOS APLANADOS (Flattening):
    // El cliente necesita ver los detalles del vehículo (placa, capacidad, etc.) 
    // en la misma pantalla de la reserva. Enviar estos campos aquí ahorra al cliente 
    // tener que hacer otra consulta al servidor (optimización de red).
    // =========================================================================================
    String numeroPlaca,      // Ej: "CP-001"
    String nombreTipo,       // Ej: "Camión Carga Pesada"
    String descripcionTipo,  // Ej: "Transporte de carga pesada nacional"
    Integer kilometraje,     // Kilometraje al momento de la reserva.
    Double capacidadCarga    // Capacidad en toneladas o kilos.

) {}