// Define la "carpeta" lógica del proyecto donde residen las entidades de datos.
package com.fleetops.vehicles.models.entities;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: Finite State Machine (Máquina de Estados)
// ¿Qué hace? Clasifica la disponibilidad del activo físico en categorías excluyentes.
// REGLA DE NEGOCIO: Evita ambigüedades. Un vehículo nunca puede estar "parcialmente en mantenimiento"
// y "disponible" al mismo tiempo; debe estar en un solo estado para garantizar la seguridad operativa.
// =========================================================================================

public enum EstadoVehiculo {

    // =========================================================================================
    // Estado: DISPONIBLE
    // ¿Qué hace? Indica que el vehículo está operativo, con papeles al día y listo para asignación.
    // REGLA DE NEGOCIO: Solo en este estado el sistema permite iniciar una reserva nueva.
    // Ejemplo: El taxi está vacío, tiene el tanque lleno y la luz verde encendida.
    // =========================================================================================
    DISPONIBLE,

    // =========================================================================================
    // Estado: RESERVADO
    // ¿Qué hace? El vehículo ya tiene un compromiso de uso futuro o inmediato.
    // REGLA DE NEGOCIO: Bloqueo de concurrencia. Impide que otros usuarios reserven el mismo 
    // activo en la misma ventana de tiempo, evitando el sobrecupo.
    // Ejemplo: El taxi tiene un letrero de "Ocupado" porque va en camino a recoger a alguien.
    // =========================================================================================
    RESERVADO,

    // =========================================================================================
    // Estado: EN_MANTENIMIENTO
    // ¿Qué hace? El vehículo está bajo intervención técnica en el taller.
    // REGLA DE NEGOCIO: Bloqueo de seguridad. Ningún sistema (App, Web, API) puede 
    // marcar este vehículo como disponible hasta que un mecánico cambie manualmente el estado.
    // Ejemplo: El camión tiene el motor desarmado; es físicamente imposible que salga a trabajar.
    // =========================================================================================
    EN_MANTENIMIENTO,

    // =========================================================================================
    // Estado: FUERA_DE_SERVICIO
    // ¿Qué hace? Es el estado de emergencia para vehículos inoperativos por causas externas.
    // REGLA DE NEGOCIO: Auditoría obligatoria. Para entrar a este estado, el sistema 
    // debe exigir obligatoriamente un "Motivo" o "Reporte" de incidente.
    // Ejemplo: El camión se pinchó en carretera, fue robado o sufrió un siniestro vial.
    // =========================================================================================
    FUERA_DE_SERVICIO

} // Cierre de la clase Enum EstadoVehiculo.