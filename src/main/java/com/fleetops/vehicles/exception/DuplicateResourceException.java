// Define la "carpeta" lógica donde agrupamos los errores personalizados del sistema.
package com.fleetops.vehicles.exception;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: Specialized Domain Exception (Excepción Especializada)
// ¿Qué hace? Crea una clase dedicada a un error específico ("Dato Duplicado").
//
// ¿Por qué es útil? Estandariza la forma en que el sistema se queja cuando intentas crear algo
// que ya existe (como una placa duplicada), evitando que cada programador invente su propia
// forma de escribir el error.
// =========================================================================================
public class DuplicateResourceException extends RuntimeException {
    
    // Al heredar de 'RuntimeException', esta excepción se dispara inmediatamente cuando ocurre 
    // el error, sin obligar a los desarrolladores a escribir código complejo para "atrapar" el error.

    // Constructor básico: Permite lanzar un mensaje de error libre en situaciones excepcionales.
    public DuplicateResourceException(String message) {
        super(message);
    }

    // =========================================================================================
    // PATRÓN DE DISEÑO: Constructor Inteligente (Builder-like)
    // Este constructor recibe los 3 datos clave para armar un mensaje claro y profesional.
    //
    // REGLA DE NEGOCIO: Unicidad de Recursos.
    // Esta regla garantiza que el sistema no permita duplicidad en campos críticos (como placas 
    // o chasis), informando siempre QUÉ campo específico causó el problema.
    // =========================================================================================
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        
        // String.format: Es una herramienta que toma el "molde" y rellena los huecos ('%s').
        //
        // Ejemplo de uso:
        // Si intentas registrar un vehículo con una placa ya existente, el mensaje resultante será:
        // "Vehículo ya existe con numeroPlaca: 'ABC-123'"
        super(String.format("%s ya existe con %s: '%s'", resourceName, fieldName, fieldValue));
    }
}