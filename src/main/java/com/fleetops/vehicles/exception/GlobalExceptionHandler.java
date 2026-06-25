// Define la "carpeta" lógica del proyecto donde se almacenan las excepciones.
package com.fleetops.vehicles.exception;

// Importa la excepción de JPA que ocurre cuando dos usuarios modifican el mismo dato simultáneamente.
import jakarta.persistence.OptimisticLockException;
// Importa la herramienta de Java para leer detalles de la petición HTTP del navegador.
import jakarta.servlet.http.HttpServletRequest;
// Importa la herramienta de Logger para registrar eventos en la consola del servidor.
import org.slf4j.Logger;
// Importa la fábrica de Loggers.
import org.slf4j.LoggerFactory;
// Importa los estados HTTP predefinidos de Spring (200, 404, 500, etc.).
import org.springframework.http.HttpStatus;
// Importa la respuesta estándar para enviar al cliente (JSON + Código HTTP).
import org.springframework.http.ResponseEntity;
// Importa la clase para acceder a los campos que fallaron la validación.
import org.springframework.validation.FieldError;
// Importa la excepción de Spring que ocurre cuando un DTO falla sus validaciones (@NotNull, etc.).
import org.springframework.web.bind.MethodArgumentNotValidException;
// Importa la anotación para marcar un método como manejador de errores.
import org.springframework.web.bind.annotation.ExceptionHandler;
// Importa la anotación que convierte esta clase en un vigilante global de todos los controladores.
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Importa herramientas de fecha y tiempo.
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
// Importa la lista para agrupar errores.
import java.util.List;
import java.util.Map;
// Importa la herramienta para convertir listas de errores a un formato legible.
import java.util.stream.Collectors;

// =========================================================================================
// PATRÓN DE DISEÑO APLICADO: Global Exception Handler (Manejador Global de Excepciones)
// ¿Qué hace? Actúa como un escudo centralizado. Si algo falla en cualquier rincón del código, 
// la excepción "vuela" hasta aquí para ser procesada sin ensuciar los métodos con bloques try-catch.
// =========================================================================================

