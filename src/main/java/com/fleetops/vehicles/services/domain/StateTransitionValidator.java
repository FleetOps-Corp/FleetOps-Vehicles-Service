package com.fleetops.vehicles.services.domain;
// Ubicación lógica del archivo.

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

// @Slf4j: Nos permite dejar un registro en consola cada vez que alguien intenta un cambio de estado prohibido.
@Slf4j
// @Component: Registra la clase en Spring para poder inyectarla en el servicio de vehículos.
@Component
public class StateTransitionValidator {
// PATRÓN DE DISEÑO APLICADO: Finite State Machine (FSM - Máquina de Estados Finitos).
// Define una "Aduana" estricta de hacia dónde puede moverse un vehículo. Evita saltos ilógicos.
// Ejemplo: Un avión no puede pasar de estar "Volando" a "Estacionado" sin pasar por "Aterrizando".

    // Creamos un diccionario gigante y altamente optimizado (EnumMap) que funcionará como nuestro "Mapa de Rutas Permitidas".
    private static final Map<EstadoVehiculo, Set<EstadoVehiculo>> TRANSICIONES_PERMITIDAS = new EnumMap<>(EstadoVehiculo.class);

    // Bloque 'static': Se ejecuta UNA SOLA VEZ cuando arranca el programa. Llena el mapa con las reglas oficiales.
    static {
        // REGLAS DE NEGOCIO: Rutas de estado.
        // Si el camión está DISPONIBLE, solo se le permite saltar a RESERVADO, MANTENIMIENTO o FUERA_DE_SERVICIO.
        TRANSICIONES_PERMITIDAS.put(EstadoVehiculo.DISPONIBLE, EnumSet.of(EstadoVehiculo.RESERVADO, EstadoVehiculo.EN_MANTENIMIENTO, EstadoVehiculo.FUERA_DE_SERVICIO));
        
        // Si el camión está RESERVADO, puede volver a DISPONIBLE o dañarse (FUERA_DE_SERVICIO), pero NO puede saltar directo al taller.
        TRANSICIONES_PERMITIDAS.put(EstadoVehiculo.RESERVADO, EnumSet.of(EstadoVehiculo.DISPONIBLE, EstadoVehiculo.EN_MANTENIMIENTO, EstadoVehiculo.FUERA_DE_SERVICIO));
        
        // Si el camión está EN MANTENIMIENTO, solo puede ser liberado (DISPONIBLE) o declarado inservible (FUERA_DE_SERVICIO).
        TRANSICIONES_PERMITIDAS.put(EstadoVehiculo.EN_MANTENIMIENTO, EnumSet.of(EstadoVehiculo.DISPONIBLE, EstadoVehiculo.FUERA_DE_SERVICIO));
        
        // Si el camión está FUERA DE SERVICIO, la ÚNICA salida es pasar por DISPONIBLE tras ser revisado.
        TRANSICIONES_PERMITIDAS.put(EstadoVehiculo.FUERA_DE_SERVICIO, EnumSet.of(EstadoVehiculo.DISPONIBLE));
    }

    public boolean isValidTransition(EstadoVehiculo estadoActual, EstadoVehiculo estadoNuevo) {
    // Método que revisa el mapa de rutas para ver si el salto solicitado es legal.

        // Si falta alguno de los dos datos, deniega el cambio.
        if (estadoActual == null || estadoNuevo == null) {
            return false;
        }
        
        // Si el estado no cambia (se queda igual), se considera un movimiento válido (no hace daño).
        if (estadoActual == estadoNuevo) {
            return true;
        }
        
        // Busca en el diccionario cuáles son las rutas permitidas partiendo desde el "estadoActual".
        Set<EstadoVehiculo> permitidos = TRANSICIONES_PERMITIDAS.get(estadoActual);
        
        // Retorna verdadero si la ruta está en la lista de permitidos.
        // Ejemplo: Si intentas pasar de MANTENIMIENTO a RESERVADO directo, el sistema ve que no está en la lista y retorna falso.
        return permitidos != null && permitidos.contains(estadoNuevo);
    }

    public void validateTransition(EstadoVehiculo estadoActual, EstadoVehiculo estadoNuevo) {
    // Método estricto que interrumpe la aplicación si descubre un salto ilegal.

        // Llama al método de arriba para preguntar si es legal. Si responde falso (!)...
        if (!isValidTransition(estadoActual, estadoNuevo)) {
            
            // Escribe en rojo en la consola que hubo un intento de romper la regla.
            log.warn("Transición de estado inválida: {} → {}", estadoActual, estadoNuevo);
            
            // Lanza una excepción que bloquea la base de datos y le avisa al cliente que su petición es ilegal.
            // Ejemplo: Le muestra al mecánico en pantalla: "Transición de estado no permitida: EN_MANTENIMIENTO → RESERVADO".
            throw new IllegalStateException(
                    "Transición de estado no permitida: " + estadoActual + " → " + estadoNuevo
            );
        }
        
        // Si el cambio era legal, deja una nota oculta (modo debug) para los desarrolladores de que todo salió bien.
        log.debug("Transición de estado válida: {} → {}", estadoActual, estadoNuevo);
    }
}