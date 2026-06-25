package com.fleetops.vehicles.services.domain;
// Ubicación lógica del archivo.

import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.repositories.SagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component; 

// @Slf4j: Genera la herramienta para imprimir alertas en la consola de la aplicación.
@Slf4j
// @Component: Registra este validador en la memoria de Spring para usarlo en los servicios.
@Component
// @RequiredArgsConstructor: Construye automáticamente el enlace con los repositorios sin escribir código manual.
@RequiredArgsConstructor 
public class IdempotencyValidator {
// PATRÓN DE DISEÑO APLICADO: Idempotency Barrier (Barrera de Idempotencia).
// Garantiza que si el usuario manda la misma solicitud 10 veces por error de red, el sistema solo procese la primera.
// Ejemplo: Si aprietas el botón de "Pagar" dos veces porque la página se congeló, esto evita que te cobren doble.

    // Herramienta para consultar si el código único ya existe en una reserva normal.
    private final ReservaRepository reservaRepository;
    
    // Herramienta para consultar si el código único ya existe en una Saga (Trámite distribuido).
    private final SagaRepository sagaRepository;

    public boolean isDuplicate(String claveIdempotencia) {
    // Método que busca si la clave (el ticket del cliente) ya fue usada antes.

        // Si la clave viene vacía, no hace validación y la deja pasar.
        if (claveIdempotencia == null || claveIdempotencia.isBlank()) {
            return false;
        }

        // Revisa en la base de datos de reservas si la clave ya está registrada.
        boolean existeEnReserva = reservaRepository.existsByClaveIdempotencia(claveIdempotencia);
        
        // Revisa en la base de datos de Sagas si la clave ya está registrada.
        boolean existeEnSaga = sagaRepository.existsByClaveIdempotencia(claveIdempotencia);

        // Si la encontró en cualquiera de los dos lados, significa que es un clon (un duplicado).
        if (existeEnReserva || existeEnSaga) {
            // Dispara una alerta en la consola advirtiendo del duplicado.
            log.warn("Clave de idempotencia duplicada detectada: {}", claveIdempotencia);
        }

        // Retorna verdadero si es un duplicado, falso si es una petición original.
        return existeEnReserva || existeEnSaga;
    }

    public void validateNotDuplicate(String claveIdempotencia) {
    // Método estricto que interrumpe violentamente el programa si detecta un clon.
    
        // Llama al método de arriba para verificar si es un duplicado.
        if (isDuplicate(claveIdempotencia)) {
            
            // Si es duplicado, lanza un error fatal que detiene la reserva.
            // Ejemplo: Le responde al celular del conductor "Esta reserva ya está siendo procesada, no presione otra vez".
            throw new IllegalStateException(
                    "Ya existe una operación con esa clave de idempotencia: " + claveIdempotencia
            );
        }
    }
}