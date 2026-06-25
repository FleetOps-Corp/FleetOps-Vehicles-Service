// Define la "carpeta lógica" donde agrupamos los sobres de respuesta de nuestra API.
package com.fleetops.vehicles.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: Saga Response DTO (Data Transfer Object)
// ¿Qué hace? Este objeto es el "Expediente de seguimiento" de un proceso distribuido.
// Como las Sagas pueden fallar o tardar tiempo, este DTO permite que el administrador 
// vea en qué paso se quedó el trámite o si falló por qué motivo.
// =========================================================================================
public record SagaResponse(
        
        // Identificador único de la transacción distribuida (Saga).
        UUID idSaga,
        
        // ID del vehículo involucrado en la transacción.
        UUID idVehiculo,
        
        // El tipo de acción que se está ejecutando (Ej: "RESERVA_VEHICULO").
        String tipoOperacion,
        
        // REGLA DE NEGOCIO: Seguimiento de Estado.
        // Indica la etapa del proceso (INICIADA, COMPLETADA, FALLIDA, COMPENSADA).
        // Ejemplo: Si el sistema de pagos falló, aquí verás "FALLIDA".
        String estadoSaga,
        
        // La clave única de idempotencia que se usó para esta Saga.
        // Ejemplo: Evita que procesemos el mismo pago 2 veces.
        String claveIdempotencia,
        
        // REGLA DE NEGOCIO: Política de Reintentos.
        // Cuenta cuántas veces el sistema ha intentado resolver este paso antes de rendirse.
        // Ejemplo: Si el microservicio de Asignaciones estaba caído, aquí verás "3" intentos previos.
        Integer intentos,
        
        // El paquete de datos (JSON) original que disparó la Saga.
        // Ejemplo: Contiene toda la información de la reserva original para poder reintentar.
        String payload,
        
        // REGLA DE NEGOCIO: Observabilidad de Fallos.
        // Si la Saga falló, aquí se guarda el mensaje técnico del error.
        // Ejemplo: "Timeout de conexión al servicio de pagos".
        String ultimoError,
        
        // REGLA DE NEGOCIO: Mecanismo de Compensación.
        // Si el proceso falló y se tuvo que deshacer (compensar), aquí se registra qué acción 
        // contraria se tomó (ej: "RESERVA_CANCELADA").
        // Ejemplo: "Se canceló la reserva X porque el vehículo ya no estaba disponible".
        String compensadoPor,
        
        // Fecha y hora de creación de la Saga.
        LocalDateTime creadoEn,
        
        // Fecha de la última actualización (cuando cambió de estado).
        LocalDateTime actualizadoEn,
        
        // =========================================================================================
        // PATRÓN APLICADO: DTO Flattening (Aplanamiento de datos)
        // Incluimos la placa directamente para que el administrador identifique el vehículo 
        // sin tener que hacer consultas adicionales a la tabla de vehículos.
        // =========================================================================================
        String numeroPlaca
) {}