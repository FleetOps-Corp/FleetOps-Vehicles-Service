package com.fleetops.vehicles.services.domain;
// Ubicación lógica del archivo.

import com.fleetops.vehicles.dto.request.ReservaRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, ReservaRequest> {
// Clase que ejecuta la lógica real detrás de la anotación @ValidDateRange. Actúa como el guardia de seguridad.

    // @Override: Indica que estamos sobreescribiendo el método 'isValid' obligatorio para cualquier validador.
    @Override
    public boolean isValid(ReservaRequest request, ConstraintValidatorContext context) {
    // Método que evalúa si los datos de la reserva tienen fechas lógicas. Retorna Verdadero o Falso.

        // Si la petición está totalmente vacía, la dejamos pasar (otras validaciones como @NotNull atraparán esto).
        if (request == null) {
            return true;
        }

        // Usamos la sintaxis de Record para extraer la fecha en la que inicia el viaje.
        // Ejemplo: 15 de Octubre.
        LocalDateTime fechaInicio = request.fechaInicio();
        
        // Extraemos la fecha en la que termina el viaje.
        // Ejemplo: 20 de Octubre.
        LocalDateTime fechaFin = request.fechaFin();

        // Si falta alguna de las dos fechas, aprobamos temporalmente (de nuevo, el @NotNull de los campos se encarga de esto).
        if (fechaFin == null || fechaInicio == null) {
            return true;
        }

        // REGLA DE NEGOCIO: Cronología Lógica.
        // Comprueba matemáticamente que la fecha de fin sea DESPUÉS de la fecha de inicio.
        boolean esValido = fechaFin.isAfter(fechaInicio);

        // Si la comprobación falla (ejemplo: Fin es el 10 de Oct, pero Inicio es el 15 de Oct)...
        if (!esValido) {
            
            // Apaga el error genérico feo de Java.
            context.disableDefaultConstraintViolation();
            
            // Construye un error amigable, y le dice al sistema que el culpable específico es el campo "fechaFin".
            // Ejemplo: Esto hace que en el Frontend (React/Angular), la casilla de "Fecha Final" se pinte de rojo.
            context.buildConstraintViolationWithTemplate(
                    "El viaje no puede terminar antes de haber empezado. Revisa las fechas.")
                .addPropertyNode("fechaFin")
                .addConstraintViolation();
        }

        // Retorna el resultado final de la evaluación.
        return esValido;
    }
}