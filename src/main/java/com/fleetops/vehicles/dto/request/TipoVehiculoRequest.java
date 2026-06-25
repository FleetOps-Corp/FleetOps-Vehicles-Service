// Define la "carpeta lógica" donde se agrupan los formularios de entrada de nuestra API.
package com.fleetops.vehicles.dto.request;

// Importa las herramientas estándar de validación de Java para proteger el sistema de datos inválidos.
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: DTO (Data Transfer Object) implementado como 'record'
// ¿Qué hace? Un DTO es un objeto especializado en transportar datos por la red (ej. desde el navegador al servidor).
// Al definirlo como 'record' (disponible en Java moderno), este "sobre" de datos se vuelve inmutable.
// Ejemplo: Es como un formulario plastificado. El usuario lo llena, lo envía, y durante el viaje nadie puede borrar ni alterar lo escrito.
// =========================================================================================
public record TipoVehiculoRequest(

        // =========================================================================================
        // ANOTACIÓN: @NotBlank
        // ¿Qué hace? Bloquea la petición si el texto es nulo, está vacío o solo tiene espacios en blanco.
        // Ejemplo: Si el cliente envía en Postman { "nombreTipo": "   " }, el sistema frena en seco 
        // y responde con el error definido, sin llegar a molestar a la base de datos.
        // =========================================================================================
        @NotBlank(message = "El nombre del tipo es obligatorio")

        // =========================================================================================
        // ANOTACIÓN: @Size
        // ¿Qué hace? Fija un límite estricto de caracteres para proteger el espacio en disco.
        // Ejemplo: Previene que un usuario malintencionado o un error de sistema envíe un texto de 
        // 5000 letras y colapse la columna SQL que solo soporta 100.
        // =========================================================================================
        @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres") 
        String nombreTipo,

        // Obliga a que la descripción de la categoría contenga texto real y útil.
        @NotBlank(message = "La descripcion es obligatoria")
        // Mismo escudo protector: asegura que el texto encaje perfectamente en el límite de la base de datos.
        @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres") 
        String descripcion,

        // =========================================================================================
        // ANOTACIÓN: @NotNull
        // ¿Qué hace? A diferencia de @NotBlank (que es para textos), se usa para variables matemáticas 
        // (números) u objetos, asegurando que el dato sí venga en el archivo JSON.
        // Ejemplo: Si al cliente simplemente se le olvida mandar el campo "capacidadCarga", la API avisa de inmediato.
        // =========================================================================================
        @NotNull(message = "El campo 'capacidadCarga' no puede quedar vacío")

        // =========================================================================================
        // ANOTACIÓN: @Positive
        // ¿Qué hace? Obliga a que el número ingresado sea estrictamente mayor a cero.
        //
        // REGLA DE NEGOCIO: Coherencia Física.
        // Es imposible en el mundo real que un vehículo tenga una capacidad de carga vacía (0) o negativa.
        // Ejemplo: Si un operario teclea por error "-500 kilos", el sistema lo rechaza porque 
        // viola las leyes de la física y la lógica logística de la empresa.
        // =========================================================================================
        @Positive(message = "El campo 'capacidadCarga' debe ser mayor a 0") 
        Double capacidadCarga
) {}