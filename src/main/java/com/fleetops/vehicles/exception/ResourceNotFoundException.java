// Define la "carpeta" lógica dentro de la estructura de paquetes del proyecto.
package com.fleetops.vehicles.exception;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: Specialized Domain Exception (Excepción Especializada)
// ¿Qué hace? Crea una plantilla estandarizada exclusivamente para cuando el sistema
// busca un dato en la base de datos y no lo encuentra.
//
// ¿Por qué es necesario? Para evitar que cada desarrollador invente una forma diferente
// de decir "no existe", garantizando que la respuesta al Frontend sea siempre coherente.
// =========================================================================================

// Esta clase extiende de RuntimeException, lo que significa que es un error "unchecked" 
// (no nos obliga a declararlo explícitamente en la firma de cada método).
public class ResourceNotFoundException extends RuntimeException {

    // Constructor simple: Se usa para lanzar un mensaje de error directo, sin necesidad de ensamblar nada.
    // Ejemplo: throw new ResourceNotFoundException("El vehículo ha sido eliminado.");
    public ResourceNotFoundException(String message) {
        // Pasa el mensaje recibido directamente al constructor de la clase padre (RuntimeException).
        super(message);
        // El núcleo de Java almacena este mensaje en su memoria interna para ser recuperado luego.
    }

    // Constructor "inteligente": Recibe los tres pedazos de información necesarios para armar un mensaje profesional.
    // Ejemplo de parámetros: "Vehículo", "Placa", "BOG123".
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        
        // =========================================================================================
        // REGLA DE NEGOCIO: Uniformidad en el Mensaje de Error (UX).
        // Usamos String.format para inyectar los valores en una plantilla fija.
        // Esto garantiza que todos los errores de "No encontrado" tengan el mismo formato visual.
        // Ejemplo generado: "Vehículo no encontrado con Placa: 'BOG123'".
        // =========================================================================================
        super(String.format("%s no encontrado con %s: '%s'", resourceName, fieldName, fieldValue));
        
        // La instrucción 'super' envía el mensaje ya ensamblado al núcleo del manejo de excepciones de Java.
    }
}