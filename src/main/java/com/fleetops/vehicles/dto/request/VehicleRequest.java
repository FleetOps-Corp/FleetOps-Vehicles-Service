// Define la "carpeta lógica" donde guardamos los formularios que el cliente envía hacia la API.
package com.fleetops.vehicles.dto.request;

// Importa todas las herramientas de validación estándar de Java (como @NotNull, @Min, @Pattern, etc.).
import jakarta.validation.constraints.*;
// Importa la herramienta de Java para manejar fechas exactas (sin tener en cuenta la hora).
import java.time.LocalDate;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: DTO (Data Transfer Object) implementado como 'record'
// ¿Qué hace? Funciona como un formulario de matrícula. Recibe toda la información del 
// nuevo vehículo desde el Frontend (React/Angular/App) y la transporta segura hasta la lógica de negocio.
// Ejemplo: Es como la planilla física que llenas al comprar un auto; agrupa todos los datos 
// en un solo paquete inalterable para que no se pierdan ni se modifiquen por el camino.
// =========================================================================================
public record VehicleRequest(
    
    // =========================================================================================
    // ANOTACIÓN: @NotNull
    // ¿Qué hace? Asegura que el dato (en este caso un número) no venga vacío o nulo en el JSON.
    // Ejemplo: Exige que el formulario diga explícitamente a qué categoría pertenece el vehículo (Ej: 3 para Camioneta).
    // =========================================================================================
    @NotNull(message = "El tipo de vehículo es obligatorio")
    Long idTipoVehiculo,

    // Asegura que la placa exista y no sea solo un espacio en blanco.
    @NotBlank(message = "La placa es obligatoria")
    // =========================================================================================
    // ANOTACIÓN: @Pattern (Expresión Regular o Regex)
    // ¿Qué hace? Obliga a que el texto introducido cumpla con un formato visual estricto, 
    // dictado por una fórmula matemática.
    //
    // REGLA DE NEGOCIO: Formato de Placa Estricto.
    // Garantiza que la flota solo registre placas válidas colombianas (autos o motos).
    // Ejemplo: Acepta "AAA123" (3 letras, 3 números) o "AAA12A" (formato motos). 
    // Si un operario intenta guardar un camión con la placa "PERRO1", el sistema lo bloquea.
    // =========================================================================================
    @Pattern(regexp = "^[A-Z]{3}\\d{3}$|^[A-Z]{3}\\d{2}[A-Z]$", message = "Formato de placa inválido")
    String numeroPlaca,

    // Exige registrar la marca del fabricante. 
    // Ejemplo: El sistema bloquea "" pero acepta "Chevrolet" o "Volvo".
    @NotBlank(message = "La marca es obligatoria")
    String marca,

    // Exige registrar la línea o modelo del vehículo.
    // Ejemplo: "NPR", "FH16".
    @NotBlank(message = "El modelo es obligatorio")
    String modelo,

    // Exige que manden el año numérico de fabricación (como número, no como texto).
    @NotNull(message = "El año de fabricación es obligatorio")
    // =========================================================================================
    // ANOTACIÓN: @Min
    // ¿Qué hace? Establece el límite matemático inferior permitido para un campo numérico.
    //
    // REGLA DE NEGOCIO: Antigüedad Máxima Permitida.
    // Por políticas de seguridad y chatarrización, no se afilian vehículos demasiado viejos.
    // Ejemplo: Si el operario intenta ingresar un camión modelo "1989", la API frena la petición 
    // indicando que el año debe ser mayor a 1990.
    // =========================================================================================
    @Min(value = 1990, message = "El año debe ser mayor a 1990")
    Integer anioFabricacion,

    // Aclaración técnica: Al usar @NotNull (y no @NotBlank), el cliente está obligado a enviar 
    // la variable "color" en el JSON, pero a nivel de código se le permite enviar un texto vacío "".
    @NotNull(message = "El color es obligatorio")
    String color,

    // Exige el serial físico del chasis del automotor, vital para temas legales y de tránsito.
    // Ejemplo: "1HGCM82633A004".
    @NotBlank(message = "El número de chasis es obligatorio")
    String numeroChasis,

    // Exige el serial del motor, necesario para el control de inventario cuando hay cambio de piezas.
    @NotBlank(message = "El número de motor es obligatorio")
    String numeroMotor,

    // Obliga a que el campo del kilometraje venga incluido en la petición inicial.
    @NotNull(message = "El kilometraje es obligatorio")
    // =========================================================================================
    // ANOTACIÓN: @PositiveOrZero
    // ¿Qué hace? Asegura que el número sea positivo (1, 2, 3...) o cero (0).
    //
    // REGLA DE NEGOCIO: Kilometraje Lógico.
    // Previene errores de digitación absurdos.
    // Ejemplo: Si el camión es nuevecito de fábrica, su kilometraje es 0 (válido). 
    // Pero el sistema rechazará un registro de "-50 kilómetros" porque es físicamente imposible.
    // =========================================================================================
    @PositiveOrZero(message = "El kilometraje no puede ser negativo")
    Integer kilometraje,

    // Exige saber en qué ciudad operará principalmente el vehículo.
    // Ejemplo: "Bogotá".
    @NotBlank(message = "La ciudad de operación es obligatoria")
    String ciudadOperacion,

    // Exige saber a qué sede física (patio) está asignado.
    // Ejemplo: "Patio Norte".
    @NotBlank(message = "La sede de operación es obligatoria")
    String sedeOperacion,

    // Exige que se asigne un estado inicial al vehículo en su momento de creación.
    // Ejemplo: "DISPONIBLE" o "EN_MANTENIMIENTO".
    @NotBlank(message = "El estado del vehículo es obligatorio")
    String estadoVehiculo,

    // Obliga a mandar la fecha de expiración del Seguro Obligatorio.
    @NotNull(message = "La fecha del SOAT es obligatoria")
    // =========================================================================================
    // ANOTACIÓN: @FutureOrPresent
    // ¿Qué hace? Compara la fecha enviada con el reloj interno del servidor.
    //
    // REGLA DE NEGOCIO: Vigencia Legal (SOAT).
    // Garantiza que el documento esté vigente al momento de matricular el carro en el sistema.
    // Ejemplo: Evita que un administrador intente registrar un camión subiendo un papel de SOAT 
    // que se venció hace 3 meses.
    // =========================================================================================
    @FutureOrPresent(message = "El SOAT debe estar vigente")
    LocalDate fechaSoat,

    // Mismo control estricto de vigencia legal, pero aplicado a la Revisión Técnico Mecánica.
    @NotNull(message = "La fecha de RTM es obligatoria")
    @FutureOrPresent(message = "La revisión tecnomecánica (RTM) debe estar vigente")
    LocalDate fechaRtm,
    
    // Obliga a incluir la fecha en la que el camión visitó el taller por última vez.
    @NotNull(message = "La fecha del ultimo mantenimiento es obligatoria")
    // =========================================================================================
    // NOTA DE DISEÑO:
    // Como los mantenimientos ocurren en el pasado, AQUÍ NO usamos @FutureOrPresent. 
    // Cualquier fecha (incluso de hace 2 años) es válida mientras el campo no venga nulo.
    // =========================================================================================
    LocalDate fechaUltimoMant

) {}