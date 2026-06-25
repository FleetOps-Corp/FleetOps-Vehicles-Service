// Define la "carpeta" lógica donde agrupamos los formularios de entrada de nuestra API.
package com.fleetops.vehicles.dto.request;

// Importa las herramientas de validación de Java para blindar la entrada de datos.
import jakarta.validation.constraints.*;
// Importa la herramienta para manejar fechas (sin hora).
import java.time.LocalDate;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: DTO (Data Transfer Object) implementado como 'record'
// ¿Qué hace? Actúa como el formulario de edición. Cuando el administrador quiere cambiar
// detalles de un vehículo, los nuevos datos viajan empaquetados en este objeto inmutable.
// Ejemplo: Es como una hoja de corrección de datos que se entrega en una oficina para 
// actualizar el expediente de un vehículo que ya estaba registrado.
// =========================================================================================
public record VehicleUpdateRequest(
        
        // Exige el ID de la categoría (Ej: 1 para Sedán, 2 para Carga).
        @NotNull(message = "El tipo de vehículo es obligatorio") 
        Long idTipoVehiculo,

        @NotBlank(message = "La placa es obligatoria")
        // =========================================================================================
        // ANOTACIÓN: @Pattern (Regex)
        // REGLA DE NEGOCIO: Integridad de Matrícula.
        // Ejemplo: Obliga a que la placa tenga el formato oficial (3 letras + 3 números o 
        // 3 letras + 2 números + 1 letra). Si alguien intenta enviar "ABC-123" (con guión), 
        // el sistema lo rechaza por no cumplir la norma de FleetOps.
        // =========================================================================================
        @Pattern(regexp = "^[A-Z]{3}\\d{3}$|^[A-Z]{3}\\d{2}[A-Z]$", message = "Formato de placa inválido") 
        String numeroPlaca,

        // Marca del fabricante. Ejemplo: "Volvo".
        @NotBlank(message = "La marca es obligatoria") 
        String marca,

        // Modelo específico. Ejemplo: "FH16".
        @NotBlank(message = "El modelo es obligatorio") 
        String modelo,

        @NotNull(message = "El año de fabricación es obligatorio")
        // =========================================================================================
        // ANOTACIÓN: @Min
        // REGLA DE NEGOCIO: Límite de Antigüedad.
        // Ejemplo: Por normas de la empresa, FleetOps no mantiene vehículos anteriores a 1990. 
        // Si el usuario edita y pone "1985", la API bloquea el cambio inmediatamente.
        // =========================================================================================
        @Min(value = 1990, message = "El año debe ser mayor a 1990") 
        Integer anioFabricacion,

        // Obliga a que el campo del color esté presente (aunque sea un texto vacío).
        @NotNull(message = "El color es obligatorio")
        String color,

        // Serial del chasis: clave para identificar al vehículo físicamente en caso de robo o siniestro.
        @NotBlank(message = "El número de chasis es obligatorio") 
        String numeroChasis,

        // Serial del motor: dato legal necesario para trámites de tránsito.
        @NotBlank(message = "El número de motor es obligatorio") 
        String numeroMotor,

        @NotNull(message = "El kilometraje es obligatorio")
        // =========================================================================================
        // ANOTACIÓN: @PositiveOrZero
        // REGLA DE NEGOCIO: Lógica de Odómetro.
        // Ejemplo: Un vehículo nunca puede tener un kilometraje negativo. Si el operario 
        // ingresa "-100" por error, el sistema bloquea la actualización para proteger la data.
        // =========================================================================================
        @PositiveOrZero(message = "El kilometraje no puede ser negativo") 
        Integer kilometraje,

        // Ciudad donde el camión realiza su trabajo.
        @NotBlank(message = "La ciudad de operación es obligatoria") 
        String ciudadOperacion,

        // Sede (patio) base del vehículo.
        @NotBlank(message = "La sede de operación es obligatoria") 
        String sedeOperacion,

        // Estado del vehículo (Ej: DISPONIBLE, MANTENIMIENTO).
        @NotBlank(message = "El estado del vehículo es obligatorio") 
        String estadoVehiculo,

        @NotNull(message = "La fecha del SOAT es obligatoria")
        // =========================================================================================
        // ANOTACIÓN: @FutureOrPresent
        // REGLA DE NEGOCIO: Vigencia Legal de Seguros.
        // Ejemplo: El sistema valida que el documento subido (SOAT) esté vigente. Si alguien intenta 
        // registrar una fecha de vencimiento que ya pasó, la validación falla para evitar operar sin seguro.
        // =========================================================================================
        @FutureOrPresent(message = "El SOAT debe estar vigente") 
        LocalDate fechaSoat,

        @NotNull(message = "La fecha de RTM es obligatoria")
        // Mismo control de vigencia para la Revisión Técnico Mecánica (RTM).
        @FutureOrPresent(message = "La revisión tecnomecánica (RTM) debe estar vigente") 
        LocalDate fechaRtm,

        // Fecha del último mantenimiento (no se exige que sea futura porque es un registro de un evento pasado).
        @NotNull(message = "La fecha del ultimo mantenimiento es obligatoria")
        LocalDate fechaUltimoMant
) {}