// @RestControllerAdvice: Anota esta clase como un vigilante global. Spring la usará para envolver
// los errores de TODOS los controladores en una respuesta JSON bonita y uniforme.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Crea el "Logger" profesional. Es el megáfono para dejar evidencia en los logs
    // del servidor.
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =========================================================================================
    // EXCEPCIÓN: ResourceNotFoundException
    // Atrapa cualquier intento de buscar algo (vehículo, reserva) que no existe en
    // la base de datos.
    // =========================================================================================
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex,
            HttpServletRequest request) {

        // Escribe en la bitácora (log) un mensaje de advertencia con la URL fallida.
        log.warn("Recurso no encontrado | URI: {} | Mensaje: {}", request.getRequestURI(), ex.getMessage());

        // Crea el objeto ErrorResponse con los datos del error para enviárselo al
        // cliente.
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(), // Fecha y hora actual.
                HttpStatus.NOT_FOUND.value(), // Código 404.
                "Not Found", // Título del error.
                ex.getMessage(), // Mensaje explicativo.
                request.getRequestURI() // URL donde ocurrió.
        );

        // Envuelve la respuesta en un ResponseEntity con código 404 y la retorna al
        // cliente.
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // =========================================================================================
    // EXCEPCIÓN: DuplicateResourceException
    // REGLA DE NEGOCIO: Intercepta violaciones de unicidad (como placas repetidas).
    // =========================================================================================
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex,
            HttpServletRequest request) {

        // Registra el conflicto en los logs técnicos del servidor.
        log.warn("Recurso duplicado | URI: {} | Mensaje: {}", request.getRequestURI(), ex.getMessage());

        // Crea el objeto de respuesta para informar que el recurso ya existe.
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(), // Fecha y hora.
                HttpStatus.CONFLICT.value(), // Código 409 (Conflicto).
                "Conflict", // Título.
                ex.getMessage(), // Mensaje detallado.
                request.getRequestURI() // Ruta.
        );

        // Retorna HTTP 409 al cliente.
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // =========================================================================================
    // EXCEPCIÓN: BusinessException
    // REGLA DE NEGOCIO: Central de rechazos operativos (Ej: SOAT vencido).
    // =========================================================================================
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {

        // Registra que una regla interna de FleetOps fue violada.
        log.warn("Regla de negocio violada | URI: {} | Mensaje: {}", request.getRequestURI(), ex.getMessage());

        // Crea la respuesta con estado 422 (Entidad no procesable - ideal para errores
        // lógicos).
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(), // Fecha y hora.
                HttpStatus.UNPROCESSABLE_ENTITY.value(), // Código 422.
                "Unprocessable Entity", // Título.
                ex.getMessage(), // Mensaje de la regla violada.
                request.getRequestURI() // Ruta.
        );

        // Retorna HTTP 422 al cliente.
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    // =========================================================================================
    // EXCEPCIÓN: OptimisticLockException
    // PATRÓN: Optimistic Locking. Ataja errores de edición simultánea por dos
    // usuarios.
    // =========================================================================================
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(OptimisticLockException ex,
            HttpServletRequest request) {

        // Advierte en logs sobre el choque de datos.
        log.warn("Colisión de concurrencia detectada | URI: {}", request.getRequestURI());

        // Crea la respuesta explicando el conflicto de edición.
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(), // Fecha y hora.
                HttpStatus.CONFLICT.value(), // Código 409.
                "Conflict", // Título.
                "El registro fue modificado por otro usuario. Por favor, recargue la página y vuelva a intentarlo.", // REGLA
                                                                                                                     // DE
                                                                                                                     // NEGOCIO
                                                                                                                     // (UX):
                                                                                                                     // Mensaje
                                                                                                                     // amigable.
                request.getRequestURI() // Ruta.
        );

        // Retorna HTTP 409.
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // =========================================================================================
    // EXCEPCIÓN: MethodArgumentNotValidException
    // REGLA DE NEGOCIO: Calidad de datos. Atrapa basura de entrada (ej: campos
    // vacíos).
    // =========================================================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Advierte que la validación de un formulario falló.
        log.warn("Error de validación de entrada | URI: {}", request.getRequestURI());

        // Usa Java Streams para procesar todos los errores encontrados en el DTO de
        // entrada.
        List<String> errors = ex.getBindingResult()
                .getFieldErrors() // Obtiene la lista de campos que fallaron.
                .stream() // Abre el flujo de datos.
                .map(FieldError::getDefaultMessage) // Convierte cada error técnico en un texto amigable.
                .collect(Collectors.toList()); // Junta todo en una lista simple.

        // Crea la respuesta incluyendo la lista de errores múltiples.
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(), // Fecha y hora.
                HttpStatus.BAD_REQUEST.value(), // Código 400.
                "Bad Request", // Título.
                "Error en la validación de los datos enviados", // Resumen.
                request.getRequestURI(), // Ruta.
                errors, // Lista de errores (el campo 'errors' del objeto).
                "VALIDATION_ERROR" // Código técnico.
        );

        // Retorna HTTP 400 con la lista de errores.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // =========================================================================================
    // EXCEPCIÓN: Exception (General)
    // REGLA DE NEGOCIO: Seguridad. Es el paracaídas para errores técnicos
    // inesperados (ej: base de datos caída).
    // =========================================================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {

        // Log con error grave para que los desarrolladores investiguen el StackTrace.
        log.error("Error interno no controlado | URI: {} | Mensaje: {}", request.getRequestURI(), ex.getMessage(), ex);

        // Arma una respuesta genérica para ocultar detalles sensibles (como nombres de
        // tablas).
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(), // Fecha y hora.
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // Código 500.
                "Error interno del servidor", // Título.
                "Ha ocurrido un error inesperado. Por favor contacte al administrador del sistema.", // Mensaje de
                                                                                                     // seguridad.
                request.getRequestURI() // Ruta.
        );

        // Retorna HTTP 500.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ReservaConflictException.class)
    public ResponseEntity<Map<String, Object>> handleReservaConflictException(ReservaConflictException ex, HttpServletRequest request) {
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value()); 
        response.put("error", "Conflicto de Agenda");
        response.put("message", ex.getMessage());
        response.put("path", request.getRequestURI());
        
        // Inyectamos el arreglo de conflictos directamente
        response.put("reservas", ex.getReservas());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}