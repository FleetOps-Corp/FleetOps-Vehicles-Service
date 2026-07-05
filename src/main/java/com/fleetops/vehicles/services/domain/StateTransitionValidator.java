package com.fleetops.vehicles.services.domain;

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class StateTransitionValidator {

    private static final Map<EstadoVehiculo, Set<EstadoVehiculo>> TRANSICIONES_PERMITIDAS = new EnumMap<>(
            EstadoVehiculo.class);

    static {
        TRANSICIONES_PERMITIDAS.put(EstadoVehiculo.DISPONIBLE,
                EnumSet.of(EstadoVehiculo.EN_MANTENIMIENTO, EstadoVehiculo.FUERA_DE_SERVICIO));

        TRANSICIONES_PERMITIDAS.put(EstadoVehiculo.EN_MANTENIMIENTO,
                EnumSet.of(EstadoVehiculo.DISPONIBLE, EstadoVehiculo.FUERA_DE_SERVICIO));

        TRANSICIONES_PERMITIDAS.put(EstadoVehiculo.FUERA_DE_SERVICIO,
                EnumSet.of(EstadoVehiculo.DISPONIBLE));
    }

    public boolean isValidTransition(EstadoVehiculo estadoActual, EstadoVehiculo estadoNuevo) {
        if (estadoActual == null || estadoNuevo == null) {
            return false;
        }
        if (estadoActual == estadoNuevo) {
            return true;
        }
        Set<EstadoVehiculo> permitidos = TRANSICIONES_PERMITIDAS.get(estadoActual);
        return permitidos != null && permitidos.contains(estadoNuevo);
    }

    public void validateTransition(EstadoVehiculo estadoActual, EstadoVehiculo estadoNuevo) {
        if (!isValidTransition(estadoActual, estadoNuevo)) {
            log.warn("Transición de estado inválida: {} → {}", estadoActual, estadoNuevo);
            throw new IllegalStateException(
                    "Transición de estado no permitida: " + estadoActual + " → " + estadoNuevo);
        }
        log.debug("Transición de estado válida: {} → {}", estadoActual, estadoNuevo);
    }
}
