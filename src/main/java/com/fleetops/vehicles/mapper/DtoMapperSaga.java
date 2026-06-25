// Define la "carpeta" lógica del proyecto donde se agrupan los convertidores (mappers) de datos.
package com.fleetops.vehicles.mapper;

// Importa la entidad de la Saga (el registro de la transacción distribuida).
import com.fleetops.vehicles.models.entities.SagaVehiculo;
// Importa la entidad del vehículo relacionada.
import com.fleetops.vehicles.models.entities.Vehiculo;
// Importa el DTO de respuesta (el sobre que enviamos al Frontend).
import com.fleetops.vehicles.dto.response.SagaResponse;
// Importa la anotación de Spring para registrar esta clase como un componente gestionado.
import org.springframework.stereotype.Component;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: Data Mapper (Traductor)
// ¿Qué hace? Este traductor toma el objeto complejo 'SagaVehiculo' (con sus estados y relaciones)
// y lo convierte en un 'SagaResponse' limpio, plano y seguro para ser leído por el usuario.
// =========================================================================================

// @Component: Registra esta clase en el contenedor de Spring para poder usarla en otros servicios.
@Component
public class DtoMapperSaga {

    // Método principal: Convierte la entidad de base de datos a un objeto de respuesta (DTO).
    public SagaResponse toDto(SagaVehiculo saga) {
        // REGLA DE SEGURIDAD: Defensiva. Si la saga es nula, regresamos nulo para evitar errores 500.
        if (saga == null) return null;

        // Extraemos la entidad vehículo relacionada para facilitar la lectura del código.
        Vehiculo v = saga.getVehiculo();

        // Construimos el objeto inmutable SagaResponse.
        return new SagaResponse(
                // 1. ID único de la Saga.
                saga.getIdSaga(),
                // 2. ID del vehículo (navegación segura: si v es null, devolvemos null).
                v != null ? v.getIdVehiculo() : null,
                // 3. Tipo de operación (Ej: RESERVA, CANCELACION).
                saga.getTipoOperacion(),
                // 4. REGLA DE NEGOCIO: Convertimos el Enum a texto plano (.name()) para compatibilidad con JSON.
                saga.getEstadoSaga() != null ? saga.getEstadoSaga().name() : null,
                // 5. La clave de idempotencia (esencial para evitar pagos o procesos duplicados).
                saga.getClaveIdempotencia(),
                // 6. REGLA DE NEGOCIO (Observabilidad): Cuántos intentos fallidos lleva esta saga.
                saga.getIntentos(),
                // 7. El payload (el JSON original de la petición guardado para auditoría).
                saga.getPayload(),
                // 8. El último mensaje de error técnico (para que soporte sepa qué falló).
                saga.getUltimoError(),
                // 9. REGLA DE NEGOCIO (Compensación): Registro de qué proceso deshizo este cambio si falló.
                saga.getCompensadoPor(),
                // 10. Fecha de creación de la transacción.
                saga.getCreadoEn(),
                // 11. Fecha de última actualización del estado.
                saga.getActualizadoEn(),
                
                // =========================================================================
                // PATRÓN DE DISEÑO: DTO Flattening (Aplanamiento de datos)
                // =========================================================================
                // 12. Extraemos solo la placa del vehículo (v) para evitar enviar el objeto vehículo entero.
                v != null ? v.getNumeroPlaca() : null
        );
    }
}