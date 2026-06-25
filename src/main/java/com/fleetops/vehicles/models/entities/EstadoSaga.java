// Define la "carpeta" lógica del proyecto donde residen las entidades del modelo.
package com.fleetops.vehicles.models.entities;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: Saga Orchestration Lifecycle (Ciclo de vida de la Saga)
// ¿Qué hace? Define los pasos permitidos para una transacción que ocurre en varios microservicios.
// Es una Máquina de Estados (Finite State Machine) que asegura que la operación sea atómica 
// (o todo termina bien, o todo se deshace).
// =========================================================================================
public enum EstadoSaga {

    // =========================================================================================
    // Estado: INICIADA
    // ¿Qué hace? Punto de partida. El sistema ha recibido la orden y ha persistido el intento.
    // Ejemplo: Entregaste los papeles en la recepción, pero nadie los ha empezado a revisar.
    // =========================================================================================
    INICIADA,

    // =========================================================================================
    // Estado: EN_PROGRESO
    // ¿Qué hace? Indica que el orquestador está llamando a los otros servicios (facturación, 
    // inventario, asignaciones).
    // Ejemplo: El trámite está "en curso"; el mecánico está revisando el auto y el sistema 
    // de pagos está verificando la tarjeta.
    // =========================================================================================
    EN_PROGRESO,

    // =========================================================================================
    // Estado: COMPLETADA
    // ¿Qué hace? El punto final exitoso. Todos los pasos del proceso fueron aprobados.
    // Ejemplo: Entregaste papeles, pagaste, y ya tienes las llaves. El ciclo cerró perfectamente.
    // =========================================================================================
    COMPLETADA,

    // =========================================================================================
    // Estado: FALLIDA
    // ¿Qué hace? Indica que un paso crítico no pudo completarse.
    // REGLA DE NEGOCIO: Este estado es el disparador para iniciar la compensación.
    // Ejemplo: El servicio de facturación se cayó, por lo que no se puede continuar.
    // =========================================================================================
    FALLIDA,

    // =========================================================================================
    // Estado: COMPENSADA
    // ¿Qué hace? El estado "sanador". Ejecuta acciones contrarias (rollbacks) a los pasos 
    // que ya se habían realizado para volver al sistema a un estado consistente.
    //
    // REGLA DE NEGOCIO: Integridad de Datos.
    // Si la saga falla, se compensa para evitar que los vehículos queden bloqueados 
    // permanentemente en un estado intermedio.
    // Ejemplo: Como no pasaste la prueba, te devolvieron tus papeles y liberaron la reserva.
    // =========================================================================================
    COMPENSADA
}