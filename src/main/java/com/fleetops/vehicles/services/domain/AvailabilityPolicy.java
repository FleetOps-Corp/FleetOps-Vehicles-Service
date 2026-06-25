package com.fleetops.vehicles.services.domain;
// Define la carpeta del sistema donde vive este archivo. Pertenece a la capa de "Dominio" (reglas de negocio puras).
// Ejemplo: Es como la carpeta del "Manual de Operaciones" de la empresa.

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

// @Slf4j: Anotación de Lombok que crea una herramienta para escribir mensajes en la bitácora del servidor.
// Ejemplo: Nos permite registrar en la consola "El camión se rechazó por falta de SOAT".
@Slf4j
// @Component: Anotación de Spring que registra esta clase en la memoria central para poder reutilizarla.
// Ejemplo: Es como contratar a un "Inspector de Calidad"; Spring lo tiene listo siempre que alguien necesite inspeccionar un camión.
@Component
public class AvailabilityPolicy {
// PATRÓN DE DISEÑO APLICADO: Policy Pattern (Patrón de Política).
// Agrupa reglas de negocio complejas en una clase separada en lugar de tener "if/else" regados por todos los servicios.
// Ejemplo: Si mañana el gobierno exige un nuevo seguro, solo modificamos esta clase y todo el sistema se actualiza.

    public boolean isAvailable(Vehiculo vehiculo) {
    // Método que verifica de forma rápida si un vehículo está teóricamente libre.
    // REGLA DE NEGOCIO: Un vehículo solo está libre si existe físicamente, no ha sido dado de baja y su estado es "DISPONIBLE".
    
        // Retorna verdadero SOLO SI se cumplen estas tres condiciones al mismo tiempo:
        return vehiculo != null
                // Condición 1: Que el objeto vehículo exista (no sea nulo).
                && Boolean.TRUE.equals(vehiculo.getActivo())
                // Condición 2: Que el vehículo esté marcado como Activo en el sistema (no esté en la papelera).
                && vehiculo.getEstadoVehiculo() == EstadoVehiculo.DISPONIBLE;
                // Condición 3: Que su etiqueta de estado diga exactamente "DISPONIBLE".
                // Ejemplo: Si el camión está "EN_MANTENIMIENTO", esta prueba falla y devuelve falso.
    }

    public boolean isAvailableForReservation(Vehiculo vehiculo) {
    // Método más riguroso que evalúa si un vehículo es legalmente apto para salir a un viaje.
    
        // Reutiliza el método de arriba. Si el vehículo no está teóricamente libre, aborta enseguida.
        if (!isAvailable(vehiculo)) {
            // Retorna falso porque no está libre.
            return false;
        }
        
        // Extrae la fecha exacta del día de hoy.
        // Ejemplo: Si hoy es 20 de Junio de 2026, guarda ese dato para comparar papeles.
        LocalDate hoy = LocalDate.now();
        
        // REGLA DE NEGOCIO: Legalidad Documental.
        // Verifica si la fecha de vencimiento del SOAT es ANTES (isBefore) del día de hoy (es decir, ya se venció).
        if (vehiculo.getFechaSoat() != null && vehiculo.getFechaSoat().isBefore(hoy)) {
            
            // Deja un registro en la consola advirtiendo el motivo del rechazo.
            log.warn("Vehículo {} rechazado: SOAT vencido", vehiculo.getNumeroPlaca());
            
            // Retorna falso porque es ilegal despachar el camión.
            // Ejemplo: El SOAT venció ayer, por lo que el sistema congela la asignación.
            return false;
        }
        
        // Verifica de la misma manera si la Revisión Técnico Mecánica (RTM) ya está vencida.
        if (vehiculo.getFechaRtm() != null && vehiculo.getFechaRtm().isBefore(hoy)) {
            
            // Registra en la bitácora que la RTM está vencida.
            log.warn("Vehículo {} rechazado: RTM vencida", vehiculo.getNumeroPlaca());
            
            // Retorna falso para impedir el viaje.
            return false;
        }
        
        // Si pasó todas las pruebas operativas y legales, retorna verdadero (Aprobado).
        return true;
    }
}