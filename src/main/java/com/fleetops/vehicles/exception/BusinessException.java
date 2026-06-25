// Define la "carpeta" lógica donde agrupamos los errores personalizados del sistema de vehículos.
package com.fleetops.vehicles.exception;

// =========================================================================================
// PATRÓN DE DISEÑO: Custom Exception Pattern (Patrón de Excepciones Personalizadas)
// ¿Qué hace? Nos permite crear errores que hablan el "idioma" de la empresa en lugar de
// errores técnicos confusos de Java.
// Ejemplo: En lugar de arrojar un "NullPointerException" (que es feo y técnico), lanzamos un 
// "BusinessException" que dice "No puedes reservar un vehículo que ya está reservado".
// =========================================================================================
public class BusinessException extends RuntimeException {

    // =========================================================================================
    // EXPLICACIÓN TÉCNICA: Extensión de RuntimeException
    // ¿Qué hace? Al heredar de 'RuntimeException', esta excepción es "unchecked".
    // Esto significa que NO nos obliga a escribir "throws BusinessException" en todos nuestros 
    // métodos, lo que mantiene nuestro código mucho más limpio y fácil de leer.
    // Ejemplo: Es como una "tarjeta roja" que podemos sacar en cualquier momento sin tener 
    // que avisarle a todo el estadio antes de empezar el partido.
    // =========================================================================================

    public BusinessException(String message) {
        // =========================================================================================
        // Constructor: Recibe el mensaje explicativo que queremos mostrar al usuario.
        // Ejemplo: Recibe el texto "El vehículo no puede estar en mantenimiento si tiene viajes activos".
        // =========================================================================================

        super(message);
        
        // =========================================================================================
        // super(message): Llama al constructor de la clase padre (RuntimeException).
        // ¿Qué hace? Pasa el mensaje hacia arriba para que Java lo guarde en la memoria y
        // sepa exactamente qué texto mostrar cuando el error ocurra.
        // Ejemplo: Es como entregarle el reporte de falla al supervisor general (Java) para 
        // que lo archive y podamos imprimirlo en pantalla cuando sea necesario.
        // =========================================================================================
    }
}