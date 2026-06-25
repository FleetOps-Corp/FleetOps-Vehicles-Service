// Define la "carpeta lógica" donde se agrupan los formularios de entrada de nuestra API.
package com.fleetops.vehicles.dto.request;

// Importa nuestra propia regla de negocio para fechas creada a la medida.
import com.fleetops.vehicles.services.domain.ValidDateRange;
// Importa las herramientas estándar de validación de Java.
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
// Importa la clase para manejar fechas con hora exacta.
import java.time.LocalDateTime;

// =========================================================================================
// ANOTACIÓN: @ValidDateRange (Anotación Personalizada)
// ¿Qué hace? Es una regla que nosotros mismos inventamos. Se ejecuta antes de dejar entrar 
// los datos al sistema y revisa ambas fechas al mismo tiempo.
//
// REGLA DE NEGOCIO: Coherencia Cronológica.
// Evita el error humano de intentar devolver un vehículo antes de haberlo retirado.
// Ejemplo: El sistema bloquea si el cliente dice "Me llevo el camión el viernes y lo devuelvo el martes anterior".
// =========================================================================================
@ValidDateRange

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: DTO (Data Transfer Object) implementado como 'record'
// ¿Qué hace? Un DTO es un objeto "tonto" que solo sirve para transportar datos por la red (como JSON). 
// Al usar la palabra 'record' (disponible en Java moderno), este sobre se vuelve "inmutable".
// Ejemplo: Es como un formulario sellado en una funda de plástico. El cliente lo llena (Frontend), 
// lo envía, y una vez que viaja por internet, nadie puede tachar o alterar los datos a mitad del camino.
// =========================================================================================
public record ReservaRequest(
    
    // =========================================================================================
    // ANOTACIÓN: @NotBlank
    // ¿Qué hace? Prohíbe que este texto venga nulo o como un espacio en blanco ("   ").
    // Ejemplo: Si el microservicio de Asignaciones envía un formulario sin poner su ID de orden, 
    // el sistema frena la petición y devuelve el mensaje: "El ID de asignación externa es obligatorio".
    // =========================================================================================
    @NotBlank(message = "El ID de asignación externa es obligatorio")
    String idAsignacionExt,

    // Exige que siempre sepamos el nombre o correo del responsable que pide el camión.
    // Ejemplo: Si envían "solicitadoPor": "", el sistema arroja error automático.
    @NotBlank(message = "El solicitante es obligatorio")
    String solicitadoPor,

    // =========================================================================================
    // ANOTACIÓN: @NotNull
    // ¿Qué hace? A diferencia de @NotBlank (que es solo para textos), @NotNull se usa para 
    // números, fechas u objetos complejos, asegurando que no vengan vacíos (null).
    // Ejemplo: Si se les olvida enviar la variable "fechaInicio" en el JSON, la API lo detecta de inmediato.
    // =========================================================================================
    @NotNull(message = "La fecha de inicio es obligatoria")
    
    // =========================================================================================
    // ANOTACIÓN: @FutureOrPresent
    // ¿Qué hace? Compara la fecha enviada con el reloj actual del servidor.
    //
    // REGLA DE NEGOCIO: No existen reservas en el pasado.
    // Ejemplo: Si hoy es 23 de Junio, y el usuario intenta hacer una reserva con fecha del 20 de Junio, 
    // el sistema cancelará la acción diciendo "La fecha de inicio debe ser presente o futura".
    // =========================================================================================
    @FutureOrPresent(message = "La fecha de inicio debe ser presente o futura")
    LocalDateTime fechaInicio,

    // Validaciones idénticas a las de arriba, pero aplicadas a la fecha de entrega del camión.
    @NotNull(message = "La fecha de fin es obligatoria")
    @FutureOrPresent(message = "La fecha de fin debe ser presente o futura")
    LocalDateTime fechaFin,

    // =========================================================================================
    // PATRÓN DE DISEÑO DETECTADO: Idempotency Key (Llave de Idempotencia)
    // ¿Qué hace? Exige que el cliente (Frontend/App) envíe un código único para esta acción.
    //
    // REGLA DE NEGOCIO: Prevención de reservas duplicadas por fallos de red.
    // Ejemplo: Si el despachador tiene el internet lento y le da 4 clics furiosos al botón de 
    // "Reservar Camión", todos esos clics viajarán con la misma 'claveIdempotencia'. Nuestro 
    // sistema procesará la primera petición y descartará las otras tres, evitando cobrar 4 veces.
    // =========================================================================================
    @NotBlank(message = "La clave de idempotencia es obligatoria")
    String claveIdempotencia
) {}