// Define la carpeta donde agrupamos la estructura de los mensajes de error.
package com.fleetops.vehicles.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

// =========================================================================================
// PATRÓN DE DISEÑO: Standard Error Response (Respuesta de Error Estandarizada)
// ¿Qué hace? Garantiza que, sin importar por qué falle el sistema, el cliente (Web o App) 
// siempre reciba la misma estructura de JSON.
// Ejemplo: La App de React siempre sabrá que el texto del error vive en "message", 
// evitando que el Frontend se rompa inesperadamente.
// =========================================================================================

// =========================================================================================
// ANOTACIÓN: @JsonInclude(JsonInclude.Include.NON_NULL)
// ¿Qué hace? Le dice a la librería Jackson (la que crea el JSON) que si una variable está vacía (null),
// simplemente no la incluya en el mensaje final.
// Ejemplo: Si el error no tiene una "lista de errores", la variable 'errors' desaparece,
// haciendo el JSON más limpio y ligero.
// =========================================================================================
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    // Guarda la fecha y hora exacta del "estallido". Ejemplo: "2026-06-19T10:15:30".
    private LocalDateTime timestamp;

    // Código de estado HTTP. Ejemplo: 400 (Bad Request), 404 (No encontrado), 500 (Servidor roto).
    private int status;

    // Título corto del problema. Ejemplo: "Not Found" o "Bad Request".
    private String error;

    // Explicación humana y amigable. 
    // REGLA DE NEGOCIO: Abstracción de Seguridad. No revelamos detalles técnicos del servidor,
    // solo lo que el usuario necesita saber para corregir su acción.
    private String message;

    // La URL que el usuario visitó cuando ocurrió el error. Ejemplo: "/vehiculos/123/reservas".
    private String path;

    // Lista de quejas múltiples (útil para validar formularios).
    // Ejemplo: ["La placa está vacía", "El año es incorrecto"].
    private List<String> errors;

    // =========================================================================================
    // REGLA DE NEGOCIO: Código de Soporte Interno (Supportability).
    // Este campo es un código único (Ej: ERR-VEH-001) para que el equipo de sistemas pueda
    // buscar el problema en el manual de procedimientos sin leer el código fuente.
    // =========================================================================================
    private String errorCode;

    // Constructor básico (para errores sencillos).
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // Constructor completo (para errores complejos que tienen listas de detalles o códigos técnicos).
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, List<String> errors, String errorCode) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.errors = errors;
        this.errorCode = errorCode;
    }

    // ==================== GETTERS Y SETTERS ====================
    // Son necesarios para que la librería Jackson pueda "leer" estos datos privados 
    // y convertirlos en el JSON que el navegador recibe.
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
}