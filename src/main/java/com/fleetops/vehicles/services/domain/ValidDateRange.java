package com.fleetops.vehicles.services.domain;
// Ubicación lógica del archivo.

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

// @Documented: Indica que esta anotación debe aparecer en la documentación oficial (JavaDoc) cuando se genere.
@Documented
// @Constraint: Conecta esta etiqueta decorativa con la clase real que hace el cálculo (DateRangeValidator).
// Ejemplo: Es como un letrero que dice "Verificador de Edad", y esta anotación lo conecta con el guardia real que pide la cédula.
@Constraint(validatedBy = DateRangeValidator.class)
// @Target: Define DÓNDE se puede usar esta anotación. 'ElementType.TYPE' significa que se pone encima de toda una Clase (como un DTO).
@Target({ ElementType.TYPE }) 
// @Retention: Indica que esta anotación debe estar viva y disponible mientras el programa se está ejecutando (RUNTIME).
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
// PATRÓN DE DISEÑO: Declarative Validation (Validación Declarativa).
// Creamos nuestra propia anotación personalizada (como @NotNull) para aplicar reglas complejas con solo escribir @ValidDateRange.

    // Define el mensaje de error por defecto si la validación llega a fallar.
    // Ejemplo: Si envían fechas chuecas, este es el texto que se le devolverá a la pantalla del usuario.
    String message() default "El campo 'fechaFin' debe ser posterior al campo 'fechaInicio'";

    // Requisito interno de Java Validation para agrupar validaciones (ej. validar solo al actualizar y no al crear).
    Class<?>[] groups() default {};

    // Requisito interno de Java Validation para enviar cargas útiles de error (ej. definir si el error es Leve o Grave).
    Class<? extends Payload>[] payload() default {};
}