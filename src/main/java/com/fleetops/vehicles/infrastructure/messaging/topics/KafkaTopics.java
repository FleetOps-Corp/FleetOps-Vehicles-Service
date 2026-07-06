package com.fleetops.vehicles.infrastructure.messaging.topics;

public final class KafkaTopics {

    private KafkaTopics(){}

    public static final String VEHICLE_REQUEST =
            "fleetops.vehiculos.solicitar";

    public static final String VEHICLE_CONFIRMED =
            "fleetops.asignaciones.vehiculo.confirmado";

    public static final String VEHICLE_FAILED =
            "fleetops.asignaciones.vehiculo.fallido";

    public static final String ASSIGNMENT_COMPLETED =
        "fleetops.asignaciones.completada";
  
    /** Asignaciones → Vehículos: cancelar/liberar una asignación confirmada. */
    public static final String VEHICLE_RELEASE =
            "fleetops.vehiculos.liberar";
}