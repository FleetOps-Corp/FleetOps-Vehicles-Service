// Define la "carpeta" lógica donde reside este contrato, parte de la capa de servicios de aplicación.
package com.fleetops.vehicles.services.application;

// Importa los DTOs de petición y respuesta, garantizando que el servicio solo hable con objetos limpios.
import com.fleetops.vehicles.dto.request.ReservaRequest;
import com.fleetops.vehicles.dto.request.UpdateReservaDatesRequest;
import com.fleetops.vehicles.dto.response.AgendaReservaResponse;
import com.fleetops.vehicles.dto.response.ReservaResponse;
import com.fleetops.vehicles.dto.response.SagaResponse;
// Importa las entidades necesarias para el modelo de dominio.
import com.fleetops.vehicles.models.entities.EstadoReserva;
import com.fleetops.vehicles.models.entities.ReservaVehiculo;

import java.util.List;
// Importa herramientas del core de Java para manejar resultados opcionales y tipos de identificador.
import java.util.Optional;
import java.util.UUID;

// Importa herramientas de Spring para paginación (evita cargar miles de registros en memoria).
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// =========================================================================================
// PATRÓN DE DISEÑO: Interface Segregation (Segregación de Interfaces).
// ¿Qué hace? Define un contrato estricto con las operaciones que el sistema puede hacer, 
// ocultando los detalles complejos de la implementación (como llamadas a Kafka o bases de datos).
// =========================================================================================
public interface SagaService {

    // Método para arrancar una transacción distribuida (Saga) buscando el vehículo
    // por su ID interno.
    // REGLA DE NEGOCIO: Antes de reservar, valida políticas de disponibilidad
    // (SOAT/RTM) y solapamiento de fechas.
    ReservaResponse iniciarReserva(UUID idVehiculo, ReservaRequest request);

    // Método para arrancar la transacción buscando el vehículo por su placa pública
    // (usado por operadores de campo).
    ReservaResponse iniciarReservaByPlaca(String placa, ReservaRequest request);

    // Método para finalizar definitivamente una reserva que estaba en estado
    // "PENDIENTE".
    // REGLA DE NEGOCIO: Solo se confirma si la fecha actual está dentro de la
    // ventana del viaje.
    Optional<ReservaResponse> confirmarReserva(UUID idReserva);

    // Método para confirmar una reserva utilizando la placa, facilitando la
    // operación rápida.
    List<ReservaResponse> confirmarReservaPorPlaca(String numeroPlaca);

    // metodo para actualizar fechas reservas
    ReservaResponse actualizarFechasReserva(UUID idReserva, UpdateReservaDatesRequest request);

    // Método para deshacer (rollback) una reserva específica usando su ID.
    // PATRÓN: Compensating Transaction (Transacción de Compensación). Es el
    // "Ctrl+Z" del sistema.
    boolean compensarPorReservaId(UUID reservaId, String motivo);

    // Método para deshacer toda la transacción distribuida usando el ID del
    // expediente global (Saga).
    // REGLA DE NEGOCIO: Retroactividad. No permite compensar Sagas completadas hace
    // más de 15 días (protección contable).
    boolean compensarSaga(UUID sagaId, String motivo);

    // Método de solo lectura para consultar el estado actual de una reserva
    // específica.
    Optional<ReservaResponse> findReservaById(UUID idReserva);

    // Método para consultar el historial global de todas las reservas de la empresa
    // (paginado).
    Page<ReservaResponse> findAllReservas(Pageable pageable);

    // Método para consultar la bandeja de entrada de reservas pendientes de
    // confirmación.
    Page<ReservaResponse> findReservasPendientes(Pageable pageable);

    // Método para listar todas las reservas confirmadas exitosamente.
    Page<ReservaResponse> findReservasConfirmadas(Pageable pageable);

    // Método para listar todas las reservas que no pudieron completarse.
    Page<ReservaResponse> findReservasFallidas(Pageable pageable);

    // Método para listar todas las reservas que fueron canceladas intencionalmente.
    Page<ReservaResponse> findReservasCanceladas(Pageable pageable);

    // Método para consultar el historial de reservas de un vehículo específico
    // usando su placa.
    Page<ReservaResponse> findReservasByPlaca(String placa, Pageable pageable);

    // Método para consultar reservas filtrando por placa y estado específico
    // (auditoría avanzada).
    Page<ReservaResponse> findReservasByPlacaAndEstado(String placa, EstadoReserva estado, Pageable pageable);

    // Método para listar todos los expedientes de Sagas globales.
    Page<SagaResponse> findAllSagas(Pageable pageable);

    // Método para obtener las sagas que acaban de ser creadas o instanciadas en el
    // sistema.
    Page<SagaResponse> findSagasIniciadas(Pageable pageable);

    // Método para obtener las sagas que están actualmente activas y procesando sus
    // pasos.
    Page<SagaResponse> findSagasEnProgreso(Pageable pageable);

    // Método para obtener las sagas que finalizaron con éxito tras completar todos
    // sus pasos.
    Page<SagaResponse> findSagasCompletadas(Pageable pageable);

    // Método para obtener las sagas que fallaron en algún punto y requieren
    // revisión técnica.
    Page<SagaResponse> findSagasFallidas(Pageable pageable);

    // Método para obtener las sagas que fueron revertidas (compensadas) debido a
    // una falla anterior.
    Page<SagaResponse> findSagasCompensadas(Pageable pageable);

    // Métodos para rastrear Sagas específicas de un vehículo por su placa.
    Page<SagaResponse> findSagasByPlaca(String placa, Pageable pageable);

    // Método para rastrear Sagas de un vehículo filtrando también por estado de
    // Saga.
    Page<SagaResponse> findSagasByPlacaAndEstado(String placa, com.fleetops.vehicles.models.entities.EstadoSaga estado,
            Pageable pageable);

}