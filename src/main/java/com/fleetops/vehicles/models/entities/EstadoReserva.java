// Define la "carpeta" lógica del proyecto donde residen las entidades del modelo.
package com.fleetops.vehicles.models.entities;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: Finite State Machine (Máquina de Estados)
// ¿Qué hace? Define un ciclo de vida cerrado. Una reserva no puede saltar de "PENDIENTE" a 
// "CANCELADA" sin pasar por reglas específicas. Esto garantiza consistencia: el sistema 
// siempre sabe en qué etapa exacta está el trámite.
//
// Ejemplo: Es como un semáforo; solo puede estar en rojo, amarillo o verde. Nunca en "morado".
// =========================================================================================
public enum EstadoReserva {

    // =========================================================================================
    // Estado: PENDIENTE
    // ¿Qué hace? Es el punto de partida cuando el cliente solicita un vehículo.
    // 
    // REGLA DE NEGOCIO: Bloqueo de Recurso.
    // Al entrar en este estado, el sistema debe "reservar" (apartar) el vehículo para que
    // nadie más lo pueda tomar, aunque la confirmación final esté pendiente de revisión.
    // Ejemplo: Como cuando pides una pizza por app; el restaurante recibió tu orden, pero aún 
    // no la han confirmado ni metido al horno.
    // =========================================================================================
    PENDIENTE,

    // =========================================================================================
    // Estado: CONFIRMADA
    // ¿Qué hace? Indica que la reserva ha sido aprobada tras cumplir con los filtros de negocio.
    // 
    // REGLA DE NEGOCIO: Ventana Operativa.
    // Solo llega a este estado si el sistema validó que la fecha solicitada está disponible y 
    // el usuario es apto. Es el estado donde el compromiso se vuelve oficial.
    // Ejemplo: El cajero del banco verificó tu firma y te entregó el dinero.
    // =========================================================================================
    CONFIRMADA,

    // =========================================================================================
    // Estado: FALLIDA
    // ¿Qué hace? Representa el fin del proceso debido a un error externo.
    // 
    // Ejemplo: Intentaste rentar el auto, pero el sistema de pagos rechazó tu tarjeta o hubo 
    // un error de conexión con el banco. La reserva muere aquí.
    // =========================================================================================
    FALLIDA,

    // =========================================================================================
    // Estado: CANCELADA
    // ¿Qué hace? Es el estado final de anulación intencional (compensación).
    // 
    // REGLA DE NEGOCIO: Reversión de Recursos.
    // Si la reserva pasa a este estado, el sistema DEBE liberar el vehículo automáticamente 
    // para que vuelva a estar DISPONIBLE para otros usuarios.
    // Ejemplo: Llamaste para decir que ya no vas a viajar, así que el operador tacha tu nombre 
    // en la agenda.
    // =========================================================================================
    CANCELADA
}