// Define la "carpeta" donde vive este archivo, agrupado con otros "formularios" de entrada.
package com.fleetops.vehicles.dto.request;

// Importa las herramientas de validación de Java para asegurar que los datos no vengan vacíos o mal formados.
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: DTO (Data Transfer Object) implementado como 'record'
// ¿Qué hace? Funciona como un formulario o "sobre" que viaja desde el exterior (Postman, web, app)
// hacia nuestro servidor. Su único trabajo es transportar datos, sin incluir lógica compleja.
// 
// Nota sobre Java: Al usar la palabra 'record', hacemos que este formulario sea inmutable.
// Ejemplo: Es como una hoja de reclamación escrita con tinta. El cliente la llena y la entrega; 
// nadie en el camino puede borrar o modificar lo que escribió ahí.
// =========================================================================================
public record EstadoCambioRequest(

    // =========================================================================================
    // ANOTACIÓN: @NotBlank
    // ¿Qué hace? Obliga a que el texto exista, no sea nulo y no contenga solamente espacios ("   ").
    // Ejemplo: Si el operador intenta enviar un nuevo estado en blanco (""), el sistema lo rechaza 
    // al instante devolviendo el 'message' definido, sin siquiera molestar a la base de datos.
    // =========================================================================================
    @NotBlank(message = "El nuevo estado es obligatorio")
    String nuevoEstado,

    // Exigimos que haya una justificación para cambiar el estado del vehículo (vital para auditoría).
    @NotBlank(message = "El motivo del cambio es obligatorio")
    
    // =========================================================================================
    // ANOTACIÓN: @Size
    // ¿Qué hace? Limita la cantidad mínima y máxima de caracteres que puede tener un campo de texto.
    //
    // REGLA DE NEGOCIO: Calidad en la Auditoría.
    // Evita que los empleados sean perezosos y escriban cosas como "ok" o "x" para salir del paso.
    // Se les obliga a detallar el problema con al menos 5 letras.
    // Ejemplo: El sistema rechazará "Mal", pero aceptará "Motor averiado".
    // =========================================================================================
    @Size(min = 5, max = 255, message = "El motivo debe tener entre 5 y 255 caracteres")
    String motivoCambio,

    // =========================================================================================
    // ANOTACIÓN: @NotBlank
    // Obliga a registrar quién o qué sistema hizo la petición. 
    // Ejemplo: Deja guardado si el cambio lo pidió la "App-Mecánicos" o el "Portal-Administrativo".
    // =========================================================================================
    @NotBlank(message = "El servicio de origen es obligatorio")
    String servicioOrigen,

    // =========================================================================================
    // OPCIONALIDAD: Al NO tener anotaciones de validación, este campo es totalmente opcional.
    // Ejemplo: Un código de ticket de soporte externo. Si el sistema que nos llama no tiene 
    // ese código, el dato llegará vacío (null) y nuestra API no lanzará ningún error.
    // =========================================================================================
    String idCorrelacion
) {}